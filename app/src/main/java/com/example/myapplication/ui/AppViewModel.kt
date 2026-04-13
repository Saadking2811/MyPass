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

    // Passport scanning is now handled via CameraX + ICAO 9303 MRZ parser in the UI layer

    fun generateQrBitmap(content: String, size: Int = 800): Bitmap {
        val matrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        return matrix.toBitmap()
    }

    fun saveBoardingPassPdf(context: Context, boardingPass: BoardingPass): String {
        val pdf = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val green = android.graphics.Color.parseColor("#006233")
        val freshGreen = android.graphics.Color.parseColor("#00A651")
        val red = android.graphics.Color.parseColor("#D21034")
        val white = android.graphics.Color.WHITE
        val darkText = android.graphics.Color.parseColor("#212529")
        val grayText = android.graphics.Color.parseColor("#868E96")
        val lightBg = android.graphics.Color.parseColor("#F5F7F9")

        // Background
        canvas.drawColor(white)

        // ── Header bar with gradient effect ──
        val headerPaint = android.graphics.Paint()
        val headerGradient = android.graphics.LinearGradient(
            0f, 0f, 595f, 0f,
            green, freshGreen,
            android.graphics.Shader.TileMode.CLAMP
        )
        headerPaint.shader = headerGradient
        canvas.drawRect(0f, 0f, 595f, 100f, headerPaint)

        // Red accent strip at bottom of header
        val redPaint = android.graphics.Paint().apply { color = red }
        canvas.drawRect(0f, 100f, 595f, 104f, redPaint)

        // Crescent + Star logo in header
        val logoPaint = android.graphics.Paint().apply { color = white; isAntiAlias = true }
        // Crescent: draw two circles
        canvas.drawCircle(45f, 50f, 22f, logoPaint)
        val erasePaint = android.graphics.Paint().apply { color = green; isAntiAlias = true }
        canvas.drawCircle(52f, 47f, 17f, erasePaint)
        // Star
        val starPaint = android.graphics.Paint().apply { color = white; isAntiAlias = true; style = android.graphics.Paint.Style.FILL }
        val starPath = android.graphics.Path()
        val sCx = 62f; val sCy = 48f; val outer = 9f; val inner = 4f
        for (i in 0 until 5) {
            val outerAngle = Math.toRadians((i * 72 - 90).toDouble())
            val innerAngle = Math.toRadians((i * 72 + 36 - 90).toDouble())
            val ox = sCx + outer * kotlin.math.cos(outerAngle).toFloat()
            val oy = sCy + outer * kotlin.math.sin(outerAngle).toFloat()
            val ix = sCx + inner * kotlin.math.cos(innerAngle).toFloat()
            val iy = sCy + inner * kotlin.math.sin(innerAngle).toFloat()
            if (i == 0) starPath.moveTo(ox, oy) else starPath.lineTo(ox, oy)
            starPath.lineTo(ix, iy)
        }
        starPath.close()
        canvas.drawPath(starPath, starPaint)

        // Header text
        val headerTextPaint = android.graphics.Paint().apply {
            color = white; textSize = 28f; isFakeBoldText = true; isAntiAlias = true
        }
        canvas.drawText("MyPass", 85f, 48f, headerTextPaint)
        val subHeaderPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(180, 255, 255, 255); textSize = 13f; isAntiAlias = true
        }
        canvas.drawText("BOARDING PASS  •  ${boardingPass.airlineName}", 85f, 70f, subHeaderPaint)

        // Flight number badge on right
        val flightBadgePaint = android.graphics.Paint().apply {
            color = white; textSize = 24f; isFakeBoldText = true; isAntiAlias = true; textAlign = android.graphics.Paint.Align.RIGHT
        }
        canvas.drawText(boardingPass.flightNumber, 565f, 58f, flightBadgePaint)

        // ── Route section ──
        val titlePaint = android.graphics.Paint().apply { textSize = 12f; color = grayText; isAntiAlias = true; letterSpacing = 0.08f }
        val valuePaint = android.graphics.Paint().apply { textSize = 18f; color = darkText; isFakeBoldText = true; isAntiAlias = true }
        val largePaint = android.graphics.Paint().apply { textSize = 42f; color = green; isFakeBoldText = true; isAntiAlias = true }
        val cityPaint = android.graphics.Paint().apply { textSize = 13f; color = grayText; isAntiAlias = true }

        var y = 140f
        canvas.drawText("FROM", 30f, y, titlePaint)
        canvas.drawText("TO", 400f, y, titlePaint)
        y += 40f
        canvas.drawText(boardingPass.origin, 30f, y, largePaint)
        canvas.drawText(boardingPass.destination, 400f, y, largePaint)
        y += 18f
        canvas.drawText(boardingPass.originCity, 30f, y, cityPaint)
        canvas.drawText(boardingPass.destinationCity, 400f, y, cityPaint)

        // Airplane icon between codes
        val planePaint = android.graphics.Paint().apply { color = red; textSize = 20f; isAntiAlias = true; textAlign = android.graphics.Paint.Align.CENTER }
        canvas.drawText("✈", 297f, y - 22f, planePaint)

        // ── Dotted divider ──
        y += 30f
        val dashPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#DEE2E6")
            strokeWidth = 1.5f
            style = android.graphics.Paint.Style.STROKE
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(8f, 5f), 0f)
            isAntiAlias = true
        }
        canvas.drawLine(30f, y, 565f, y, dashPaint)

        // ── Two-column info grid ──
        y += 30f
        val leftFields = listOf(
            "PASSENGER" to boardingPass.passengerName,
            "SEAT" to boardingPass.seat,
            "BOARDING GROUP" to boardingPass.boardingGroup,
            "DEPARTURE" to boardingPass.departureTime,
            "BAGGAGE" to boardingPass.baggageInfo,
            "ISSUED" to boardingPass.issuedAt.take(16)
        )
        val rightFields = listOf(
            "BOOKING REF" to boardingPass.bookingReference,
            "CLASS" to boardingPass.seatClass,
            "SEQUENCE" to boardingPass.sequence,
            "ARRIVAL" to boardingPass.arrivalTime,
            "GATE" to boardingPass.gate,
            "TERMINAL" to boardingPass.terminal
        )

        leftFields.forEachIndexed { i, (label, value) ->
            val fy = y + i * 52f
            canvas.drawText(label, 30f, fy, titlePaint)
            canvas.drawText(value, 30f, fy + 20f, valuePaint)
        }
        rightFields.forEachIndexed { i, (label, value) ->
            val fy = y + i * 52f
            canvas.drawText(label, 330f, fy, titlePaint)
            canvas.drawText(value, 330f, fy + 20f, valuePaint)
        }

        // ── Dotted divider before QR ──
        val qrDivY = y + leftFields.size * 52f + 10f
        canvas.drawLine(30f, qrDivY, 565f, qrDivY, dashPaint)

        // ── QR Code section ──
        val qrBitmap = generateQrBitmap(boardingPass.qrPayload, 180)
        val qrY = qrDivY + 20f
        // Light background behind QR
        val qrBgPaint = android.graphics.Paint().apply { color = lightBg; isAntiAlias = true }
        canvas.drawRoundRect(android.graphics.RectF(30f, qrY, 565f, qrY + 200f), 12f, 12f, qrBgPaint)
        canvas.drawBitmap(qrBitmap, 207f, qrY + 10f, null)

        // QR label
        val qrLabelPaint = android.graphics.Paint().apply {
            textSize = 11f; color = grayText; isAntiAlias = true; textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText("Scan at gate for boarding", 297f, qrY + 195f, qrLabelPaint)

        // ── Footer ──
        val footerY = 820f
        val footerPaint = android.graphics.Paint().apply { textSize = 10f; color = grayText; isAntiAlias = true; textAlign = android.graphics.Paint.Align.CENTER }
        canvas.drawText("MyPass™ — Your Digital Passport to the World  |  Status: ${boardingPass.status}", 297f, footerY, footerPaint)

        // Green bottom bar
        canvas.drawRect(0f, 832f, 595f, 842f, android.graphics.Paint().apply { color = green })

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
