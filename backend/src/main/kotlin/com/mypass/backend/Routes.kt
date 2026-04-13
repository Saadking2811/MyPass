package com.mypass.backend

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

// ═══════════════════════════════════════════════════════════════
//  REQUEST / RESPONSE MODELS
// ═══════════════════════════════════════════════════════════════

@Serializable data class RegisterReq(val fullName: String, val email: String, val phone: String = "", val password: String)
@Serializable data class LoginReq(val email: String, val password: String)
@Serializable data class GoogleAuthReq(val idToken: String, val email: String, val displayName: String)

@Serializable data class AuthResp(val success: Boolean, val user: UserDto? = null, val token: String = "", val message: String = "")
@Serializable data class UserDto(val id: String, val fullName: String, val email: String, val phone: String, val avatarUrl: String = "", val createdAt: String)

@Serializable data class FlightDto(
    val id: String, val bookingReference: String, val lastName: String, val passengerName: String,
    val flightNumber: String, val airlineName: String, val aircraftType: String,
    val origin: String, val originCity: String, val destination: String, val destinationCity: String,
    val departureTime: String, val arrivalTime: String, val duration: String,
    val gate: String, val terminal: String, val seatClass: String,
    val checkInOpen: Boolean, val checkInStatus: String, val price: Double, val currency: String
)
@Serializable data class FlightLookupResp(val success: Boolean, val flight: FlightDto? = null, val message: String = "")

@Serializable data class SeatDto(
    val seatCode: String, val row: Int, val column: String, val premium: Boolean,
    val occupied: Boolean, val extraLegroom: Boolean, val window: Boolean, val aisle: Boolean, val price: Double
)
@Serializable data class SeatMapResp(val success: Boolean, val seats: List<SeatDto> = emptyList(), val message: String = "")

@Serializable data class PassportDto(
    val fullName: String = "", val passportNumber: String = "", val nationality: String = "",
    val dateOfBirth: String = "", val expiryDate: String = "", val gender: String = ""
)
@Serializable data class BaggageDto(val checkedBags: Int = 0, val carryOnBags: Int = 1, val oversizedBags: Int = 0)
@Serializable data class SpecialReqDto(
    val dietaryPreference: String = "Standard", val needsAssistance: Boolean = false,
    val travelingWithInfant: Boolean = false, val travelingWithPet: Boolean = false, val notes: String = ""
)
@Serializable data class CheckInReq(
    val bookingReference: String, val passengerName: String,
    val passportInfo: PassportDto = PassportDto(), val selectedSeat: String,
    val baggage: BaggageDto = BaggageDto(), val specialRequests: SpecialReqDto = SpecialReqDto()
)

@Serializable data class BoardingPassDto(
    val id: String, val bookingReference: String, val passengerName: String,
    val flightNumber: String, val airlineName: String,
    val origin: String, val originCity: String, val destination: String, val destinationCity: String,
    val departureTime: String, val arrivalTime: String, val gate: String, val terminal: String,
    val seat: String, val seatClass: String, val boardingGroup: String, val sequence: String,
    val qrPayload: String, val issuedAt: String, val baggageInfo: String, val status: String
)
@Serializable data class CheckInResp(val success: Boolean, val boardingPass: BoardingPassDto? = null, val message: String = "")

@Serializable data class SyncReq(val userId: String, val lastSyncTimestamp: String = "")
@Serializable data class SyncResp(
    val success: Boolean, val boardingPasses: List<BoardingPassDto> = emptyList(),
    val flights: List<FlightDto> = emptyList(), val timestamp: String = "", val message: String = ""
)

// ═══════════════════════════════════════════════════════════════
//  ROUTE CONFIGURATION
// ═══════════════════════════════════════════════════════════════

