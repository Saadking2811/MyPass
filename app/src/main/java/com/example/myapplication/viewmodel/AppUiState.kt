package com.example.myapplication.viewmodel

import com.example.myapplication.model.BoardingPass
import com.example.myapplication.model.CheckInDraft
import com.example.myapplication.model.FlightItinerary
import com.example.myapplication.model.Seat
import com.example.myapplication.model.UserAccount

/**
 * Single source of truth for everything the UI renders.
 * Owned by [AppViewModel] and observed via `StateFlow`.
 */
data class AppUiState(
    val currentUser: UserAccount? = null,
    val lookupResult: FlightItinerary? = null,
    val draft: CheckInDraft? = null,
    val seatMap: List<Seat> = emptyList(),
    val latestBoardingPass: BoardingPass? = null,
    val cachedBoardingPasses: List<BoardingPass> = emptyList(),
    val cachedFlights: List<FlightItinerary> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val isOnline: Boolean = true,
    /** True from cold start until `/auth/me` resolves; UI uses this to avoid an auth-screen flicker. */
    val isRestoringSession: Boolean = true,
    val statusMessage: String? = null,
    val passportRawText: String = "",
    val checkInStep: Int = 0,
    /** true = login, false = register. */
    val isAuthMode: Boolean = true
)
