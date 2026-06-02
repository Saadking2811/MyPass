package com.example.myapplication.util

import com.example.myapplication.model.FlightItinerary
import com.example.myapplication.model.PassportInfo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Cross-checks a scanned passport against the booking the user is checking in for.
 * Returns a list of human-readable issues; empty list means the passport is valid
 * for this trip.
 */
object PassportValidator {

    fun validate(passport: PassportInfo, itinerary: FlightItinerary): List<String> {
        val issues = mutableListOf<String>()

        // 1. Expiry — must not be in the past, and many airlines require 6 months remaining
        val expiry = parseDate(passport.expiryDate)
        if (expiry != null) {
            val today = LocalDate.now()
            if (expiry.isBefore(today)) {
                issues += "Passport expired on ${passport.expiryDate}. Please renew before traveling."
            } else if (expiry.isBefore(today.plusMonths(6))) {
                issues += "Passport expires in less than 6 months (${passport.expiryDate}). Some destinations may refuse entry."
            }
        } else if (passport.expiryDate.isNotBlank()) {
            issues += "Could not read passport expiry date — please rescan."
        }

        // 2. Name match — booking passenger name vs passport full name
        val passportTokens = tokenize(passport.fullName)
        val bookingTokens  = tokenize(itinerary.passengerName)
        val bookingLast    = normalize(itinerary.lastName)

        if (passportTokens.isNotEmpty() && (bookingTokens.isNotEmpty() || bookingLast.isNotEmpty())) {
            val lastNameMatches = bookingLast.isEmpty() || passportTokens.any { token ->
                token == bookingLast ||
                token.startsWith(bookingLast) ||
                bookingLast.startsWith(token) && token.length >= 3
            }
            val anyTokenOverlap = passportTokens.intersect(bookingTokens.toSet()).isNotEmpty()

            if (!lastNameMatches && !anyTokenOverlap) {
                issues += "Passport name (${passport.fullName.trim()}) does not match the booking (${itinerary.passengerName.trim()})."
            }
        }

        return issues
    }

    private fun tokenize(s: String): List<String> =
        normalize(s).split(' ').filter { it.length >= 2 }

    private fun normalize(s: String): String = s.uppercase(Locale.ROOT)
        .replace(Regex("[^A-Z ]"), " ")
        .trim()
        .replace(Regex("\\s+"), " ")

    private fun parseDate(s: String): LocalDate? {
        val trimmed = s.trim()
        val formats = listOf("dd/MM/yyyy", "d/M/yyyy", "yyyy-MM-dd", "dd-MM-yyyy")
        for (f in formats) {
            runCatching {
                return LocalDate.parse(trimmed, DateTimeFormatter.ofPattern(f, Locale.ENGLISH))
            }
        }
        return null
    }
}
