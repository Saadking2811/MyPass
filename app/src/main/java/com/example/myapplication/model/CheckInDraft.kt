package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class CheckInDraft(
    val itinerary: FlightItinerary,
    val passportInfo: PassportInfo? = null,
    val selectedSeat: String? = null,
    val baggageDeclaration: BaggageDeclaration = BaggageDeclaration(),
    val specialRequests: SpecialRequests = SpecialRequests(),
    val currentStep: Int = 0
)

data class CheckInStep(
    val index: Int,
    val title: String,
    val icon: String,
    val completed: Boolean = false,
    val active: Boolean = false
)
