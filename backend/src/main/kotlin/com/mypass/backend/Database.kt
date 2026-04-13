package com.mypass.backend

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

// ═══════════════════════════════════════════════════════════════
//  TABLE DEFINITIONS
// ═══════════════════════════════════════════════════════════════

object UsersTable : Table("users") {
    val id = varchar("id", 36)
    val fullName = varchar("full_name", 200)
    val email = varchar("email", 200).uniqueIndex()
    val phone = varchar("phone", 50).default("")
    val passwordHash = varchar("password_hash", 200)
    val avatarUrl = varchar("avatar_url", 500).default("")
    val createdAt = datetime("created_at")
    override val primaryKey = PrimaryKey(id)
}

object FlightsTable : Table("flights") {
    val id = varchar("id", 36)
    val bookingReference = varchar("booking_reference", 20).index()
    val lastName = varchar("last_name", 100).index()
    val passengerName = varchar("passenger_name", 200)
    val flightNumber = varchar("flight_number", 20)
    val airlineName = varchar("airline_name", 100).default("Air Algérie")
    val aircraftType = varchar("aircraft_type", 100).default("Boeing 737-800")
    val origin = varchar("origin", 10)
    val originCity = varchar("origin_city", 100)
    val destination = varchar("destination", 10)
    val destinationCity = varchar("destination_city", 100)
    val departureTime = varchar("departure_time", 50)
    val arrivalTime = varchar("arrival_time", 50)
    val duration = varchar("duration", 20)
    val gate = varchar("gate", 10)
    val terminal = varchar("terminal", 10)
    val seatClass = varchar("seat_class", 30).default("Economy")
    val checkInOpen = bool("check_in_open").default(true)
    val checkInStatus = varchar("check_in_status", 30).default("Not Checked-In")
    val price = double("price").default(0.0)
    val currency = varchar("currency", 10).default("DZD")
    override val primaryKey = PrimaryKey(id)
}

object SeatsTable : Table("seats") {
    val id = integer("id").autoIncrement()
    val flightId = varchar("flight_id", 36).references(FlightsTable.id)
    val seatCode = varchar("seat_code", 10)
    val row = integer("row")
    val column = varchar("col", 5)
    val premium = bool("premium").default(false)
    val occupied = bool("occupied").default(false)
    val extraLegroom = bool("extra_legroom").default(false)
    val window = bool("window").default(false)
    val aisle = bool("aisle").default(false)
    val price = double("price").default(0.0)
    override val primaryKey = PrimaryKey(id)
}

object BoardingPassesTable : Table("boarding_passes") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id).nullable()
    val bookingReference = varchar("booking_reference", 20)
    val passengerName = varchar("passenger_name", 200)
    val flightNumber = varchar("flight_number", 20)
    val airlineName = varchar("airline_name", 100).default("Air Algérie")
    val origin = varchar("origin", 10)
    val originCity = varchar("origin_city", 100)
    val destination = varchar("destination", 10)
    val destinationCity = varchar("destination_city", 100)
    val departureTime = varchar("departure_time", 50)
    val arrivalTime = varchar("arrival_time", 50)
    val gate = varchar("gate", 10)
    val terminal = varchar("terminal", 10)
    val seat = varchar("seat", 10)
    val seatClass = varchar("seat_class", 30)
    val boardingGroup = varchar("boarding_group", 20)
    val sequence = varchar("sequence", 10)
    val qrPayload = varchar("qr_payload", 500)
    val issuedAt = datetime("issued_at")
    val baggageInfo = varchar("baggage_info", 200).default("")
    val status = varchar("status", 20).default("Active")
    val passportNumber = varchar("passport_number", 20).default("")
    val passportName = varchar("passport_name", 200).default("")
    val nationality = varchar("nationality", 10).default("")
    override val primaryKey = PrimaryKey(id)
}

