package com.example.myapplication.ocr

/**
 * ICAO 9303 Machine Readable Zone (MRZ) Parser
 *
 * Implements the international standard used by:
 * - SITA iBorders (90% of world airlines)
 * - Amadeus Altéa Check-in
 * - Regula Document Reader SDK
 * - Thales/Gemalto border control systems
 * - ReadID NFC passport readers
 *
 * Supports TD3 format (passport): 2 lines × 44 characters
 * Validates check digits using ICAO weighted algorithm (7-3-1)
 */
object MrzParser {

    data class MrzResult(
        val documentType: String,
        val issuingCountry: String,
        val lastName: String,
        val firstName: String,
        val passportNumber: String,
        val nationality: String,
        val dateOfBirth: String,
        val sex: String,
        val expiryDate: String,
        val personalNumber: String,
        val isValid: Boolean,
        val checkDigitsValid: CheckDigits,
        val rawMrz: String,
        val confidence: Float
    )

    data class CheckDigits(
        val passportNumberValid: Boolean,
        val dateOfBirthValid: Boolean,
        val expiryDateValid: Boolean,
        val compositeValid: Boolean
    ) {
        val allValid get() = passportNumberValid && dateOfBirthValid && expiryDateValid
    }

    // ISO 3166-1 alpha-3 country codes for common nationalities
    private val COUNTRY_NAMES = mapOf(
        "DZA" to "Algeria", "FRA" to "France", "USA" to "United States",
        "GBR" to "United Kingdom", "DEU" to "Germany", "MAR" to "Morocco",
        "TUN" to "Tunisia", "EGY" to "Egypt", "SAU" to "Saudi Arabia",
        "ARE" to "UAE", "TUR" to "Turkey", "ESP" to "Spain",
        "ITA" to "Italy", "CAN" to "Canada", "CHN" to "China",
        "JPN" to "Japan", "KOR" to "South Korea", "IND" to "India",
        "BRA" to "Brazil", "RUS" to "Russia", "QAT" to "Qatar",
        "JOR" to "Jordan", "LBN" to "Lebanon", "LBY" to "Libya",
        "NGA" to "Nigeria", "ZAF" to "South Africa", "AUS" to "Australia"
    )

    /**
     * Attempts to find and parse MRZ lines from raw OCR text.
     * Returns null if no valid MRZ is detected.
     */
    fun findMrzInText(ocrText: String): MrzResult? {
        val normalized = ocrText.uppercase()
            .replace("«", "<")
            .replace("‹", "<")
            .replace("»", "<")
            .replace("›", "<")

        val lines = normalized.lines()
            .map { it.trim().replace(" ", "") }
            .map { normalizeOcrMistakes(it) }
            .filter { it.length >= 30 }

        // Strategy 1: Find two consecutive lines that look like TD3 MRZ
        for (i in 0 until lines.size - 1) {
            val line1 = padToLength(lines[i], 44)
            val line2 = padToLength(lines[i + 1], 44)

            if (line1.length == 44 && line2.length == 44 && line1.startsWith("P")) {
                val result = parseTd3(line1, line2)
                if (result != null) return result
            }
        }

        // Strategy 2: Find any lines matching MRZ pattern
        val mrzCandidates = lines.filter { it.length in 40..50 && it.count { c -> c == '<' } >= 3 }
        for (i in 0 until mrzCandidates.size - 1) {
            val line1 = padToLength(mrzCandidates[i], 44)
            val line2 = padToLength(mrzCandidates[i + 1], 44)

            if (line1.startsWith("P") && line1.length == 44 && line2.length == 44) {
                val result = parseTd3(line1, line2)
                if (result != null) return result
            }
        }

        // Strategy 3: Relaxed parsing - find lines with high < density
        val relaxed = lines.filter { line ->
            line.length >= 35 &&
                line.count { it == '<' } >= 5 &&
                line.all { it.isLetterOrDigit() || it == '<' }
        }
        if (relaxed.size >= 2) {
            val l1 = padToLength(relaxed[0], 44)
            val l2 = padToLength(relaxed[1], 44)
            if (l1.length == 44 && l2.length == 44) {
                return parseTd3(l1, l2, relaxed = true)
            }
        }

        return null
    }

