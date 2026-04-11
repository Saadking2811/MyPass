package com.example.myapplication.network

import com.example.myapplication.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/google")
    suspend fun googleSignIn(@Body request: GoogleAuthRequest): Response<AuthResponse>

    @GET("flights/lookup")
    suspend fun lookupFlight(
        @Query("bookingRef") bookingRef: String,
        @Query("lastName") lastName: String
    ): Response<FlightLookupResponse>

    @GET("flights/{flightId}/seats")
    suspend fun getSeatMap(@Path("flightId") flightId: String): Response<SeatMapResponse>

    @POST("checkin/complete")
    suspend fun completeCheckIn(@Body request: CheckInRequest): Response<CheckInResponse>

    @GET("boardingpass/{passId}")
    suspend fun getBoardingPass(@Path("passId") passId: String): Response<BoardingPass>

    @GET("user/{userId}/boardingpasses")
    suspend fun getUserBoardingPasses(@Path("userId") userId: String): Response<List<BoardingPass>>

    @POST("sync")
    suspend fun syncData(@Body request: SyncRequest): Response<SyncResponse>
}

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class GoogleAuthRequest(
    val idToken: String,
    val email: String,
    val displayName: String
)

data class AuthResponse(
    val success: Boolean,
    val user: UserAccount? = null,
    val token: String = "",
    val message: String = ""
)

data class FlightLookupResponse(
    val success: Boolean,
    val flight: FlightItinerary? = null,
    val message: String = ""
)

data class SeatMapResponse(
    val success: Boolean,
    val seats: List<Seat> = emptyList(),
    val message: String = ""
)

data class CheckInRequest(
    val bookingReference: String,
    val passengerName: String,
    val passportInfo: PassportInfo,
    val selectedSeat: String,
    val baggage: BaggageDeclaration,
    val specialRequests: SpecialRequests
)

data class CheckInResponse(
    val success: Boolean,
    val boardingPass: BoardingPass? = null,
    val message: String = ""
)

data class SyncRequest(
    val userId: String,
    val lastSyncTimestamp: String
)

data class SyncResponse(
    val success: Boolean,
    val boardingPasses: List<BoardingPass> = emptyList(),
    val flights: List<FlightItinerary> = emptyList(),
    val timestamp: String = ""
)
