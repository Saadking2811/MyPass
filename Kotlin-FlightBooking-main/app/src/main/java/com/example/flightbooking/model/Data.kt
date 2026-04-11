package com.example.flightbooking.model

data class Data(
    val aircraft: Any,
    val airline: Airline,
    val arrival: Arrival,
    val departure: Departure,
    val flight: Flight,
    val flight_date: String,
    val flight_status: String,
    val live: Any
)