    /**
     * Parses a TD3 passport MRZ (2 lines × 44 characters each).
     *
     * Line 1 format: P<UTOERIKSSON<<ANNA<MARIA<<<<<<<<<<<<<<<<<<<
     *   Pos 0-1:  Document type (P = Passport)
     *   Pos 2-4:  Issuing country (ISO 3166-1 alpha-3)
     *   Pos 5-43: Last name << First name(s) (padded with <)
     *
     * Line 2 format: L898902C36UTO7408122F1204159ZE184226B<<<<<10
     *   Pos 0-8:   Passport number
     *   Pos 9:     Check digit (passport number)
     *   Pos 10-12: Nationality (ISO 3166-1 alpha-3)
     *   Pos 13-18: Date of birth (YYMMDD)
     *   Pos 19:    Check digit (DOB)
     *   Pos 20:    Sex (M/F/<)
     *   Pos 21-26: Expiry date (YYMMDD)
     *   Pos 27:    Check digit (expiry)
     *   Pos 28-41: Personal number / optional data
     *   Pos 42:    Check digit (personal number)
     *   Pos 43:    Composite check digit
     */
    private fun parseTd3(line1: String, line2: String, relaxed: Boolean = false): MrzResult? {
        if (line1.length != 44 || line2.length != 44) return null

        // ── Line 1 parsing ──
        val docType = line1.substring(0, 2).replace("<", "").ifEmpty { "P" }
        val issuingCountry = line1.substring(2, 5).replace("<", "")
        val nameField = line1.substring(5)
        val nameParts = nameField.split("<<", limit = 2)
        val lastName = nameParts.getOrElse(0) { "" }.replace("<", " ").trim()
        val firstName = nameParts.getOrElse(1) { "" }.replace("<", " ").trim()

        // ── Line 2 parsing ──
        val passportNumber = line2.substring(0, 9).replace("<", "")
        val passCheckDigit = charToValue(line2[9])
        val nationality = line2.substring(10, 13).replace("<", "")
        val dobRaw = line2.substring(13, 19)
        val dobCheckDigit = charToValue(line2[19])
        val sex = line2.substring(20, 21)
        val expiryRaw = line2.substring(21, 27)
        val expiryCheckDigit = charToValue(line2[27])
        val personalNumber = line2.substring(28, 42).replace("<", "")
        val personalCheckDigit = charToValue(line2[42])
        val compositeCheckDigit = charToValue(line2[43])

        // ── Check digit verification (ICAO weighted algorithm) ──
        val passValid = computeCheckDigit(line2.substring(0, 9)) == passCheckDigit
        val dobValid = computeCheckDigit(dobRaw) == dobCheckDigit
        val expiryValid = computeCheckDigit(expiryRaw) == expiryCheckDigit
        val compositeData = line2.substring(0, 10) + line2.substring(13, 20) + line2.substring(21, 43)
        val compositeValid = computeCheckDigit(compositeData) == compositeCheckDigit

        val checks = CheckDigits(passValid, dobValid, expiryValid, compositeValid)

        // Confidence score
        val confidence = calculateConfidence(
            lastName, firstName, passportNumber, issuingCountry,
            nationality, dobRaw, expiryRaw, checks, relaxed
        )

        // Only return if we have minimum viable data
        if (passportNumber.isBlank() && lastName.isBlank()) return null
        if (!relaxed && confidence < 0.3f) return null

        return MrzResult(
            documentType = docType,
            issuingCountry = issuingCountry,
            lastName = lastName,
            firstName = firstName,
            passportNumber = passportNumber,
            nationality = nationality,
            dateOfBirth = formatMrzDate(dobRaw),
            sex = when (sex) { "M" -> "Male"; "F" -> "Female"; else -> "Unspecified" },
            expiryDate = formatMrzDate(expiryRaw),
            personalNumber = personalNumber,
            isValid = checks.allValid && confidence >= 0.6f,
            checkDigitsValid = checks,
            rawMrz = "$line1\n$line2",
            confidence = confidence
        )
    }

    /**
     * ICAO check digit algorithm:
     * Each character position has a weight cycling through 7, 3, 1.
     * Character values: 0-9 = 0-9, A-Z = 10-35, < = 0
     * Check digit = sum(value × weight) mod 10
     */
    fun computeCheckDigit(data: String): Int {
        val weights = intArrayOf(7, 3, 1)
        var sum = 0
        data.forEachIndexed { index, char ->
            sum += charToValue(char) * weights[index % 3]
        }
        return sum % 10
    }

