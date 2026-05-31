package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.model.*
import com.example.myapplication.network.*

/**
 * Backend-only repository. No local SQLite/DataStore caching of business data.
 * The server (PostgreSQL via Fastify) is the single source of truth.
 *
 * The app requires an internet connection. Errors are surfaced clearly so the
 * UI can show "Cannot reach server" instead of silently using stale data.
 */
class AppRepository(@Suppress("unused") private val context: Context) {

    private val api get() = RetrofitClient.apiService

    // ─────────────── AUTH ───────────────

    suspend fun register(fullName: String, email: String, phone: String, password: String): Result<UserAccount> {
        return runCatching {
            val response = api.register(RegisterRequest(fullName, email, phone, password))
            val body = response.body()
            if (!response.isSuccessful || body?.success != true || body.user == null) {
                throw Exception(body?.message ?: "Registration failed (HTTP ${response.code()})")
            }
            RetrofitClient.setAuthToken(body.token)
            RetrofitClient.setUserId(body.user.id)
            body.user
        }.recoverCatching { e ->
            throw Exception("Cannot reach backend at ${RetrofitClient.getBaseUrl()}. ${e.message ?: ""}".trim())
        }
    }

    suspend fun login(email: String, password: String): Result<UserAccount> {
        return runCatching {
            val response = api.login(LoginRequest(email, password))
            val body = response.body()
            if (!response.isSuccessful || body?.success != true || body.user == null) {
                throw Exception(body?.message ?: "Invalid credentials")
            }
            RetrofitClient.setAuthToken(body.token)
            RetrofitClient.setUserId(body.user.id)
            body.user
        }.recoverCatching { e ->
            throw Exception(e.message ?: "Cannot reach backend at ${RetrofitClient.getBaseUrl()}")
        }
    }

    suspend fun googleSignIn(displayName: String, email: String): Result<UserAccount> {
        return runCatching {
            val response = api.googleSignIn(GoogleAuthRequest("mock_token", email, displayName))
            val body = response.body()
            if (!response.isSuccessful || body?.success != true || body.user == null) {
                throw Exception(body?.message ?: "Google sign-in failed")
            }
            RetrofitClient.setAuthToken(body.token)
            RetrofitClient.setUserId(body.user.id)
            body.user
        }.recoverCatching { e ->
            throw Exception("Cannot reach backend at ${RetrofitClient.getBaseUrl()}. ${e.message ?: ""}".trim())
        }
    }

    /** Restores the user session using the stored Bearer token. */
    suspend fun restoreSession(): Result<UserAccount?> = runCatching {
        if (RetrofitClient.getUserId().isBlank()) return@runCatching null
        val response = api.whoami()
        if (response.code() == 401) {
            // Token expired or invalid — clear it.
            RetrofitClient.setAuthToken(null)
            RetrofitClient.setUserId(null)
            return@runCatching null
        }
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) return@runCatching null
        body.user
    }

    suspend fun updateProfile(userId: String, fullName: String? = null, phone: String? = null): Result<UserAccount> {
        return runCatching {
            val response = api.updateUser(userId, UpdateProfileRequest(fullName = fullName, phone = phone))
            val body = response.body()
            if (!response.isSuccessful || body?.success != true || body.user == null) {
                throw Exception(body?.message ?: "Profile update failed (HTTP ${response.code()})")
            }
            body.user
        }
    }

    // ─────────────── FLIGHTS ───────────────

    suspend fun findFlight(bookingReference: String, lastName: String): Result<FlightItinerary> {
        return runCatching {
            val response = api.lookupFlight(bookingReference.trim(), lastName.trim())
            val body = response.body()
            if (!response.isSuccessful || body?.success != true || body.flight == null) {
                throw Exception(body?.message ?: "No booking found for $bookingReference / $lastName")
            }
            body.flight
        }
    }

    suspend fun getSeatMap(flightId: String): Result<List<Seat>> {
        return runCatching {
            val response = api.getSeatMap(flightId)
            val body = response.body()
            if (!response.isSuccessful || body?.success != true) {
                throw Exception(body?.message ?: "Failed to load seat map")
            }
            body.seats
        }
    }

    // ─────────────── USER DATA ───────────────

    suspend fun getUserBoardingPasses(userId: String): Result<List<BoardingPass>> {
        return runCatching {
            val response = api.getUserBoardingPasses(userId)
            if (!response.isSuccessful) throw Exception("Failed to load boarding passes (HTTP ${response.code()})")
            response.body() ?: emptyList()
        }
    }

    suspend fun getUserFlights(userId: String): Result<List<FlightItinerary>> {
        return runCatching {
            val response = api.getUserFlights(userId)
            if (!response.isSuccessful) throw Exception("Failed to load flights (HTTP ${response.code()})")
            response.body() ?: emptyList()
        }
    }

    // ─────────────── CHECK-IN ───────────────

    suspend fun completeCheckIn(userId: String, draft: CheckInDraft): Result<BoardingPass> {
        return runCatching {
            val request = CheckInRequest(
                userId = userId,
                bookingReference = draft.itinerary.bookingReference,
                passengerName = draft.itinerary.passengerName,
                passportInfo = draft.passportInfo ?: PassportInfo(),
                selectedSeat = draft.selectedSeat ?: "",
                baggage = draft.baggageDeclaration,
                specialRequests = draft.specialRequests
            )
            val response = api.completeCheckIn(request)
            val body = response.body()
            if (!response.isSuccessful || body?.success != true || body.boardingPass == null) {
                throw Exception(body?.message ?: "Check-in failed (HTTP ${response.code()})")
            }
            body.boardingPass
        }
    }

    // ─────────────── SYNC ───────────────

    suspend fun synchronize(userId: String): Result<SyncResponse> {
        return runCatching {
            val response = api.syncData(SyncRequest(userId))
            val body = response.body()
            if (!response.isSuccessful || body?.success != true) {
                throw Exception(body?.message ?: "Sync failed")
            }
            body
        }
    }

    // ─────────────── LOGOUT ───────────────

    fun logout() {
        RetrofitClient.setAuthToken(null)
        RetrofitClient.setUserId(null)
    }
}
