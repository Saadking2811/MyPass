package com.example.myapplication.repository

import com.example.myapplication.model.UserAccount
import com.example.myapplication.network.GoogleAuthRequest
import com.example.myapplication.network.LoginRequest
import com.example.myapplication.network.RegisterRequest
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.UpdateProfileRequest

/** Authentication endpoints — register, login, Google sign-in, session restore, profile update. */
class AuthRepository {

    private val api get() = RetrofitClient.apiService

    suspend fun register(fullName: String, email: String, phone: String, password: String): Result<UserAccount> = runCatching {
        val response = api.register(RegisterRequest(fullName, email, phone, password))
        val body = response.body()
        if (!response.isSuccessful || body?.success != true || body.user == null) {
            throw Exception(body?.message ?: "Registration failed (HTTP ${response.code()})")
        }
        RetrofitClient.setAuthToken(body.token)
        RetrofitClient.setUserId(body.user.id)
        body.user
    }.recoverCatching { e ->
        throw Exception("Cannot reach backend at ${RetrofitClient.getBaseUrl()}. ${e.message ?: ""}".trim())
    }

    suspend fun login(email: String, password: String): Result<UserAccount> = runCatching {
        val response = api.login(LoginRequest(email, password))
        val body = response.body()
        if (!response.isSuccessful || body?.success != true || body.user == null) {
            throw Exception(body?.message ?: "Invalid credentials")
        }
        RetrofitClient.setAuthToken(body.token)
        RetrofitClient.setUserId(body.user.id)
        body.user
    }.recoverCatching { e ->
        throw Exception(e.message ?: "Cannot reach backend at ${RetrofitClient.getBaseUrl()}")
    }

    suspend fun googleSignIn(displayName: String, email: String): Result<UserAccount> = runCatching {
        val response = api.googleSignIn(GoogleAuthRequest("mock_token", email, displayName))
        val body = response.body()
        if (!response.isSuccessful || body?.success != true || body.user == null) {
            throw Exception(body?.message ?: "Google sign-in failed")
        }
        RetrofitClient.setAuthToken(body.token)
        RetrofitClient.setUserId(body.user.id)
        body.user
    }.recoverCatching { e ->
        throw Exception("Cannot reach backend at ${RetrofitClient.getBaseUrl()}. ${e.message ?: ""}".trim())
    }

    /** Restores the user session using the stored Bearer token. Returns null if expired or missing. */
    suspend fun restoreSession(): Result<UserAccount?> = runCatching {
        if (RetrofitClient.getUserId().isBlank()) return@runCatching null
        val response = api.whoami()
        if (response.code() == 401) {
            RetrofitClient.setAuthToken(null)
            RetrofitClient.setUserId(null)
            return@runCatching null
        }
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) return@runCatching null
        body.user
    }

    suspend fun updateProfile(userId: String, fullName: String? = null, phone: String? = null): Result<UserAccount> = runCatching {
        val response = api.updateUser(userId, UpdateProfileRequest(fullName = fullName, phone = phone))
        val body = response.body()
        if (!response.isSuccessful || body?.success != true || body.user == null) {
            throw Exception(body?.message ?: "Profile update failed (HTTP ${response.code()})")
        }
        body.user
    }

    fun logout() {
        RetrofitClient.setAuthToken(null)
        RetrofitClient.setUserId(null)
    }
}
