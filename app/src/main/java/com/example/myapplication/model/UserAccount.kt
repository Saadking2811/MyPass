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
