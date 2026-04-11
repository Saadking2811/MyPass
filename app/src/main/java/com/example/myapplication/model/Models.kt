package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class UserAccount(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val passwordHash: String = "",
    val avatarUrl: String = "",
    val createdAt: String = ""
)

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

@Serializable
data class PassportInfo(
    val fullName: String = "",
    val passportNumber: String = "",
    val nationality: String = "",
    val dateOfBirth: String = "",
    val expiryDate: String = "",
    val gender: String = "",
    val rawText: String = "",
    val verified: Boolean = false
)

@Serializable
data class BaggageDeclaration(
    val checkedBags: Int = 0,
    val carryOnBags: Int = 1,
    val oversizedBags: Int = 0,
    val totalWeight: Double = 0.0,
    val checkedBagWeight: Double = 23.0,
    val extraBagFee: Double = 0.0
)

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

@Serializable
data class CheckInDraft(
    val itinerary: FlightItinerary,
    val passportInfo: PassportInfo? = null,
    val selectedSeat: String? = null,
    val baggageDeclaration: BaggageDeclaration = BaggageDeclaration(),
    val specialRequests: SpecialRequests = SpecialRequests(),
    val currentStep: Int = 0
)

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

@Serializable
data class OfflineCache(
    val user: UserAccount? = null,
    val boardingPasses: List<BoardingPass> = emptyList(),
    val flights: List<FlightItinerary> = emptyList(),
    val lastSyncTimestamp: String = ""
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String = ""
)

data class CheckInStep(
    val index: Int,
    val title: String,
    val icon: String,
    val completed: Boolean = false,
    val active: Boolean = false
)
