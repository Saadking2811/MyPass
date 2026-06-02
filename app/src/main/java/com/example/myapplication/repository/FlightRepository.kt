package com.example.myapplication.repository

import com.example.myapplication.model.FlightItinerary
import com.example.myapplication.model.Seat
import com.example.myapplication.network.RetrofitClient

/** Flight lookup, seat map, and the user's flight history. */
class FlightRepository {

    private val api get() = RetrofitClient.apiService

    suspend fun findFlight(bookingReference: String, lastName: String): Result<FlightItinerary> = runCatching {
        val response = api.lookupFlight(bookingReference.trim(), lastName.trim())
        val body = response.body()
        if (!response.isSuccessful || body?.success != true || body.flight == null) {
            throw Exception(body?.message ?: "No booking found for $bookingReference / $lastName")
        }
        body.flight
    }

    suspend fun getSeatMap(flightId: String): Result<List<Seat>> = runCatching {
        val response = api.getSeatMap(flightId)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            throw Exception(body?.message ?: "Failed to load seat map")
        }
        body.seats
    }

    suspend fun getUserFlights(userId: String): Result<List<FlightItinerary>> = runCatching {
        val response = api.getUserFlights(userId)
        if (!response.isSuccessful) throw Exception("Failed to load flights (HTTP ${response.code()})")
        response.body() ?: emptyList()
    }
}