object CheckInsTable : Table("check_ins") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id).nullable()
    val flightId = varchar("flight_id", 36).references(FlightsTable.id)
    val boardingPassId = varchar("boarding_pass_id", 36).references(BoardingPassesTable.id)
    val passportNumber = varchar("passport_number", 20)
    val passportName = varchar("passport_name", 200)
    val passportNationality = varchar("passport_nationality", 10)
    val passportDob = varchar("passport_dob", 20)
    val passportExpiry = varchar("passport_expiry", 20)
    val passportGender = varchar("passport_gender", 5)
    val selectedSeat = varchar("selected_seat", 10)
    val checkedBags = integer("checked_bags").default(0)
    val carryOnBags = integer("carry_on_bags").default(1)
    val oversizedBags = integer("oversized_bags").default(0)
    val dietaryPreference = varchar("dietary_preference", 50).default("Standard")
    val needsAssistance = bool("needs_assistance").default(false)
    val travelingWithInfant = bool("traveling_with_infant").default(false)
    val travelingWithPet = bool("traveling_with_pet").default(false)
    val notes = text("notes").default("")
    val checkedInAt = datetime("checked_in_at")
    override val primaryKey = PrimaryKey(id)
}

// ═══════════════════════════════════════════════════════════════
//  DATABASE FACTORY
// ═══════════════════════════════════════════════════════════════

object DatabaseFactory {

