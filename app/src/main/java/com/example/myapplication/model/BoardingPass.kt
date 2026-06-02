package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class BoardingPass(
    val id: String = "",
    val bookingReference: String = "",
    val passengerName: String = "",
    val flightNumber: String = "",
    val airlineName: String = "Air Algérie",
    val origin: String = "",
    val originCity: String = "",
    val destination: String = "",
    val destinationCity: String = "",
    val departureTime: String = "",
    val arrivalTime: String = "",
    val gate: String = "",
    val terminal: String = "",
    val seat: String = "",
    val seatClass: String = "Economy",
    val boardingGroup: String = "A",
    val sequence: String = "001",
    val qrPayload: String = "",
    val issuedAt: String = "",
    val baggageInfo: String = "",
    val status: String = "Active"
)
