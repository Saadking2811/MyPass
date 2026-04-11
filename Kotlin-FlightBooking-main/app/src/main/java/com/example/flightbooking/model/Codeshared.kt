package com.example.flightbooking.model

data class Codeshared(
    val airline_iata: String,
    val airline_icao: String,
    val airline_name: String,
    val flight_iata: String,
    val flight_icao: String,
    val flight_number: String
)