    fun init() {
        val useH2 = System.getenv("DB_MODE")?.lowercase() == "h2"
        val db = if (useH2 || !isPostgresReachable()) {
            println("⚡ Using H2 in-memory database (dev/test mode)")
            Database.connect("jdbc:h2:mem:mypass;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        } else {
            println("🐘 Connecting to PostgreSQL")
            val config = HikariConfig().apply {
                jdbcUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/mypass"
                driverClassName = "org.postgresql.Driver"
                username = System.getenv("DB_USER") ?: "postgres"
                password = System.getenv("DB_PASSWORD") ?: "postgres"
                maximumPoolSize = 10
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                validate()
            }
            Database.connect(HikariDataSource(config))
        }

        transaction {
            SchemaUtils.create(
                UsersTable,
                FlightsTable,
                SeatsTable,
                BoardingPassesTable,
                CheckInsTable
            )
            seedDataIfEmpty()
        }
    }

    private fun isPostgresReachable(): Boolean {
        return try {
            val url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/mypass"
            val host = url.substringAfter("://").substringBefore(":").substringBefore("/")
            val port = url.substringAfter(host).substringAfter(":").substringBefore("/").toIntOrNull() ?: 5432
            java.net.Socket().use { socket ->
                socket.connect(java.net.InetSocketAddress(host, port), 2000)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun seedDataIfEmpty() {
        if (FlightsTable.selectAll().count() > 0) return

        // ── Seed flights ────────────────────────────────────
        // ── Seed Namoune user ────────────────────────────────
        val namouneId = "user-namoune-001"
        UsersTable.insert {
            it[id] = namouneId
            it[fullName] = "Saad Namoune"
            it[email] = "namoune@mypass.dz"
            it[phone] = "+213 555 123 456"
            it[passwordHash] = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults().hashToString(12, "namoune123".toCharArray())
            it[createdAt] = java.time.LocalDateTime.now()
        }

        // ── Seed flights ────────────────────────────────────
        val flights = listOf(
            mapOf(
                "id" to "f1", "ref" to "NM2025A", "last" to "NAMOUNE",
                "name" to "Saad Namoune", "flight" to "AH 701",
                "aircraft" to "Airbus A330-200",
                "origin" to "ALG", "originCity" to "Algiers",
                "dest" to "CDG", "destCity" to "Paris",
                "dep" to "2026-04-15 10:30", "arr" to "2026-04-15 13:45",
                "dur" to "3h 15m", "gate" to "A12", "term" to "T1",
                "class" to "Economy", "price" to 45000.0
            ),
            mapOf(
                "id" to "f2", "ref" to "NM2025B", "last" to "NAMOUNE",
                "name" to "Saad Namoune", "flight" to "AH 244",
                "aircraft" to "Boeing 737-800",
                "origin" to "ALG", "originCity" to "Algiers",
                "dest" to "DXB", "destCity" to "Dubai",
                "dep" to "2026-04-16 21:10", "arr" to "2026-04-17 05:05",
                "dur" to "6h 55m", "gate" to "C03", "term" to "T2",
                "class" to "Business", "price" to 120000.0
            ),
            mapOf(
                "id" to "f3", "ref" to "NM2025C", "last" to "NAMOUNE",
                "name" to "Saad Namoune", "flight" to "AH 1018",
                "aircraft" to "ATR 72-600",
                "origin" to "ALG", "originCity" to "Algiers",
                "dest" to "ORN", "destCity" to "Oran",
                "dep" to "2026-04-17 07:00", "arr" to "2026-04-17 08:15",
                "dur" to "1h 15m", "gate" to "B08", "term" to "T1",
                "class" to "Economy", "price" to 12000.0
            ),
            mapOf(
                "id" to "f4", "ref" to "KL45PQ", "last" to "BENALI",
                "name" to "Karim Benali", "flight" to "AH 510",
                "aircraft" to "Boeing 737-800",
                "origin" to "ALG", "originCity" to "Algiers",
                "dest" to "IST", "destCity" to "Istanbul",
                "dep" to "2026-04-18 14:30", "arr" to "2026-04-18 19:15",
                "dur" to "3h 45m", "gate" to "D05", "term" to "T1",
                "class" to "Economy", "price" to 55000.0
            ),
            mapOf(
                "id" to "f5", "ref" to "QW34TY", "last" to "MARTINEZ",
                "name" to "Carlos Martinez", "flight" to "AH 860",
                "aircraft" to "Airbus A330-200",
                "origin" to "ALG", "originCity" to "Algiers",
                "dest" to "JFK", "destCity" to "New York",
                "dep" to "2026-04-19 22:00", "arr" to "2026-04-20 04:30",
                "dur" to "9h 30m", "gate" to "A01", "term" to "T1",
                "class" to "Business", "price" to 250000.0
            )
        )

        flights.forEach { f ->
            val flightId = f["id"] as String
            FlightsTable.insert {
                it[id] = flightId
                it[bookingReference] = f["ref"] as String
                it[lastName] = f["last"] as String
                it[passengerName] = f["name"] as String
                it[flightNumber] = f["flight"] as String
                it[aircraftType] = f["aircraft"] as String
                it[origin] = f["origin"] as String
                it[originCity] = f["originCity"] as String
                it[destination] = f["dest"] as String
                it[destinationCity] = f["destCity"] as String
                it[departureTime] = f["dep"] as String
                it[arrivalTime] = f["arr"] as String
                it[duration] = f["dur"] as String
                it[gate] = f["gate"] as String
                it[terminal] = f["term"] as String
                it[seatClass] = f["class"] as String
                it[price] = f["price"] as Double
            }

            // Generate seat map for each flight
            val offset = (f["ref"] as String).sumOf { it.code } % 7
            for (row in 1..30) {
                listOf("A", "B", "C", "D", "E", "F").forEachIndexed { index, col ->
                    val code = "$row$col"
                    val isPremium = row <= 4
                    val isExtraLeg = row == 12 || row == 13
                    val isWindow = col == "A" || col == "F"
                    val isAisle = col == "C" || col == "D"
                    val isOccupied = ((row + index + offset) % 5 == 0) || ((row * index + offset) % 11 == 0)
                    val seatPrice = when {
                        isPremium -> 5000.0
                        isExtraLeg -> 2500.0
                        isWindow -> 1200.0
                        isAisle -> 800.0
                        else -> 0.0
                    }
                    SeatsTable.insert {
                        it[SeatsTable.flightId] = flightId
                        it[seatCode] = code
                        it[SeatsTable.row] = row
                        it[column] = col
                        it[premium] = isPremium
                        it[occupied] = isOccupied
                        it[extraLegroom] = isExtraLeg
                        it[window] = isWindow
                        it[aisle] = isAisle
                        it[price] = seatPrice
                    }
                }
            }
        }
    }
}
