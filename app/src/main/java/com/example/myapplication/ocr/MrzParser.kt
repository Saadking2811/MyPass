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

        // Position-aware normalization fixes per-character OCR confusions.
        // Numeric MRZ positions (0-8, 13-18, 19, 21-26, 27, 28-41, 42, 43 of line 2)
        // must be digits — common confusions: O→0, I→1, B→8, S→5, Z→2, A→4
        // Letter positions (country codes, name) — confusions: 0→O, 1→I, 8→B, 5→S
        val fixedLine2 = fixLine2Positions(line2)

        // ── Line 1 parsing ──
        val docType = line1.substring(0, 2).replace("<", "").ifEmpty { "P" }
        val issuingCountry = fixLetters(line1.substring(2, 5).replace("<", ""))
        val nameField = line1.substring(5)

        // Smart filler detection: OCR often misreads runs of '<' as runs of identical
        // letters (K, L, c, etc.). Detect long letter runs and treat them as fillers.
        val sanitizedNameField = recoverFillerFromLetterRuns(nameField)
        val (lastName, firstName) = extractNames(sanitizedNameField)

        // ── Line 2 parsing (after digit/letter normalization) ──
        val passportNumber = fixedLine2.substring(0, 9).replace("<", "")
        val passCheckDigit = charToValue(fixedLine2[9])
        val nationality = fixLetters(fixedLine2.substring(10, 13).replace("<", ""))
        val dobRaw = fixedLine2.substring(13, 19)
        val dobCheckDigit = charToValue(fixedLine2[19])
        val sex = fixedLine2.substring(20, 21)
        val expiryRaw = fixedLine2.substring(21, 27)
        val expiryCheckDigit = charToValue(fixedLine2[27])
        val personalNumber = fixedLine2.substring(28, 42).replace("<", "")
        val personalCheckDigit = charToValue(fixedLine2[42])
        val compositeCheckDigit = charToValue(fixedLine2[43])

        // ── Check digit verification (ICAO weighted algorithm) — uses fixed line2 ──
        val passValid = computeCheckDigit(fixedLine2.substring(0, 9)) == passCheckDigit
        val dobValid = computeCheckDigit(dobRaw) == dobCheckDigit
        val expiryValid = computeCheckDigit(expiryRaw) == expiryCheckDigit
        val compositeData = fixedLine2.substring(0, 10) + fixedLine2.substring(13, 20) + fixedLine2.substring(21, 43)
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
        // Keep yy as a 2-char String — converting to Int drops a leading zero
        // ("04".toInt() = 4 → "$yy" = "4" → year becomes "204" instead of "2004")
        val yyStr = yymmdd.substring(0, 2)
        val yyNum = yyStr.toIntOrNull() ?: return yymmdd
        val mm = yymmdd.substring(2, 4)
        val dd = yymmdd.substring(4, 6)
        val century = if (yyNum > 50) "19" else "20"
        return "$dd/$mm/$century$yyStr"
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

    // ═══════════════════════════════════════════════════════════════
    //  Position-aware OCR fixes
    //  ICAO 9303 MRZ uses fixed character classes per position.
    //  Numeric fields → fix letter→digit confusions (O→0, I→1, B→8, …)
    //  Letter fields  → fix digit→letter confusions (0→O, 1→I, 8→B, …)
    // ═══════════════════════════════════════════════════════════════

    /** Force a char to a digit using common OCR confusion table. */
    private fun toDigit(c: Char): Char = when (c) {
        in '0'..'9' -> c
        'O', 'o', 'Q', 'D' -> '0'
        'I', 'l', '|', 'T' -> '1'
        'Z', 'z' -> '2'
        'A' -> '4'
        'S', 's', '$' -> '5'
        'G', 'C' -> '6'
        'B' -> '8'
        'g', 'q' -> '9'
        else -> c
    }

    /** Force a char to a letter using common OCR confusion table. */
    private fun toLetter(c: Char): Char = when (c) {
        in 'A'..'Z' -> c
        in 'a'..'z' -> c.uppercaseChar()
        '0' -> 'O'
        '1' -> 'I'
        '2' -> 'Z'
        '4' -> 'A'
        '5' -> 'S'
        '6' -> 'G'
        '8' -> 'B'
        else -> c
    }

    /** Convert every char of a letter-only field (country, name) to its best letter guess. */
    private fun fixLetters(s: String): String = s.map { toLetter(it) }.joinToString("")

    /**
     * Letters that are easily mistaken for the MRZ filler character `<` by OCR.
     * Used by [cleanFirstName] and [splitCompoundName] to detect orphan filler residues.
     */
    private val ORPHAN_LETTERS = setOf('C', 'K', 'L', 'T', 'I', 'F', 'V', 'J', 'Y')

    /**
     * Apply per-position character-class corrections to TD3 line 2 (44 chars).
     * Layout:
     *   Pos 0-8   numeric  (passport number — alphanumeric in spec but mostly digits)
     *   Pos 9     numeric  (check digit)
     *   Pos 10-12 letters  (nationality)
     *   Pos 13-18 numeric  (DOB)
     *   Pos 19    numeric  (check digit)
     *   Pos 20    letter   (sex: M/F/<)
     *   Pos 21-26 numeric  (expiry)
     *   Pos 27    numeric  (check digit)
     *   Pos 28-41 alphanum (personal number)
     *   Pos 42    numeric  (check digit)
     *   Pos 43    numeric  (composite check digit)
     */
    private fun fixLine2Positions(line2: String): String {
        if (line2.length != 44) return line2
        val arr = line2.toCharArray()
        // Numeric positions
        val numericRanges = listOf(9..9, 13..19, 21..27, 42..43)
        numericRanges.forEach { range ->
            for (i in range) {
                if (arr[i] != '<') arr[i] = toDigit(arr[i])
            }
        }
        // DOB (13-18) and expiry (21-26) get extra-aggressive digit forcing
        // since they are critical for check-digit validation.
        for (i in 13..18) if (arr[i] != '<') arr[i] = toDigit(arr[i])
        for (i in 21..26) if (arr[i] != '<') arr[i] = toDigit(arr[i])

        // Nationality (10-12) — letters only
        for (i in 10..12) if (arr[i] != '<') arr[i] = toLetter(arr[i])

        // Passport number (0-8) — keep as-is (alphanumeric per spec) but light fixes
        // We don't aggressively force digits here because some passports do use letters.

        return String(arr)
    }

    /**
     * OCR engines often misread runs of `<` as runs of identical letters (K, L, c, T, ...).
     * Detect runs of 4+ consecutive identical letters at the END of the name field, or
     * 2+ identical letters used as a delimiter, and convert them back to `<`.
     *
     * Heuristic: in a real MRZ name field, real names rarely have runs of 4+ identical
     * letters. So treat such runs as filler.
     */
    private fun recoverFillerFromLetterRuns(nameField: String): String {
        // Step A: find runs of 4+ identical chars (very likely OCR'd filler)
        var result = nameField
        val longRunRegex = Regex("(.)\\1{3,}")
        longRunRegex.findAll(nameField).forEach { match ->
            val ch = match.value[0]
            if (ch != '<') {
                result = result.replace(Regex(Regex.escape(ch.toString()) + "{3,}")) { m ->
                    "<".repeat(m.value.length)
                }
            }
        }

        // Step B: any pair (or longer) of the SAME non-< character that follows real letters
        // and appears in the body is likely a misread `<<` delimiter.
        // Try the most common non-< character that appears 2+ times in a row.
        val pairRegex = Regex("([A-Z])\\1+")
        val candidates = pairRegex.findAll(result).map { it.value[0] }.toList().groupingBy { it }.eachCount()
        // Prefer rare chars (K, X, Q, Z) — common in OCR errors but rare as repeated name letters
        val likelyFiller = candidates.entries
            .filter { it.value >= 1 && it.key !in setOf('A', 'E', 'I', 'O', 'U', 'L', 'N', 'M', 'R', 'S', 'T') }
            .maxByOrNull { it.value }?.key

        if (likelyFiller != null) {
            // Replace runs of 2+ likelyFiller with <
            result = result.replace(Regex(Regex.escape(likelyFiller.toString()) + "{2,}")) { m ->
                "<".repeat(m.value.length)
            }
        }

        return result
    }

    /**
     * Extract last and first names from MRZ name field (post-filler-recovery).
     * Tries `<<` split first, then falls back to splitting on any run of `<`.
     * Also strips orphan lone characters that are likely OCR-mistaken `<` chars
     * stuck to the start of the first name (e.g. "<<SAAD" misread as "<CSAAD" → "CSAAD").
     */
    private fun extractNames(nameField: String): Pair<String, String> {
        // Strategy 1: Standard `<<` separator
        val parts1 = nameField.split("<<", limit = 2)
        if (parts1.size == 2) {
            val last  = parts1[0].replace("<", " ").trim()
            val firstRaw = parts1[1].replace("<", " ").trim()
            if (last.isNotBlank() && last.length >= 2 && last.all { it.isLetter() || it == ' ' }) {
                return last to cleanFirstName(firstRaw)
            }
        }

        // Strategy 2: Split on any run of `<` (1 or more)
        val parts2 = nameField.split(Regex("<+")).filter { it.isNotBlank() }
        if (parts2.size >= 2) {
            val last  = parts2[0].trim()
            val firstRaw = parts2.drop(1).joinToString(" ").trim()
            if (last.length >= 2) return last to cleanFirstName(firstRaw)
        }

        // Strategy 3: take first sequence of letters as last name
        if (parts2.size == 1) return parts2[0].trim() to ""

        return "" to ""
    }

    /**
     * Clean a parsed first name field by:
     *  1) stripping leading 1-char "words" that are misread `<<` delimiters
     *  2) stripping leading orphan letters glued to the first word (e.g. "CSAAD" → "SAAD")
     *  3) splitting compound words that look like two names fused by a misread `<`
     *     (e.g. "SAADCSEIF" → "SAAD SEIF", "SEIFCEL" → "SEIF EL")
     */
    private fun cleanFirstName(first: String): String {
        var words = first.split(' ').filter { it.isNotBlank() }.toMutableList()

        // Step 1: strip lone 1-char first/last words (often misread `<` fillers)
        while (words.size >= 2 && words[0].length == 1) words.removeAt(0)
        while (words.size >= 2 && words.last().length == 1) words.removeAt(words.size - 1)

        // Step 2: strip leading orphan letter glued to the first word
        val orphanLetters = ORPHAN_LETTERS
        if (words.isNotEmpty()) {
            val w0 = words[0]
            if (w0.length in 5..14 && w0[0] in orphanLetters &&
                w0.drop(1).length >= 3 && w0.drop(1).all { it.isLetter() }) {
                words[0] = w0.drop(1)
            }
            // Also strip trailing orphan letter glued to the last word
            val wLast = words.last()
            if (wLast.length in 5..14 && wLast.last() in orphanLetters &&
                wLast.dropLast(1).length >= 3 && wLast.dropLast(1).all { it.isLetter() }) {
                words[words.size - 1] = wLast.dropLast(1)
            }
        }

        // Step 3: split every word that looks like two names fused by a misread `<`
        words = words.flatMap { splitCompoundName(it) }.toMutableList()

        return words.joinToString(" ")
    }

    /**
     * Detect compound names like "SAADCSEIF" where the `<` between two names
     * was misread as a letter. Returns 1 or 2 words.
     *
     * Heuristic:
     *  - word must be 7+ letters
     *  - find an orphan-letter (C, K, L, T, I, F) at position 3..len-3
     *  - if both surrounding halves are 3+ letters, split there
     */
    private fun splitCompoundName(word: String): List<String> {
        if (word.length < 7 || !word.all { it.isLetter() }) return listOf(word)
        val orphanLetters = ORPHAN_LETTERS

        // Scan positions where an orphan letter sits between two letter neighbours,
        // and the right-hand half looks like a proper name (starts with a strong consonant
        // pair break, e.g. "SEIF", "EL", "MOHAMED").
        for (i in 3..word.length - 4) {
            val ch = word[i]
            if (ch !in orphanLetters) continue
            val left = word.substring(0, i)
            val right = word.substring(i + 1)
            if (left.length >= 3 && right.length >= 3 &&
                left.all { it.isLetter() } && right.all { it.isLetter() }) {
                // Verify the split is plausible: the orphan letter shouldn't be the only
                // possible vowel in a syllable. If left ends with a consonant and right
                // starts with a consonant, it's a strong signal of a name boundary.
                val vowels = setOf('A', 'E', 'I', 'O', 'U', 'Y')
                val leftEndsConsonant = left.last() !in vowels
                val rightStartsConsonant = right.first() !in vowels
                if (leftEndsConsonant && rightStartsConsonant) {
                    // Recurse on the right half — there may be more fused names
                    return listOf(left) + splitCompoundName(right)
                }
            }
        }
        return listOf(word)
    }
}
