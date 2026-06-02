package com.example.myapplication.ui.util

/** Marketing destination tile shown on Home / Explore. Backend-independent. */
data class Destination(
    val code: String,
    val city: String,
    val country: String,
    val imageUrl: String,
    val price: String
)

val popularDestinations: List<Destination> = listOf(
    Destination("CDG", "Paris",     "France",  Img.PARIS,     ""),
    Destination("DXB", "Dubai",     "UAE",     Img.DUBAI,     ""),
    Destination("IST", "Istanbul",  "Turkey",  Img.ISTANBUL,  ""),
    Destination("LHR", "London",    "UK",      Img.LONDON,    ""),
    Destination("BCN", "Barcelona", "Spain",   Img.BARCELONA, ""),
    Destination("FCO", "Rome",      "Italy",   Img.ROME,      ""),
    Destination("NRT", "Tokyo",     "Japan",   Img.TOKYO,     ""),
    Destination("JFK", "New York",  "USA",     Img.NYC,       "")
)
