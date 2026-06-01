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
    val isRestoringSession: Boolean = true,  // true on app start until /auth/me resolves
    val statusMessage: String? = null,
    val passportRawText: String = "",
    val checkInStep: Int = 0,
    val isAuthMode: Boolean = true  // true = login, false = register
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application.applicationContext)
    private val networkMonitor = NetworkMonitor(application.applicationContext)

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        ensureNotificationChannel()

        // 1. Watch network connectivity.
        viewModelScope.launch {
            networkMonitor.isOnline.collectLatest { online ->
                _uiState.update { it.copy(isOnline = online) }
            }
        }

        // 2. Restore session on cold start (calls /auth/me with stored Bearer token).
        viewModelScope.launch {
            val restored = repository.restoreSession().getOrNull()
            if (restored != null) {
                _uiState.update { it.copy(currentUser = restored, isRestoringSession = false) }
                refreshUserData(restored.id)
            } else {
                _uiState.update { it.copy(isRestoringSession = false) }
            }
        }
    }

    /** Pulls fresh boarding passes + flights from the backend. */
    private fun refreshUserData(userId: String) {
        viewModelScope.launch {
            val passes = repository.getUserBoardingPasses(userId).getOrNull().orEmpty()
            val flights = repository.getUserFlights(userId).getOrNull().orEmpty()
            _uiState.update {
                it.copy(
                    cachedBoardingPasses = passes,
                    cachedFlights = flights
                )
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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = user,
                        statusMessage = "Welcome aboard, ${user.fullName.split(" ").first()}!"
                    )
                }
                refreshUserData(user.id)
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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = user,
                        statusMessage = "Welcome back, ${user.fullName.split(" ").first()}!"
                    )
                }
                refreshUserData(user.id)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, statusMessage = error.message) }
            }
        }
    }

    fun signInWithGoogle(email: String = "", displayName: String = "") {
        val finalEmail = email.trim().ifBlank { "guest@mypass.dz" }
        val finalName = displayName.trim().ifBlank { "Guest Traveler" }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.googleSignIn(finalName, finalEmail)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = user,
                        statusMessage = "Signed in as ${user.fullName.split(" ").first()}."
                    )
                }
                refreshUserData(user.id)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, statusMessage = error.message) }
            }
        }
    }

    fun logout() {
        repository.logout()
        _uiState.update { AppUiState(isRestoringSession = false) }
    }

    /** Update name / phone on the server. */
    fun updateProfile(fullName: String, phone: String) {
        val userId = _uiState.value.currentUser?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.updateProfile(userId, fullName, phone)
            result.onSuccess { user ->
                _uiState.update { it.copy(isLoading = false, currentUser = user, statusMessage = "Profile updated.") }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, statusMessage = error.message) }
            }
        }
    }

    fun lookupFlight(bookingReference: String, lastName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = null) }
            val flightResult = repository.findFlight(bookingReference, lastName)
            flightResult.onSuccess { itinerary ->
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
            val result = repository.completeCheckIn(userId, draft)
            result.onSuccess { pass ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        lookupResult = it.lookupResult?.copy(checkInStatus = "Checked-In"),
                        latestBoardingPass = pass,
                        statusMessage = "Check-in completed! Your boarding pass is ready."
                    )
                }
                postCheckInNotification(pass)
                refreshUserData(userId)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, statusMessage = "Check-in failed: ${error.message}") }
            }
        }
    }

    fun synchronize() {
        val userId = _uiState.value.currentUser?.id ?: return
        if (!_uiState.value.isOnline) {
            _uiState.update { it.copy(statusMessage = "Offline. Connect to the internet to sync.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val result = repository.synchronize(userId)
            result.onSuccess { resp ->
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        cachedBoardingPasses = resp.boardingPasses,
                        cachedFlights = resp.flights,
                        statusMessage = "Synced ${resp.boardingPasses.size} passes, ${resp.flights.size} flights."
                    )
                }
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

    // Passport scanning is now handled via CameraX + ICAO 9303 MRZ parser in the UI layer

    fun generateQrBitmap(content: String, size: Int = 800): Bitmap {
        val matrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        return matrix.toBitmap()
    }

    /**
     * Save the boarding pass as a refined modern PDF.
     * Returns Pair(fileName, contentUri) so the caller can immediately share/view it.
     */
    fun saveBoardingPassPdfWithUri(context: Context, boardingPass: BoardingPass): Pair<String, android.net.Uri> {
        val pdf = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        // Refined palette — matches the in-app boarding pass
        val emerald   = android.graphics.Color.parseColor("#004D2F")
        val emeraldDark = android.graphics.Color.parseColor("#003520")
        val white     = android.graphics.Color.WHITE
        val charcoal  = android.graphics.Color.parseColor("#0F172A")
        val gray      = android.graphics.Color.parseColor("#475569")
        val grayMuted = android.graphics.Color.parseColor("#94A3B8")
        val divider   = android.graphics.Color.parseColor("#E2E8F0")
        val cream     = android.graphics.Color.parseColor("#F8F8F6")

        // Page background
        canvas.drawColor(white)

        // ── Header — solid deep emerald, no gradient ──
        val headerH = 110f
        canvas.drawRect(0f, 0f, 595f, headerH, android.graphics.Paint().apply { color = emerald })

        // Brand mark (rounded white square with airplane glyph)
        val markX = 32f
        val markY = 32f
        val markSize = 46f
        canvas.drawRoundRect(
            android.graphics.RectF(markX, markY, markX + markSize, markY + markSize),
            9f, 9f,
            android.graphics.Paint().apply { color = white; isAntiAlias = true }
        )
        // Airplane glyph inside mark
        val planePaint = android.graphics.Paint().apply { color = emerald; isAntiAlias = true }
        val planePath = android.graphics.Path()
        val pcx = markX + markSize / 2
        val pcy = markY + markSize / 2
        // simple paper-plane shape at -30°
        val matrix = android.graphics.Matrix().apply { setRotate(-30f, pcx, pcy) }
        // fuselage
        planePath.addRect(pcx - 1.5f, pcy - 11f, pcx + 1.5f, pcy + 11f, android.graphics.Path.Direction.CW)
        // wings (swept)
        planePath.moveTo(pcx - 2f, pcy - 2f); planePath.lineTo(pcx - 13f, pcy + 5f)
        planePath.lineTo(pcx - 13f, pcy + 7f); planePath.lineTo(pcx + 13f, pcy + 7f)
        planePath.lineTo(pcx + 13f, pcy + 5f); planePath.lineTo(pcx + 2f, pcy - 2f)
        planePath.close()
        // tail
        planePath.moveTo(pcx - 5f, pcy + 7f); planePath.lineTo(pcx - 5f, pcy + 9f)
        planePath.lineTo(pcx + 5f, pcy + 9f); planePath.lineTo(pcx + 5f, pcy + 7f); planePath.close()
        planePath.transform(matrix)
        canvas.drawPath(planePath, planePaint)

        // Brand text
        canvas.drawText("MyPass",
            markX + markSize + 14f, markY + 24f,
            android.graphics.Paint().apply { color = white; textSize = 22f; isFakeBoldText = true; isAntiAlias = true }
        )
        canvas.drawText("BOARDING PASS",
            markX + markSize + 14f, markY + 42f,
            android.graphics.Paint().apply {
                color = android.graphics.Color.argb(220, 255, 255, 255)
                textSize = 9f; isAntiAlias = true; letterSpacing = 0.20f; isFakeBoldText = true
            }
        )

        // Right side header — flight number
        val rightPaint = android.graphics.Paint().apply {
            color = white; textSize = 11f; isAntiAlias = true; letterSpacing = 0.18f
            textAlign = android.graphics.Paint.Align.RIGHT
        }
        canvas.drawText("FLIGHT", 565f, markY + 8f, rightPaint)
        canvas.drawText(boardingPass.flightNumber, 565f,  markY + 36f,
            android.graphics.Paint().apply {
                color = white; textSize = 26f; isFakeBoldText = true; isAntiAlias = true
                textAlign = android.graphics.Paint.Align.RIGHT
            }
        )

        // ── Route section ──
        val labelPaint = android.graphics.Paint().apply {
            color = gray; textSize = 9f; isAntiAlias = true; letterSpacing = 0.18f; isFakeBoldText = true
        }
        val cityPaint = android.graphics.Paint().apply {
            color = gray; textSize = 11f; isAntiAlias = true; letterSpacing = 0.10f
        }
        val timePaint = android.graphics.Paint().apply {
            color = charcoal; textSize = 16f; isFakeBoldText = true; isAntiAlias = true
        }
        val codePaint = android.graphics.Paint().apply {
            color = emerald; textSize = 56f; isFakeBoldText = true; isAntiAlias = true
        }

        var y = headerH + 50f
        canvas.drawText("FROM", 32f, y, labelPaint)
        canvas.drawText("TO", 470f, y, labelPaint)
        y += 50f
        canvas.drawText(boardingPass.origin,      32f, y, codePaint)
        canvas.drawText(boardingPass.destination, 470f, y, codePaint)
        y += 18f
        canvas.drawText(boardingPass.originCity.uppercase(),      32f, y, cityPaint)
        canvas.drawText(boardingPass.destinationCity.uppercase(), 470f, y, cityPaint)
        y += 20f
        canvas.drawText(boardingPass.departureTime.takeLast(5), 32f, y, timePaint)
        canvas.drawText(boardingPass.arrivalTime.takeLast(5), 470f, y, timePaint)

        // Center: airplane icon + DIRECT line
        val centerX = 297.5f
        val arrowY = headerH + 100f
        canvas.drawText("✈", centerX - 6f, arrowY - 4f,
            android.graphics.Paint().apply {
                color = emerald; textSize = 22f; isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
        )
        // Hairline dashed line under
        val dashPaint = android.graphics.Paint().apply {
            color = divider
            strokeWidth = 1f
            style = android.graphics.Paint.Style.STROKE
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(3.5f, 3.5f), 0f)
            isAntiAlias = true
        }
        canvas.drawLine(180f, arrowY + 8f, 270f, arrowY + 8f, dashPaint)
        canvas.drawLine(324f, arrowY + 8f, 414f, arrowY + 8f, dashPaint)
        canvas.drawText("DIRECT", centerX, arrowY + 22f,
            android.graphics.Paint().apply {
                color = gray; textSize = 9f; isFakeBoldText = true; isAntiAlias = true
                letterSpacing = 0.20f
                textAlign = android.graphics.Paint.Align.CENTER
            }
        )

        // ── Section divider ──
        y += 36f
        canvas.drawLine(32f, y, 563f, y,
            android.graphics.Paint().apply { color = divider; strokeWidth = 0.6f }
        )

        // ── Passenger block ──
        y += 36f
        canvas.drawText("PASSENGER", 32f, y, labelPaint)
        y += 22f
        canvas.drawText(boardingPass.passengerName.uppercase(),
            32f, y,
            android.graphics.Paint().apply { color = emerald; textSize = 22f; isFakeBoldText = true; isAntiAlias = true }
        )

        // ── 3x3 info grid (SEAT, GATE, GROUP / DEPART, ARRIVAL, TERMINAL / CLASS, SEQ, BAGGAGE) ──
        y += 40f
        val colWidth = (563f - 32f) / 3f
        val gridLabelPaint = labelPaint
        val gridValuePaint = android.graphics.Paint().apply {
            color = emerald; textSize = 22f; isFakeBoldText = true; isAntiAlias = true
        }
        val rows = listOf(
            listOf("SEAT" to boardingPass.seat,
                   "GATE" to boardingPass.gate,
                   "GROUP" to boardingPass.boardingGroup),
            listOf("DEPART" to boardingPass.departureTime.takeLast(5),
                   "ARRIVAL" to boardingPass.arrivalTime.takeLast(5),
                   "TERMINAL" to boardingPass.terminal.ifBlank { "—" }),
            listOf("CLASS" to boardingPass.seatClass,
                   "SEQUENCE" to boardingPass.sequence,
                   "BAGGAGE" to boardingPass.baggageInfo.take(12).ifBlank { "—" })
        )
        rows.forEach { row ->
            row.forEachIndexed { idx, (label, value) ->
                val cx = 32f + idx * colWidth
                canvas.drawText(label, cx, y, gridLabelPaint)
                canvas.drawText(value, cx, y + 26f, gridValuePaint)
            }
            // hairline between rows
            canvas.drawLine(32f, y + 38f, 563f, y + 38f,
                android.graphics.Paint().apply { color = divider; strokeWidth = 0.4f })
            y += 56f
        }

        // ── Perforated divider before QR section ──
        y += 4f
        canvas.drawLine(32f, y, 563f, y, dashPaint)

        // ── QR code section ──
        y += 24f
        val qrSize = 170f
        val qrX = (595f - qrSize) / 2f
        // Cream background card behind QR
        canvas.drawRoundRect(
            android.graphics.RectF(qrX - 18f, y - 18f, qrX + qrSize + 18f, y + qrSize + 42f),
            10f, 10f,
            android.graphics.Paint().apply { color = cream }
        )
        val qrBitmap = generateQrBitmap(boardingPass.qrPayload, qrSize.toInt())
        canvas.drawBitmap(qrBitmap, qrX, y, null)
        // Scan label
        canvas.drawText("SCAN AT GATE",
            centerX, y + qrSize + 26f,
            android.graphics.Paint().apply {
                color = gray; textSize = 10f; isFakeBoldText = true; isAntiAlias = true
                letterSpacing = 0.30f
                textAlign = android.graphics.Paint.Align.CENTER
            }
        )

        // ── Footer ──
        val footerY = 800f
        canvas.drawLine(32f, footerY, 563f, footerY,
            android.graphics.Paint().apply { color = divider; strokeWidth = 0.5f })
        // Status pill
        val statusText = "STATUS · ${boardingPass.status.uppercase()}"
        val statusPaint = android.graphics.Paint().apply {
            color = emerald; textSize = 10f; isFakeBoldText = true; isAntiAlias = true
            letterSpacing = 0.20f
        }
        canvas.drawText(statusText, 32f, footerY + 22f, statusPaint)
        canvas.drawText("BOOKING · ${boardingPass.bookingReference}",
            563f, footerY + 22f,
            android.graphics.Paint().apply {
                color = grayMuted; textSize = 10f; isAntiAlias = true; letterSpacing = 0.20f
                textAlign = android.graphics.Paint.Align.RIGHT
            }
        )
        canvas.drawText("MyPass · ${boardingPass.airlineName} · Issued ${boardingPass.issuedAt.take(10)}",
            centerX, footerY + 22f,
            android.graphics.Paint().apply {
                color = grayMuted; textSize = 9f; isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
        )

        // Brand bottom strip
        canvas.drawRect(0f, 838f, 595f, 842f,
            android.graphics.Paint().apply { color = emeraldDark })

        pdf.finishPage(page)

        val fileName = "MyPass-${boardingPass.bookingReference}-${boardingPass.flightNumber.replace(" ", "")}.pdf"
        val values = android.content.ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Unable to create PDF file")
        resolver.openOutputStream(uri)?.use { output -> pdf.writeTo(output) }
            ?: throw IOException("Unable to open output stream")
        pdf.close()
        return fileName to uri
    }

    /** Backwards-compatible wrapper returning just the file name. */
    fun saveBoardingPassPdf(context: Context, boardingPass: BoardingPass): String =
        saveBoardingPassPdfWithUri(context, boardingPass).first

    /** Save the PDF then open the Share sheet so the user can save to Drive / Wallet / Files / email. */
    fun shareBoardingPass(context: Context, boardingPass: BoardingPass) {
        val (_, uri) = saveBoardingPassPdfWithUri(context, boardingPass)
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "MyPass boarding pass · ${boardingPass.flightNumber}")
            putExtra(android.content.Intent.EXTRA_TEXT,
                "Boarding pass for ${boardingPass.passengerName}: " +
                "${boardingPass.flightNumber} (${boardingPass.origin}→${boardingPass.destination}), " +
                "Seat ${boardingPass.seat}, Gate ${boardingPass.gate}.")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = android.content.Intent.createChooser(intent, "Add boarding pass to…")
            .apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(chooser)
    }

    /** Open the saved PDF directly in the default viewer (no share sheet). */
    fun openBoardingPassPdf(context: Context, boardingPass: BoardingPass) {
        val (_, uri) = saveBoardingPassPdfWithUri(context, boardingPass)
        val view = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            )
        }
        runCatching { context.startActivity(view) }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(CHANNEL_ID, "Boarding pass updates", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Confirms when your boarding pass is ready and reminds you about boarding"
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
                lightColor = android.graphics.Color.parseColor("#004D2F")
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun postCheckInNotification(boardingPass: BoardingPass) {
        val app = getApplication<Application>()

        // Tap → re-open the app on the boarding pass screen (via launcher intent)
        val launchIntent = app.packageManager
            .getLaunchIntentForPackage(app.packageName)
            ?.apply { setPackage(null); flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK }
        val pendingFlags = android.app.PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) android.app.PendingIntent.FLAG_IMMUTABLE else 0
        val contentPI = launchIntent?.let {
            android.app.PendingIntent.getActivity(app, boardingPass.id.hashCode(), it, pendingFlags)
        }

        val depTime = boardingPass.departureTime.takeLast(5)
        val bigText = """
            ✈ ${boardingPass.origin} → ${boardingPass.destination}
            Flight ${boardingPass.flightNumber} · ${boardingPass.airlineName}
            Seat ${boardingPass.seat} · Gate ${boardingPass.gate} · Boarding ${boardingPass.boardingGroup}
            Departure $depTime · Terminal ${boardingPass.terminal}
        """.trimIndent()

        val notification = NotificationCompat.Builder(app, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(android.graphics.Color.parseColor("#004D2F"))
            .setColorized(true)
            .setContentTitle("Boarding pass ready")
            .setContentText("${boardingPass.flightNumber} · ${boardingPass.origin}→${boardingPass.destination} · Seat ${boardingPass.seat}")
            .setSubText(boardingPass.airlineName)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("Boarding pass ready · ${boardingPass.flightNumber}")
                    .bigText(bigText)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(contentPI)
            .build()

        runCatching {
            val notifManager = NotificationManagerCompat.from(app)
            if (notifManager.areNotificationsEnabled()) {
                notifManager.notify(boardingPass.id.hashCode(), notification)
            }
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
