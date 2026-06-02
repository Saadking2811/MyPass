package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class OfflineCache(
    val user: UserAccount? = null,
    val boardingPasses: List<BoardingPass> = emptyList(),
    val flights: List<FlightItinerary> = emptyList(),
    val lastSyncTimestamp: String = ""
)
