package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class FlightItinerary(
    val id: String = "",
    val bookingReference: String = "",
    val lastName: String = "",
    val passengerName: String = "",
    val flightNumber: String = "",
    val airlineName: String = "Air Algérie",
    val airlineLogo: String = "",
    val aircraftType: String = "Boeing 737-800",
    val origin: String = "",
    val originCity: String = "",
    val destination: String = "",
    val destinationCity: String = "",
    val departureTime: String = "",
    val arrivalTime: String = "",
    val duration: String = "",
    val gate: String = "",
    val terminal: String = "",
    val seatClass: String = "Economy",
    val checkInOpen: Boolean = false,
    val checkInStatus: String = "Not Checked-In",
    val price: Double = 0.0,
    val currency: String = "DZD"
)
