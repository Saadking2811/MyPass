package com.example.myapplication.model

import kotlinx.serialization.Serializable

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
