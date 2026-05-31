package com.example.myapplication.network

import com.example.myapplication.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("health")
    suspend fun healthCheck(): Response<Map<String, String>>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/google")
    suspend fun googleSignIn(@Body request: GoogleAuthRequest): Response<AuthResponse>

    /** Restores the current session from the Authorization Bearer header. */
    @GET("auth/me")
    suspend fun whoami(): Response<MeResponse>

    @GET("flights/lookup")
    suspend fun lookupFlight(
        @Query("bookingRef") bookingRef: String,
        @Query("lastName") lastName: String
    ): Response<FlightLookupResponse>

    @GET("flights/{flightId}/seats")
    suspend fun getSeatMap(@Path("flightId") flightId: String): Response<SeatMapResponse>

    @POST("checkin/complete")
    suspend fun completeCheckIn(@Body request: CheckInRequest): Response<CheckInResponse>

    @GET("checkin/{passId}")
    suspend fun getBoardingPass(@Path("passId") passId: String): Response<BoardingPassResponse>

    @GET("user/{userId}")
    suspend fun getUser(@Path("userId") userId: String): Response<UserResponse>

    @PATCH("user/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: String,
        @Body request: UpdateProfileRequest
    ): Response<UserResponse>

    @GET("user/{userId}/boardingpasses")
    suspend fun getUserBoardingPasses(@Path("userId") userId: String): Response<List<BoardingPass>>

    @GET("user/{userId}/flights")
    suspend fun getUserFlights(@Path("userId") userId: String): Response<List<FlightItinerary>>

    @POST("sync")
    suspend fun syncData(@Body request: SyncRequest): Response<SyncResponse>
}

// ── Auth ──
data class RegisterRequest(val fullName: String, val email: String, val phone: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class GoogleAuthRequest(val idToken: String, val email: String, val displayName: String)
data class AuthResponse(val success: Boolean, val user: UserAccount? = null, val token: String = "", val message: String = "")
data class MeResponse(val success: Boolean, val user: UserAccount? = null, val message: String = "")
data class UserResponse(val success: Boolean, val user: UserAccount? = null, val message: String = "")
data class UpdateProfileRequest(val fullName: String? = null, val phone: String? = null, val avatarUrl: String? = null)

// ── Flights ──
data class FlightLookupResponse(val success: Boolean, val flight: FlightItinerary? = null, val message: String = "")
data class SeatMapResponse(val success: Boolean, val seats: List<Seat> = emptyList(), val message: String = "")

// ── Check-in ──
data class CheckInRequest(
    val userId: String,
    val bookingReference: String,
    val passengerName: String,
    val passportInfo: PassportInfo,
    val selectedSeat: String,
    val baggage: BaggageDeclaration,
    val specialRequests: SpecialRequests
)
data class CheckInResponse(val success: Boolean, val boardingPass: BoardingPass? = null, val message: String = "")
data class BoardingPassResponse(val success: Boolean, val boardingPass: BoardingPass? = null, val message: String = "")

// ── Sync ──
data class SyncRequest(val userId: String, val lastSyncTimestamp: String = "")
data class SyncResponse(
    val success: Boolean,
    val boardingPasses: List<BoardingPass> = emptyList(),
    val flights: List<FlightItinerary> = emptyList(),
    val timestamp: String = "",
    val message: String = ""
)
