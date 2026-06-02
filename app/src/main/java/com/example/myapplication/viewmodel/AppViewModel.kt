package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.NetworkMonitor
import com.example.myapplication.data.OfflineCacheManager
import com.example.myapplication.model.BaggageDeclaration
import com.example.myapplication.model.BoardingPass
import com.example.myapplication.model.CheckInDraft
import com.example.myapplication.model.PassportInfo
import com.example.myapplication.model.SpecialRequests
import com.example.myapplication.repository.AppRepository
import com.example.myapplication.util.BoardingPassPdfGenerator
import com.example.myapplication.util.BoardingPassSharer
import com.example.myapplication.util.CheckInNotifier
import com.example.myapplication.util.PassportValidator
import com.example.myapplication.util.QrGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Single AndroidViewModel for the entire app.
 * Owns the [AppUiState] and orchestrates the repository, offline cache, network
 * monitor and side-effects (notifications, PDF, share intents). Side-effect-only
 * helpers live in `util/` so this class stays focused on state.
 */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application.applicationContext)
    private val networkMonitor = NetworkMonitor(application.applicationContext)
    private val offlineCache = OfflineCacheManager(application.applicationContext)
    private val notifier = CheckInNotifier(application.applicationContext)
    private var wasOffline = false

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        // 1. Restore offline cache immediately so the UI has data without network.
        viewModelScope.launch {
            val cache = offlineCache.current()
            _uiState.update {
                it.copy(
                    currentUser = cache.user,
                    cachedBoardingPasses = cache.boardingPasses,
                    cachedFlights = cache.flights
                )
            }
        }

        // 2. Watch connectivity → auto-sync when we come back online.
        viewModelScope.launch {
            networkMonitor.isOnline.collectLatest { online ->
                _uiState.update { it.copy(isOnline = online) }
                if (online && wasOffline) {
                    val uid = _uiState.value.currentUser?.id
                    if (uid != null) {
                        refreshUserData(uid)
                        _uiState.update { it.copy(statusMessage = "Synced with server.") }
                    }
                }
                wasOffline = !online
            }
        }

        // 3. Validate stored session against /auth/me on cold start.
        viewModelScope.launch {
            val restored = repository.restoreSession().getOrNull()
            if (restored != null) {
                _uiState.update { it.copy(currentUser = restored, isRestoringSession = false) }
                offlineCache.saveUser(restored)
                refreshUserData(restored.id)
            } else {
                _uiState.update { it.copy(isRestoringSession = false) }
            }
        }
    }

    /** Pulls fresh boarding passes + flights from the backend, persists to offline cache. */
    private fun refreshUserData(userId: String) {
        viewModelScope.launch {
            val passes  = repository.getUserBoardingPasses(userId).getOrNull()
            val flights = repository.getUserFlights(userId).getOrNull()
            _uiState.update {
                it.copy(
                    cachedBoardingPasses = passes ?: it.cachedBoardingPasses,
                    cachedFlights = flights ?: it.cachedFlights
                )
            }
            if (passes  != null) offlineCache.saveBoardingPasses(passes)
            if (flights != null) offlineCache.saveFlights(flights)
        }
    }

    // ─────────────── AUTH ───────────────

    fun toggleAuthMode() {
        _uiState.update { it.copy(isAuthMode = !it.isAuthMode) }
    }

    fun register(fullName: String, email: String, phone: String, password: String) {
        if (fullName.isBlank() || email.isBlank() || password.length < 6) {
            _uiState.update { it.copy(statusMessage = "Please complete all fields. Password must be at least 6 characters.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.register(fullName, email, phone, password)
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false, currentUser = user,
                            statusMessage = "Welcome aboard, ${user.fullName.split(" ").first()}!"
                        )
                    }
                    offlineCache.saveUser(user)
                    refreshUserData(user.id)
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, statusMessage = e.message) } }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(statusMessage = "Please enter your email and password.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.login(email, password)
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false, currentUser = user,
                            statusMessage = "Welcome back, ${user.fullName.split(" ").first()}!"
                        )
                    }
                    offlineCache.saveUser(user)
                    refreshUserData(user.id)
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, statusMessage = e.message) } }
        }
    }

    fun signInWithGoogle(email: String = "", displayName: String = "") {
        val finalEmail = email.trim().ifBlank { "guest@mypass.dz" }
        val finalName  = displayName.trim().ifBlank { "Guest Traveler" }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.googleSignIn(finalName, finalEmail)
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false, currentUser = user,
                            statusMessage = "Signed in as ${user.fullName.split(" ").first()}."
                        )
                    }
                    offlineCache.saveUser(user)
                    refreshUserData(user.id)
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, statusMessage = e.message) } }
        }
    }

    fun logout() {
        repository.logout()
        viewModelScope.launch { offlineCache.clear() }
        _uiState.update { AppUiState(isRestoringSession = false) }
    }

    fun updateProfile(fullName: String, phone: String) {
        val userId = _uiState.value.currentUser?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.updateProfile(userId, fullName, phone)
                .onSuccess { user -> _uiState.update { it.copy(isLoading = false, currentUser = user, statusMessage = "Profile updated.") } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, statusMessage = e.message) } }
        }
    }

    // ─────────────── FLIGHT LOOKUP ───────────────

    fun lookupFlight(bookingReference: String, lastName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = null) }
            repository.findFlight(bookingReference, lastName)
                .onSuccess { itinerary ->
                    val seatMap = repository.getSeatMap(itinerary.id).getOrNull().orEmpty()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lookupResult = itinerary,
                            draft = CheckInDraft(itinerary = itinerary),
                            seatMap = seatMap,
                            checkInStep = 0,
                            statusMessage = "Booking retrieved successfully."
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, statusMessage = e.message) } }
        }
    }

    // ─────────────── CHECK-IN DRAFT ───────────────

    fun attachPassportInfo(info: PassportInfo) {
        _uiState.update { state ->
            val issues = state.draft?.itinerary?.let { PassportValidator.validate(info, it) }.orEmpty()
            val msg = when {
                issues.isEmpty() -> "Passport information extracted successfully."
                else -> issues.first()
            }
            state.copy(
                passportRawText = info.rawText,
                passportIssues = issues,
                draft = state.draft?.copy(passportInfo = info),
                statusMessage = msg
            )
        }
    }

    fun setCheckInStep(step: Int) {
        _uiState.update { it.copy(checkInStep = step) }
    }

    fun updateSeat(seatCode: String) {
        _uiState.update { state -> state.copy(draft = state.draft?.copy(selectedSeat = seatCode)) }
    }

    fun updateBaggage(checked: Int, carryOn: Int, oversized: Int) {
        _uiState.update { state ->
            state.copy(
                draft = state.draft?.copy(
                    baggageDeclaration = BaggageDeclaration(
                        checkedBags = checked,
                        carryOnBags = carryOn,
                        oversizedBags = oversized,
                        totalWeight = checked * 23.0 + carryOn * 7.0
                    )
                )
            )
        }
    }

    fun updateSpecialRequests(requests: SpecialRequests) {
        _uiState.update { state -> state.copy(draft = state.draft?.copy(specialRequests = requests)) }
    }

    fun completeCheckIn() {
        val draft = _uiState.value.draft
        val userId = _uiState.value.currentUser?.id
        if (draft == null || draft.selectedSeat.isNullOrBlank()) {
            _uiState.update { it.copy(statusMessage = "Please select a seat before completing check-in.") }
            return
        }
        if (userId.isNullOrBlank()) {
            _uiState.update { it.copy(statusMessage = "You must be signed in to complete check-in.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.completeCheckIn(userId, draft)
                .onSuccess { pass ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lookupResult = it.lookupResult?.copy(checkInStatus = "Checked-In"),
                            latestBoardingPass = pass,
                            statusMessage = "Check-in completed! Your boarding pass is ready."
                        )
                    }
                    notifier.postCheckInComplete(pass)
                    refreshUserData(userId)
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, statusMessage = "Check-in failed: ${e.message}") } }
        }
    }

    // ─────────────── SYNC ───────────────

    fun synchronize() {
        val userId = _uiState.value.currentUser?.id ?: return
        if (!_uiState.value.isOnline) {
            _uiState.update { it.copy(statusMessage = "Offline. Connect to the internet to sync.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            repository.synchronize(userId)
                .onSuccess { resp ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            cachedBoardingPasses = resp.boardingPasses,
                            cachedFlights = resp.flights,
                            statusMessage = "Synced ${resp.boardingPasses.size} passes, ${resp.flights.size} flights."
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isSyncing = false, statusMessage = e.message) } }
        }
    }

    // ─────────────── UI HELPERS ───────────────

    fun clearMessage()              { _uiState.update { it.copy(statusMessage = null) } }
    fun showMessage(message: String) { _uiState.update { it.copy(statusMessage = message) } }

    fun resetForNewCheckIn() {
        _uiState.update {
            it.copy(
                lookupResult = null, draft = null, seatMap = emptyList(),
                latestBoardingPass = null, checkInStep = 0,
                passportIssues = emptyList()
            )
        }
    }

    // ─────────────── BOARDING-PASS OUTPUT (thin wrappers over util/) ───────────────

    fun generateQrBitmap(content: String, size: Int = 800): Bitmap = QrGenerator.generate(content, size)

    fun saveBoardingPassPdf(context: Context, boardingPass: BoardingPass): String =
        BoardingPassPdfGenerator.save(context, boardingPass).first

    fun shareBoardingPass(context: Context, boardingPass: BoardingPass) =
        BoardingPassSharer.share(context, boardingPass)

    fun openBoardingPassPdf(context: Context, boardingPass: BoardingPass) =
        BoardingPassSharer.openInViewer(context, boardingPass)
}
