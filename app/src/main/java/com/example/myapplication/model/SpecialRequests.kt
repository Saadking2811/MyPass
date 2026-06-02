package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class SpecialRequests(
    val dietaryPreference: String = "Standard",
    val needsAssistance: Boolean = false,
    val assistanceType: String = "",
    val travelingWithInfant: Boolean = false,
    val infantName: String = "",
    val travelingWithPet: Boolean = false,
    val petType: String = "",
    val notes: String = ""
)
