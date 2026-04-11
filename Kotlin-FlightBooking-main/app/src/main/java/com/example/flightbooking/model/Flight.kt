package com.example.flightbooking.model

data class Flight(
    val codeshared: Codeshared,
    val iata: String,
    val icao: String,
    val number: String
)