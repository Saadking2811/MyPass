package com.example.flightbooking.model

data class Departure(
    val `actual`: String,
    val actual_runway: String,
    val airport: String,
    val delay: Int,
    val estimated: String,
    val estimated_runway: String,
    val gate: String,
    val iata: String,
    val icao: String,
    val scheduled: String,
    val terminal: String,
    val timezone: String
)