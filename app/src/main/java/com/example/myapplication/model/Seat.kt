package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class Seat(
    val seatCode: String,
    val row: Int,
    val column: String,
    val premium: Boolean,
    val occupied: Boolean,
    val extraLegroom: Boolean = false,
    val window: Boolean = false,
    val aisle: Boolean = false,
    val price: Double = 0.0
)
