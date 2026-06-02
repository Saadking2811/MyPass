package com.example.myapplication.repository

import com.example.myapplication.model.BoardingPass
import com.example.myapplication.model.CheckInDraft
import com.example.myapplication.model.PassportInfo
import com.example.myapplication.network.CheckInRequest
import com.example.myapplication.network.RetrofitClient

/** Boarding pass listing + check-in submission. */
class BoardingPassRepository {

    private val api get() = RetrofitClient.apiService

    suspend fun getUserBoardingPasses(userId: String): Result<List<BoardingPass>> = runCatching {
        val response = api.getUserBoardingPasses(userId)
        if (!response.isSuccessful) throw Exception("Failed to load boarding passes (HTTP ${response.code()})")
        response.body() ?: emptyList()
    }

    suspend fun completeCheckIn(userId: String, draft: CheckInDraft): Result<BoardingPass> = runCatching {
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
