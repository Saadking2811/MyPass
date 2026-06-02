package com.example.myapplication.repository

import android.content.Context
import com.example.myapplication.model.BoardingPass
import com.example.myapplication.model.CheckInDraft
import com.example.myapplication.model.FlightItinerary
import com.example.myapplication.model.Seat
import com.example.myapplication.model.UserAccount
import com.example.myapplication.network.SyncResponse

/**
 * Backend-only facade that composes the four domain repositories and exposes
 * a single entry point to the ViewModel. The server (PostgreSQL via Fastify)
 * is the single source of truth; every method round-trips through the API.
 */
class AppRepository(@Suppress("unused") private val context: Context) {

    private val auth          = AuthRepository()
    private val flights       = FlightRepository()
    private val boardingPasses = BoardingPassRepository()
    private val sync          = SyncRepository()

    // Auth
    suspend fun register(fullName: String, email: String, phone: String, password: String): Result<UserAccount> =
        auth.register(fullName, email, phone, password)

    suspend fun login(email: String, password: String): Result<UserAccount> =
        auth.login(email, password)

    suspend fun googleSignIn(displayName: String, email: String): Result<UserAccount> =
        auth.googleSignIn(displayName, email)

    suspend fun restoreSession(): Result<UserAccount?> = auth.restoreSession()

    suspend fun updateProfile(userId: String, fullName: String? = null, phone: String? = null): Result<UserAccount> =
        auth.updateProfile(userId, fullName, phone)

    fun logout() = auth.logout()

    // Flights
    suspend fun findFlight(bookingReference: String, lastName: String): Result<FlightItinerary> =
        flights.findFlight(bookingReference, lastName)

    suspend fun getSeatMap(flightId: String): Result<List<Seat>> = flights.getSeatMap(flightId)

    suspend fun getUserFlights(userId: String): Result<List<FlightItinerary>> = flights.getUserFlights(userId)

    // Boarding passes & check-in
    suspend fun getUserBoardingPasses(userId: String): Result<List<BoardingPass>> =
        boardingPasses.getUserBoardingPasses(userId)

    suspend fun completeCheckIn(userId: String, draft: CheckInDraft): Result<BoardingPass> =
        boardingPasses.completeCheckIn(userId, draft)

    // Sync
    suspend fun synchronize(userId: String): Result<SyncResponse> = sync.synchronize(userId)
}
