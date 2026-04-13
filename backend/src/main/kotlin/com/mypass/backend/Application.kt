package com.mypass.backend

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8082

    embeddedServer(Netty, port = port) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        install(CORS) {
            anyHost()
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
        }

        install(StatusPages) {
            exception<IllegalArgumentException> { call, cause ->
                call.respond(HttpStatusCode.BadRequest, mapOf("success" to false, "message" to (cause.message ?: "Bad request")))
            }
            exception<Throwable> { call, cause ->
                call.application.environment.log.error("Unhandled error", cause)
                call.respond(HttpStatusCode.InternalServerError, mapOf("success" to false, "message" to "Internal server error"))
            }
        }

        DatabaseFactory.init()
        configureRoutes()

        environment.log.info("MyPass backend running on port $port")
        environment.log.info("PostgreSQL connected successfully")
    }.start(wait = true)
}