    private fun charToValue(char: Char): Int = when {
        char.isDigit() -> char.digitToInt()
        char.isLetter() -> char.uppercaseChar().code - 'A'.code + 10
        char == '<' -> 0
        else -> 0
    }

    /**
     * Convert MRZ date (YYMMDD) to human-readable DD/MM/YYYY.
     * Century rule: YY > 50 → 19YY, else → 20YY
     */
    private fun formatMrzDate(yymmdd: String): String {
        if (yymmdd.length != 6 || !yymmdd.all { it.isDigit() }) return yymmdd
        val yy = yymmdd.substring(0, 2).toIntOrNull() ?: return yymmdd
        val mm = yymmdd.substring(2, 4)
        val dd = yymmdd.substring(4, 6)
        val century = if (yy > 50) "19" else "20"
        return "$dd/$mm/$century$yy"
    }

    /**
     * Calculate confidence score (0.0 - 1.0) based on parsed data quality.
     */
    private fun calculateConfidence(
        lastName: String, firstName: String, passportNumber: String,
        issuingCountry: String, nationality: String, dob: String,
        expiry: String, checks: CheckDigits, relaxed: Boolean
    ): Float {
        var score = 0f
        val max = 10f

        // Name quality
        if (lastName.length >= 2 && lastName.all { it.isLetter() || it == ' ' }) score += 1.5f
        if (firstName.length >= 2 && firstName.all { it.isLetter() || it == ' ' }) score += 1f

        // Passport number (typically 8-9 alphanumeric)
        if (passportNumber.length in 6..9 && passportNumber.all { it.isLetterOrDigit() }) score += 1.5f

        // Country codes (3-letter, existing in our map or all letters)
        if (issuingCountry.length == 3 && issuingCountry.all { it.isLetter() }) score += 1f
        if (nationality.length == 3 && nationality.all { it.isLetter() }) score += 1f

        // Date validity
        if (isValidMrzDate(dob)) score += 1f
        if (isValidMrzDate(expiry)) score += 1f

        // Check digits
        if (checks.passportNumberValid) score += 0.5f
        if (checks.dateOfBirthValid) score += 0.5f
        if (checks.expiryDateValid) score += 0.5f
        if (checks.compositeValid) score += 0.5f

        return (score / max).coerceIn(0f, 1f)
    }

    private fun isValidMrzDate(yymmdd: String): Boolean {
        if (yymmdd.length != 6 || !yymmdd.all { it.isDigit() }) return false
        val mm = yymmdd.substring(2, 4).toIntOrNull() ?: return false
        val dd = yymmdd.substring(4, 6).toIntOrNull() ?: return false
        return mm in 1..12 && dd in 1..31
    }

    /**
     * Correct common OCR misreadings in MRZ text.
     * OCR engines often confuse similar-looking characters.
     */
    private fun normalizeOcrMistakes(line: String): String {
        return line
            .replace('О', 'O')  // Cyrillic О → Latin O
            .replace('С', 'C')  // Cyrillic С → Latin C
            .replace('Р', 'P')  // Cyrillic Р → Latin P
            .replace('Н', 'H')  // Cyrillic Н → Latin H
            .replace('В', 'B')  // Cyrillic В → Latin B
            .replace('А', 'A')  // Cyrillic А → Latin A
            .replace('Е', 'E')  // Cyrillic Е → Latin E
            .replace('К', 'K')  // Cyrillic К → Latin K
            .replace('М', 'M')  // Cyrillic М → Latin M
            .replace('Т', 'T')  // Cyrillic Т → Latin T
            .filter { it.isLetterOrDigit() || it == '<' }
    }

    private fun padToLength(line: String, target: Int): String {
        return if (line.length > target) line.take(target)
        else if (line.length < target) line + "<".repeat(target - line.length)
        else line
    }

    /**
     * Get human-readable country name from ISO 3166-1 alpha-3 code.
     */
    fun getCountryName(code: String): String = COUNTRY_NAMES[code.uppercase()] ?: code
}