fun Application.configureRoutes() {
    routing {
        route("/api") {

            // ── Health check ─────────────────────────────────
            get("/health") {
                call.respond(mapOf("status" to "ok", "service" to "MyPass Backend", "version" to "1.0.0"))
            }

            // ── AUTH ─────────────────────────────────────────
            route("/auth") {

                post("/register") {
                    val req = call.receive<RegisterReq>()
                    if (req.fullName.isBlank() || req.email.isBlank() || req.password.length < 6) {
                        call.respond(HttpStatusCode.BadRequest, AuthResp(false, message = "Name, email required. Password min 6 chars."))
                        return@post
                    }

                    val existing = transaction { UsersTable.selectAll().where { UsersTable.email eq req.email.trim() }.firstOrNull() }
                    if (existing != null) {
                        call.respond(HttpStatusCode.Conflict, AuthResp(false, message = "Email already registered."))
                        return@post
                    }

                    val userId = UUID.randomUUID().toString()
                    val hash = BCrypt.withDefaults().hashToString(12, req.password.toCharArray())
                    val now = LocalDateTime.now()

                    transaction {
                        UsersTable.insert {
                            it[id] = userId
                            it[fullName] = req.fullName.trim()
                            it[email] = req.email.trim().lowercase()
                            it[phone] = req.phone.trim()
                            it[passwordHash] = hash
                            it[createdAt] = now
                        }
                    }

                    val user = UserDto(userId, req.fullName.trim(), req.email.trim().lowercase(), req.phone.trim(), createdAt = now.toString())
                    call.respond(AuthResp(true, user, token = "jwt_${userId}_${System.currentTimeMillis()}", message = "Registration successful"))
                }

                post("/login") {
                    val req = call.receive<LoginReq>()
                    val row = transaction {
                        UsersTable.selectAll().where { UsersTable.email eq req.email.trim().lowercase() }.firstOrNull()
                    }
                    if (row == null) {
                        call.respond(HttpStatusCode.Unauthorized, AuthResp(false, message = "Invalid credentials."))
                        return@post
                    }

                    val verified = BCrypt.verifyer().verify(req.password.toCharArray(), row[UsersTable.passwordHash])
                    if (!verified.verified) {
                        call.respond(HttpStatusCode.Unauthorized, AuthResp(false, message = "Invalid credentials."))
                        return@post
                    }

                    val user = UserDto(
                        row[UsersTable.id], row[UsersTable.fullName], row[UsersTable.email],
                        row[UsersTable.phone], row[UsersTable.avatarUrl], row[UsersTable.createdAt].toString()
                    )
                    call.respond(AuthResp(true, user, token = "jwt_${user.id}_${System.currentTimeMillis()}", message = "Login successful"))
                }

                post("/google") {
                    val req = call.receive<GoogleAuthReq>()
                    val existing = transaction {
                        UsersTable.selectAll().where { UsersTable.email eq req.email.trim().lowercase() }.firstOrNull()
                    }

                    val user = if (existing != null) {
                        UserDto(
                            existing[UsersTable.id], existing[UsersTable.fullName], existing[UsersTable.email],
                            existing[UsersTable.phone], existing[UsersTable.avatarUrl], existing[UsersTable.createdAt].toString()
                        )
                    } else {
                        val userId = UUID.randomUUID().toString()
                        val now = LocalDateTime.now()
                        transaction {
                            UsersTable.insert {
                                it[id] = userId
                                it[fullName] = req.displayName.trim()
                                it[email] = req.email.trim().lowercase()
                                it[passwordHash] = BCrypt.withDefaults().hashToString(12, UUID.randomUUID().toString().toCharArray())
                                it[createdAt] = now
                            }
                        }
                        UserDto(userId, req.displayName.trim(), req.email.trim().lowercase(), "", createdAt = now.toString())
                    }

                    call.respond(AuthResp(true, user, token = "jwt_${user.id}_${System.currentTimeMillis()}", message = "Google sign-in successful"))
                }
            }

            // ── FLIGHTS ──────────────────────────────────────
            route("/flights") {

                get("/lookup") {
                    val bookingRef = call.request.queryParameters["bookingRef"]?.trim()?.uppercase()
                    val lastName = call.request.queryParameters["lastName"]?.trim()?.uppercase()

                    if (bookingRef.isNullOrBlank() || lastName.isNullOrBlank()) {
                        call.respond(HttpStatusCode.BadRequest, FlightLookupResp(false, message = "bookingRef and lastName are required"))
                        return@get
                    }

                    val row = transaction {
                        FlightsTable.selectAll().where {
                            (FlightsTable.bookingReference.upperCase() eq bookingRef) and
                                    (FlightsTable.lastName.upperCase() eq lastName)
                        }.firstOrNull()
                    }

                    if (row == null) {
                        call.respond(HttpStatusCode.NotFound, FlightLookupResp(false, message = "No booking found for $bookingRef / $lastName"))
                        return@get
                    }

                    call.respond(FlightLookupResp(true, row.toFlightDto()))
                }

                get("/{flightId}/seats") {
                    val flightId = call.parameters["flightId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

                    val seats = transaction {
                        SeatsTable.selectAll().where { SeatsTable.flightId eq flightId }
                            .map { r ->
                                SeatDto(
                                    r[SeatsTable.seatCode], r[SeatsTable.row], r[SeatsTable.column],
                                    r[SeatsTable.premium], r[SeatsTable.occupied], r[SeatsTable.extraLegroom],
                                    r[SeatsTable.window], r[SeatsTable.aisle], r[SeatsTable.price]
                                )
                            }
                    }

                    call.respond(SeatMapResp(true, seats))
                }
            }

            // ── CHECK-IN ─────────────────────────────────────
            route("/checkin") {

                post("/complete") {
                    val req = call.receive<CheckInReq>()

                    val flight = transaction {
                        FlightsTable.selectAll().where {
                            FlightsTable.bookingReference.upperCase() eq req.bookingReference.trim().uppercase()
                        }.firstOrNull()
                    } ?: run {
                        call.respond(HttpStatusCode.NotFound, CheckInResp(false, message = "Flight not found"))
                        return@post
                    }

                    // Mark seat as occupied
                    transaction {
                        SeatsTable.update({
                            (SeatsTable.flightId eq flight[FlightsTable.id]) and
                                    (SeatsTable.seatCode eq req.selectedSeat)
                        }) {
                            it[occupied] = true
                        }

                        // Update flight check-in status
                        FlightsTable.update({ FlightsTable.id eq flight[FlightsTable.id] }) {
                            it[checkInStatus] = "Checked-In"
                        }
                    }

                    val passId = UUID.randomUUID().toString()
                    val now = LocalDateTime.now()
                    val seat = req.selectedSeat
                    val baggage = buildString {
                        if (req.baggage.checkedBags > 0) append("${req.baggage.checkedBags} checked ")
                        if (req.baggage.carryOnBags > 0) append("${req.baggage.carryOnBags} carry-on ")
                        if (req.baggage.oversizedBags > 0) append("${req.baggage.oversizedBags} oversized")
                    }.trim().ifEmpty { "No baggage" }

                    val qrPayload = listOf(
                        "M1", req.passengerName.take(20).uppercase().replace(" ", "/"),
                        "E${req.bookingReference.uppercase()}",
                        "${flight[FlightsTable.origin]}${flight[FlightsTable.destination]}",
                        flight[FlightsTable.flightNumber], "127",
                        "${seat.padStart(4, '0')}001"
                    ).joinToString("")

                    val boardingGroup = if (seat.firstOrNull()?.digitToIntOrNull()?.let { it <= 4 } == true) "Priority" else "A"

                    transaction {
                        BoardingPassesTable.insert {
                            it[id] = passId
                            it[bookingReference] = req.bookingReference.uppercase()
                            it[passengerName] = req.passengerName
                            it[flightNumber] = flight[FlightsTable.flightNumber]
                            it[airlineName] = flight[FlightsTable.airlineName]
                            it[origin] = flight[FlightsTable.origin]
                            it[originCity] = flight[FlightsTable.originCity]
                            it[destination] = flight[FlightsTable.destination]
                            it[destinationCity] = flight[FlightsTable.destinationCity]
                            it[departureTime] = flight[FlightsTable.departureTime]
                            it[arrivalTime] = flight[FlightsTable.arrivalTime]
                            it[BoardingPassesTable.gate] = flight[FlightsTable.gate]
                            it[BoardingPassesTable.terminal] = flight[FlightsTable.terminal]
                            it[BoardingPassesTable.seat] = seat
                            it[seatClass] = flight[FlightsTable.seatClass]
                            it[BoardingPassesTable.boardingGroup] = boardingGroup
                            it[sequence] = String.format("%03d", (1..200).random())
                            it[BoardingPassesTable.qrPayload] = qrPayload
                            it[issuedAt] = now
                            it[baggageInfo] = baggage
                            it[passportNumber] = req.passportInfo.passportNumber
                            it[passportName] = req.passportInfo.fullName
                            it[nationality] = req.passportInfo.nationality
                        }

                        CheckInsTable.insert {
                            it[id] = UUID.randomUUID().toString()
                            it[flightId] = flight[FlightsTable.id]
                            it[boardingPassId] = passId
                            it[passportNumber] = req.passportInfo.passportNumber
                            it[passportName] = req.passportInfo.fullName
                            it[passportNationality] = req.passportInfo.nationality
                            it[passportDob] = req.passportInfo.dateOfBirth
                            it[passportExpiry] = req.passportInfo.expiryDate
                            it[passportGender] = req.passportInfo.gender
                            it[selectedSeat] = seat
                            it[checkedBags] = req.baggage.checkedBags
                            it[carryOnBags] = req.baggage.carryOnBags
                            it[oversizedBags] = req.baggage.oversizedBags
                            it[dietaryPreference] = req.specialRequests.dietaryPreference
                            it[needsAssistance] = req.specialRequests.needsAssistance
                            it[travelingWithInfant] = req.specialRequests.travelingWithInfant
                            it[travelingWithPet] = req.specialRequests.travelingWithPet
                            it[notes] = req.specialRequests.notes
                            it[checkedInAt] = now
                        }
                    }

                    val pass = BoardingPassDto(
                        passId, req.bookingReference.uppercase(), req.passengerName,
                        flight[FlightsTable.flightNumber], flight[FlightsTable.airlineName],
                        flight[FlightsTable.origin], flight[FlightsTable.originCity],
                        flight[FlightsTable.destination], flight[FlightsTable.destinationCity],
                        flight[FlightsTable.departureTime], flight[FlightsTable.arrivalTime],
                        flight[FlightsTable.gate], flight[FlightsTable.terminal],
                        seat, flight[FlightsTable.seatClass], boardingGroup,
                        String.format("%03d", (1..200).random()), qrPayload,
                        now.toString(), baggage, "Active"
                    )

                    call.respond(CheckInResp(true, pass, message = "Check-in completed successfully"))
                }

                get("/{passId}") {
                    val passId = call.parameters["passId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val row = transaction {
                        BoardingPassesTable.selectAll().where { BoardingPassesTable.id eq passId }.firstOrNull()
                    }
                    if (row == null) {
                        call.respond(HttpStatusCode.NotFound, CheckInResp(false, message = "Boarding pass not found"))
                        return@get
                    }
                    call.respond(CheckInResp(true, row.toBoardingPassDto()))
                }
            }

            // ── SYNC ─────────────────────────────────────────
            post("/sync") {
                val req = call.receive<SyncReq>()
                val passes = transaction {
                    BoardingPassesTable.selectAll().where {
                        BoardingPassesTable.userId eq req.userId
                    }.map { it.toBoardingPassDto() }
                }
                call.respond(SyncResp(true, passes, timestamp = LocalDateTime.now().toString()))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  ROW MAPPERS
// ═══════════════════════════════════════════════════════════════

private fun ResultRow.toFlightDto() = FlightDto(
    this[FlightsTable.id], this[FlightsTable.bookingReference], this[FlightsTable.lastName],
    this[FlightsTable.passengerName], this[FlightsTable.flightNumber], this[FlightsTable.airlineName],
    this[FlightsTable.aircraftType], this[FlightsTable.origin], this[FlightsTable.originCity],
    this[FlightsTable.destination], this[FlightsTable.destinationCity],
    this[FlightsTable.departureTime], this[FlightsTable.arrivalTime], this[FlightsTable.duration],
    this[FlightsTable.gate], this[FlightsTable.terminal], this[FlightsTable.seatClass],
    this[FlightsTable.checkInOpen], this[FlightsTable.checkInStatus],
    this[FlightsTable.price], this[FlightsTable.currency]
)

private fun ResultRow.toBoardingPassDto() = BoardingPassDto(
    this[BoardingPassesTable.id], this[BoardingPassesTable.bookingReference],
    this[BoardingPassesTable.passengerName], this[BoardingPassesTable.flightNumber],
    this[BoardingPassesTable.airlineName], this[BoardingPassesTable.origin],
    this[BoardingPassesTable.originCity], this[BoardingPassesTable.destination],
    this[BoardingPassesTable.destinationCity], this[BoardingPassesTable.departureTime],
    this[BoardingPassesTable.arrivalTime], this[BoardingPassesTable.gate],
    this[BoardingPassesTable.terminal], this[BoardingPassesTable.seat],
    this[BoardingPassesTable.seatClass], this[BoardingPassesTable.boardingGroup],
    this[BoardingPassesTable.sequence], this[BoardingPassesTable.qrPayload],
    this[BoardingPassesTable.issuedAt].toString(), this[BoardingPassesTable.baggageInfo],
    this[BoardingPassesTable.status]
)
