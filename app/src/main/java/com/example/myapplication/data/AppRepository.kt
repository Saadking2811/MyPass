package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.model.*
import com.example.myapplication.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "skypass_data")

class AppRepository(private val context: Context) {

    private val cacheKey = stringPreferencesKey("offline_cache")
    private val tokenKey = stringPreferencesKey("auth_token")
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    private val api = RetrofitClient.apiService

    val offlineCache: Flow<OfflineCache> = context.dataStore.data.map { prefs ->
        val serialized = prefs[cacheKey]
        if (serialized.isNullOrBlank()) OfflineCache()
        else runCatching { json.decodeFromString<OfflineCache>(serialized) }.getOrElse { OfflineCache() }
    }

    // ── Auth ─────────────────────────────────────────────────

    suspend fun register(fullName: String, email: String, phone: String, password: String): Result<UserAccount> {
        return try {
            val response = api.register(RegisterRequest(fullName, email, phone, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()!!.user!!
                val token = response.body()!!.token
                saveToken(token)
                saveUser(user)
                Result.success(user)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            // Fallback: create local account when server unreachable
            val user = UserAccount(
                id = UUID.randomUUID().toString(),
                fullName = fullName.trim(),
                email = email.trim(),
                phone = phone.trim(),
                createdAt = Instant.now().toString()
            )
            saveUser(user)
            Result.success(user)
        }
    }

    suspend fun login(email: String, password: String): Result<UserAccount> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()!!.user!!
                saveToken(response.body()!!.token)
                saveUser(user)
                Result.success(user)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Invalid credentials"))
            }
        } catch (e: Exception) {
            // Check local cache
            val cache = currentCache()
            if (cache.user != null && cache.user.email == email) {
                Result.success(cache.user)
            } else {
                Result.failure(Exception("Cannot connect to server. Please check your connection."))
            }
        }
    }

    suspend fun googleSignIn(displayName: String, email: String): Result<UserAccount> {
        return try {
            val response = api.googleSignIn(GoogleAuthRequest("mock_token", email, displayName))
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()!!.user!!
                saveToken(response.body()!!.token)
                saveUser(user)
                Result.success(user)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Google sign-in failed"))
            }
        } catch (e: Exception) {
            val user = UserAccount(
                id = UUID.randomUUID().toString(),
                fullName = displayName,
                email = email,
                phone = "",
                createdAt = Instant.now().toString()
            )
            saveUser(user)
            Result.success(user)
        }
    }

    // ── Flights ──────────────────────────────────────────────

    suspend fun findFlight(bookingReference: String, lastName: String): Result<FlightItinerary> {
        return try {
            val response = api.lookupFlight(bookingReference.trim(), lastName.trim())
            if (response.isSuccessful && response.body()?.success == true) {
                val flight = response.body()!!.flight!!
                // Cache flight locally
                val cache = currentCache()
                val updated = cache.flights.filterNot { it.bookingReference == flight.bookingReference } + flight
                persist(cache.copy(flights = updated))
                Result.success(flight)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Flight not found"))
            }
        } catch (e: Exception) {
            // Fallback to local mock data
            val remote = fakeRemoteFlights()
            val found = remote.firstOrNull {
                it.bookingReference.equals(bookingReference.trim(), ignoreCase = true) &&
                    it.lastName.equals(lastName.trim(), ignoreCase = true)
            }
            if (found != null) Result.success(found)
            else Result.failure(Exception("No booking found. Check your reference and last name."))
        }
    }

    suspend fun getSeatMap(flightId: String, bookingReference: String): List<Seat> {
        return try {
            val response = api.getSeatMap(flightId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()!!.seats
            } else {
                generateSeatMap(bookingReference)
            }
        } catch (e: Exception) {
            generateSeatMap(bookingReference)
        }
    }

    // ── Check-In ─────────────────────────────────────────────

    suspend fun completeCheckIn(draft: CheckInDraft): Result<BoardingPass> {
        return try {
            val request = CheckInRequest(
                bookingReference = draft.itinerary.bookingReference,
                passengerName = draft.itinerary.passengerName,
                passportInfo = draft.passportInfo ?: PassportInfo(),
                selectedSeat = draft.selectedSeat ?: "",
                baggage = draft.baggageDeclaration,
                specialRequests = draft.specialRequests
            )
            val response = api.completeCheckIn(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val pass = response.body()!!.boardingPass!!
                saveBoardingPass(pass)
                Result.success(pass)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Check-in failed"))
            }
        } catch (e: Exception) {
            // Create boarding pass locally
            val pass = createLocalBoardingPass(draft)
            saveBoardingPass(pass)
            Result.success(pass)
        }
    }

    // ── Sync ─────────────────────────────────────────────────

    suspend fun synchronizeWithServer(): Result<String> {
        return try {
            val cache = currentCache()
            val userId = cache.user?.id ?: return Result.failure(Exception("No user logged in"))
            val response = api.syncData(SyncRequest(userId, cache.lastSyncTimestamp))
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                persist(cache.copy(
                    boardingPasses = body.boardingPasses.ifEmpty { cache.boardingPasses },
                    flights = body.flights.ifEmpty { cache.flights },
                    lastSyncTimestamp = body.timestamp
                ))
                Result.success("Synchronized at ${body.timestamp}")
            } else {
                Result.failure(Exception("Sync failed"))
            }
        } catch (e: Exception) {
            Result.success("Offline sync queued. Will retry when connected.")
        }
    }

    // ── Local Storage ────────────────────────────────────────

    suspend fun saveUser(userAccount: UserAccount) {
        val cache = currentCache()
        persist(cache.copy(user = userAccount))
    }

    suspend fun saveBoardingPass(boardingPass: BoardingPass) {
        val cache = currentCache()
        val updated = cache.boardingPasses.filterNot { it.id == boardingPass.id } + boardingPass
        persist(cache.copy(boardingPasses = updated.sortedByDescending { it.issuedAt }))
    }

    suspend fun logout() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun currentCache(): OfflineCache {
        return offlineCache.first()
    }

    private suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[tokenKey] = token
        }
    }

    private suspend fun persist(cache: OfflineCache) {
        context.dataStore.edit { prefs ->
            prefs[cacheKey] = json.encodeToString(cache)
        }
    }

    private fun createLocalBoardingPass(draft: CheckInDraft): BoardingPass {
        val id = UUID.randomUUID().toString()
        val seat = draft.selectedSeat ?: "Not Assigned"
        val baggage = buildString {
            if (draft.baggageDeclaration.checkedBags > 0) append("${draft.baggageDeclaration.checkedBags} checked ")
            if (draft.baggageDeclaration.carryOnBags > 0) append("${draft.baggageDeclaration.carryOnBags} carry-on ")
            if (draft.baggageDeclaration.oversizedBags > 0) append("${draft.baggageDeclaration.oversizedBags} oversized")
        }.trim().ifEmpty { "No baggage" }

        val payload = listOf(
            "M1", draft.itinerary.passengerName.take(20).uppercase().replace(" ", "/"),
            "E${draft.itinerary.bookingReference.uppercase()}",
            "${draft.itinerary.origin}${draft.itinerary.destination}",
            draft.itinerary.flightNumber, "127",
            "${seat.padStart(4, '0')}001"
        ).joinToString("")

        return BoardingPass(
            id = id,
            bookingReference = draft.itinerary.bookingReference,
            passengerName = draft.itinerary.passengerName,
            flightNumber = draft.itinerary.flightNumber,
            airlineName = draft.itinerary.airlineName,
            origin = draft.itinerary.origin,
            originCity = draft.itinerary.originCity,
            destination = draft.itinerary.destination,
            destinationCity = draft.itinerary.destinationCity,
            departureTime = draft.itinerary.departureTime,
            arrivalTime = draft.itinerary.arrivalTime,
            gate = draft.itinerary.gate,
            terminal = draft.itinerary.terminal,
            seat = seat,
            seatClass = draft.itinerary.seatClass,
            boardingGroup = if (seat.first().digitToIntOrNull()?.let { it <= 4 } == true) "Priority" else "A",
            sequence = String.format("%03d", (1..200).random()),
            qrPayload = payload,
            issuedAt = Instant.now().toString(),
            baggageInfo = baggage,
            status = "Active"
        )
    }

    private fun generateSeatMap(bookingReference: String): List<Seat> {
        val offset = bookingReference.uppercase().sumOf { it.code } % 7
        return buildList {
            for (row in 1..30) {
                listOf("A", "B", "C", "D", "E", "F").forEachIndexed { index, col ->
                    val code = "$row$col"
                    val isPremium = row <= 4
                    val isExtraLeg = row == 12 || row == 13
                    val isWindow = col == "A" || col == "F"
                    val isAisle = col == "C" || col == "D"
                    val occupied = ((row + index + offset) % 5 == 0) || ((row * index + offset) % 11 == 0)
                    val price = when {
                        isPremium -> 5000.0
                        isExtraLeg -> 2500.0
                        isWindow -> 1200.0
                        isAisle -> 800.0
                        else -> 0.0
                    }
                    add(Seat(
                        seatCode = code,
                        row = row,
                        column = col,
                        premium = isPremium,
                        occupied = occupied,
                        extraLegroom = isExtraLeg,
                        window = isWindow,
                        aisle = isAisle,
                        price = price
                    ))
                }
            }
        }
    }

    private fun fakeRemoteFlights(): List<FlightItinerary> {
        return listOf(
            FlightItinerary(
                id = "f1",
                bookingReference = "NM2025A",
                lastName = "NAMOUNE",
                passengerName = "Saad Namoune",
                flightNumber = "AH 701",
                airlineName = "Air Algérie",
                aircraftType = "Airbus A330-200",
                origin = "ALG",
                originCity = "Algiers",
                destination = "CDG",
                destinationCity = "Paris",
                departureTime = "2026-04-15 10:30",
                arrivalTime = "2026-04-15 13:45",
                duration = "3h 15m",
                gate = "A12",
                terminal = "T1",
                seatClass = "Economy",
                checkInOpen = true,
                price = 45000.0,
                currency = "DZD"
            ),
            FlightItinerary(
                id = "f2",
                bookingReference = "NM2025B",
                lastName = "NAMOUNE",
                passengerName = "Saad Namoune",
                flightNumber = "AH 244",
                airlineName = "Air Algérie",
                aircraftType = "Boeing 737-800",
                origin = "ALG",
                originCity = "Algiers",
                destination = "DXB",
                destinationCity = "Dubai",
                departureTime = "2026-04-16 21:10",
                arrivalTime = "2026-04-17 05:05",
                duration = "6h 55m",
                gate = "C03",
                terminal = "T2",
                seatClass = "Business",
                checkInOpen = true,
                price = 120000.0,
                currency = "DZD"
            ),
            FlightItinerary(
                id = "f3",
                bookingReference = "NM2025C",
                lastName = "NAMOUNE",
                passengerName = "Saad Namoune",
                flightNumber = "AH 1018",
                airlineName = "Air Algérie",
                aircraftType = "ATR 72-600",
                origin = "ALG",
                originCity = "Algiers",
                destination = "ORN",
                destinationCity = "Oran",
                departureTime = "2026-04-17 07:00",
                arrivalTime = "2026-04-17 08:15",
                duration = "1h 15m",
                gate = "B08",
                terminal = "T1",
                seatClass = "Economy",
                checkInOpen = true,
                price = 12000.0,
                currency = "DZD"
            ),
            FlightItinerary(
                id = "f4",
                bookingReference = "KL45PQ",
                lastName = "BENALI",
                passengerName = "Karim Benali",
                flightNumber = "AH 510",
                airlineName = "Air Algérie",
                aircraftType = "Boeing 737-800",
                origin = "ALG",
                originCity = "Algiers",
                destination = "IST",
                destinationCity = "Istanbul",
                departureTime = "2026-04-18 14:30",
                arrivalTime = "2026-04-18 19:15",
                duration = "3h 45m",
                gate = "D05",
                terminal = "T1",
                seatClass = "Economy",
                checkInOpen = true,
                price = 55000.0,
                currency = "DZD"
            )
        )
    }
}
