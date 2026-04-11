package com.example.myapplication.ui

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.data.AppRepository
import com.example.myapplication.data.NetworkMonitor
import com.example.myapplication.model.*
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.time.Instant
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class AppUiState(
    val currentUser: UserAccount? = null,
    val lookupResult: FlightItinerary? = null,
    val draft: CheckInDraft? = null,
    val seatMap: List<Seat> = emptyList(),
    val latestBoardingPass: BoardingPass? = null,
    val cachedBoardingPasses: List<BoardingPass> = emptyList(),
    val cachedFlights: List<FlightItinerary> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val isOnline: Boolean = true,
    val statusMessage: String? = null,
    val passportRawText: String = "",
    val checkInStep: Int = 0,
    val isAuthMode: Boolean = true  // true = login, false = register
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application.applicationContext)
    private val networkMonitor = NetworkMonitor(application.applicationContext)
    private var hasPendingSync = false

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        ensureNotificationChannel()

        viewModelScope.launch {
            repository.offlineCache.collect { cache ->
                _uiState.update {
                    it.copy(
                        currentUser = cache.user ?: it.currentUser,
                        cachedBoardingPasses = cache.boardingPasses,
                        cachedFlights = cache.flights
                    )
                }
            }
        }

        viewModelScope.launch {
            networkMonitor.isOnline.collectLatest { online ->
                _uiState.update { it.copy(isOnline = online) }
                if (online && hasPendingSync) {
                    synchronize()
                }
            }
        }
    }

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
            val result = repository.register(fullName, email, phone, password)
            result.onSuccess { user ->
                hasPendingSync = true
                _uiState.update { it.copy(isLoading = false, currentUser = user, statusMessage = "Welcome aboard, ${user.fullName}!") }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, statusMessage = error.message) }
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(statusMessage = "Please enter your email and password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.login(email, password)
            result.onSuccess { user ->
                _uiState.update { it.copy(isLoading = false, currentUser = user, statusMessage = "Welcome back, ${user.fullName}!") }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, statusMessage = error.message) }
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.googleSignIn("Google Passenger", "google.user@gmail.com")
            result.onSuccess { user ->
                hasPendingSync = true
                _uiState.update { it.copy(isLoading = false, currentUser = user, statusMessage = "Signed in with Google.") }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, statusMessage = error.message) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update { AppUiState() }
        }
    }

    fun lookupFlight(bookingReference: String, lastName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = null) }
            val result = repository.findFlight(bookingReference, lastName)
            result.onSuccess { itinerary ->
                val seatMap = repository.getSeatMap(itinerary.id, itinerary.bookingReference)
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
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, statusMessage = error.message) }
            }
        }
    }

    fun attachPassportInfo(info: PassportInfo) {
        _uiState.update { state ->
            state.copy(
                passportRawText = info.rawText,
                draft = state.draft?.copy(passportInfo = info),
                statusMessage = "Passport information extracted successfully."
            )
        }
    }

    fun setCheckInStep(step: Int) {
        _uiState.update { it.copy(checkInStep = step) }
    }

    fun updateSeat(seatCode: String) {
        _uiState.update { state ->
            state.copy(draft = state.draft?.copy(selectedSeat = seatCode))
        }
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
        _uiState.update { state ->
            state.copy(draft = state.draft?.copy(specialRequests = requests))
        }
    }

    fun completeCheckIn() {
        val draft = _uiState.value.draft
        if (draft == null || draft.selectedSeat.isNullOrBlank()) {
            _uiState.update { it.copy(statusMessage = "Please select a seat before completing check-in.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.completeCheckIn(draft)
            result.onSuccess { pass ->
                hasPendingSync = true
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        lookupResult = it.lookupResult?.copy(checkInStatus = "Checked-In"),
                        latestBoardingPass = pass,
                        statusMessage = "Check-in completed! Your boarding pass is ready."
                    )
                }
                postCheckInNotification(pass)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, statusMessage = "Check-in failed: ${error.message}") }
            }
        }
    }

    fun synchronize() {
        if (!_uiState.value.isOnline) {
            _uiState.update { it.copy(statusMessage = "Offline mode. Will sync when connected.") }
            hasPendingSync = true
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val result = repository.synchronizeWithServer()
            hasPendingSync = false
            result.onSuccess { message ->
                _uiState.update { it.copy(isSyncing = false, statusMessage = message) }
            }.onFailure { error ->
                _uiState.update { it.copy(isSyncing = false, statusMessage = error.message) }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun showMessage(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }

    fun resetForNewCheckIn() {
        _uiState.update {
            it.copy(
                lookupResult = null,
                draft = null,
                seatMap = emptyList(),
                latestBoardingPass = null,
                checkInStep = 0
            )
        }
    }

    suspend fun scanPassportWithOcr(context: Context, imageUri: Uri): PassportInfo {
        val image = InputImage.fromFilePath(context, imageUri)
        return scanPassportWithImage(image)
    }

    suspend fun scanPassportBitmapWithOcr(bitmap: Bitmap): PassportInfo {
        val image = InputImage.fromBitmap(bitmap, 0)
        return scanPassportWithImage(image)
    }

    private suspend fun scanPassportWithImage(image: InputImage): PassportInfo {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val result = recognizer.process(image).await()
        val fullText = result.text

        if (fullText.isBlank()) {
            throw IllegalArgumentException("No readable text detected. Please use a clearer image.")
        }

        // Extract passport number (MRZ pattern or alphanumeric 6-9 chars)
        val numberMatch = Regex("[A-Z0-9]{6,9}").find(fullText.uppercase())?.value ?: "UNKNOWN"

        // Extract name (longest line with mostly letters)
        val bestName = fullText.lines()
            .map { it.trim() }
            .filter { it.count { ch -> ch.isLetter() } >= 5 }
            .maxByOrNull { it.count { ch -> ch.isLetter() } }
            ?.uppercase()
            ?: "UNKNOWN PASSENGER"

        // Try extract nationality
        val nationalityCodes = listOf("DZA", "FRA", "USA", "GBR", "DEU", "MAR", "TUN", "EGY", "SAU", "ARE", "TUR")
        val nationality = nationalityCodes.firstOrNull { fullText.uppercase().contains(it) } ?: ""

        // Try extract dates (DD/MM/YYYY or YYMMDD)
        val datePattern = Regex("\\d{2}[/\\-.]\\d{2}[/\\-.]\\d{4}")
        val dates = datePattern.findAll(fullText).map { it.value }.toList()

        val passportInfo = PassportInfo(
            fullName = bestName,
            passportNumber = numberMatch,
            nationality = nationality,
            dateOfBirth = dates.getOrElse(0) { "" },
            expiryDate = dates.getOrElse(1) { "" },
            gender = if (fullText.uppercase().contains(" M ") || fullText.uppercase().contains("/M/")) "M" else if (fullText.uppercase().contains(" F ") || fullText.uppercase().contains("/F/")) "F" else "",
            rawText = fullText,
            verified = numberMatch != "UNKNOWN"
        )
        attachPassportInfo(passportInfo)
        return passportInfo
    }

    fun generateQrBitmap(content: String, size: Int = 800): Bitmap {
        val matrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        return matrix.toBitmap()
    }

    fun saveBoardingPassPdf(context: Context, boardingPass: BoardingPass): String {
        val pdf = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        // Background
        val bgPaint = android.graphics.Paint().apply { color = android.graphics.Color.WHITE }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        // Header bar
        val headerPaint = android.graphics.Paint().apply { color = android.graphics.Color.parseColor("#0770E3") }
        canvas.drawRect(0f, 0f, 595f, 80f, headerPaint)

        val headerTextPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 24f
            isFakeBoldText = true
        }
        canvas.drawText("SkyPass - Boarding Pass", 30f, 50f, headerTextPaint)

        // Flight info
        val titlePaint = android.graphics.Paint().apply {
            textSize = 14f
            color = android.graphics.Color.parseColor("#868E96")
        }
        val valuePaint = android.graphics.Paint().apply {
            textSize = 18f
            color = android.graphics.Color.parseColor("#212529")
            isFakeBoldText = true
        }
        val largePaint = android.graphics.Paint().apply {
            textSize = 36f
            color = android.graphics.Color.parseColor("#212529")
            isFakeBoldText = true
        }

        var y = 120f

        // Route
        canvas.drawText("FROM", 30f, y, titlePaint)
        canvas.drawText("TO", 350f, y, titlePaint)
        y += 35f
        canvas.drawText(boardingPass.origin, 30f, y, largePaint)
        canvas.drawText(boardingPass.destination, 350f, y, largePaint)
        y += 20f
        canvas.drawText(boardingPass.originCity, 30f, y, titlePaint)
        canvas.drawText(boardingPass.destinationCity, 350f, y, titlePaint)

        y += 50f
        val fields = listOf(
            "PASSENGER" to boardingPass.passengerName,
            "FLIGHT" to boardingPass.flightNumber,
            "BOOKING REF" to boardingPass.bookingReference,
            "SEAT" to boardingPass.seat,
            "CLASS" to boardingPass.seatClass,
            "GATE" to boardingPass.gate,
            "TERMINAL" to boardingPass.terminal,
            "DEPARTURE" to boardingPass.departureTime,
            "ARRIVAL" to boardingPass.arrivalTime,
            "BOARDING GROUP" to boardingPass.boardingGroup,
            "SEQUENCE" to boardingPass.sequence,
            "BAGGAGE" to boardingPass.baggageInfo,
            "STATUS" to boardingPass.status,
            "ISSUED" to boardingPass.issuedAt.take(19)
        )

        fields.forEach { (label, value) ->
            canvas.drawText(label, 30f, y, titlePaint)
            y += 22f
            canvas.drawText(value, 30f, y, valuePaint)
            y += 32f
        }

        // QR Code
        val qrBitmap = generateQrBitmap(boardingPass.qrPayload, 200)
        canvas.drawBitmap(qrBitmap, 370f, 200f, null)

        pdf.finishPage(page)

        val fileName = "SkyPass-${boardingPass.bookingReference}-${boardingPass.flightNumber}.pdf"
        val values = android.content.ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Unable to create PDF file")

        resolver.openOutputStream(uri)?.use { output ->
            pdf.writeTo(output)
        } ?: throw IOException("Unable to open output stream")

        pdf.close()
        return fileName
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(CHANNEL_ID, "Check-In Updates", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Notifications for check-in completion and boarding pass"
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun postCheckInNotification(boardingPass: BoardingPass) {
        val app = getApplication<Application>()
        val notification = NotificationCompat.Builder(app, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Check-In Complete ✓")
            .setContentText("${boardingPass.flightNumber} • Seat ${boardingPass.seat} • Gate ${boardingPass.gate}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your boarding pass for ${boardingPass.flightNumber} (${boardingPass.origin} → ${boardingPass.destination}) is ready. Seat: ${boardingPass.seat}, Gate: ${boardingPass.gate}, Departure: ${boardingPass.departureTime}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        runCatching {
            NotificationManagerCompat.from(app).notify(boardingPass.id.hashCode(), notification)
        }
    }

    private companion object {
        const val CHANNEL_ID = "checkin_updates"
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
}

private fun BitMatrix.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (get(x, y)) android.graphics.Color.parseColor("#212529") else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}
