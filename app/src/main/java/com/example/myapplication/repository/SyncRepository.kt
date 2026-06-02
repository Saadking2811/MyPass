package com.example.myapplication.repository

import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.network.SyncRequest
import com.example.myapplication.network.SyncResponse

/** Pulls the user's flights and boarding passes in a single call (auto-sync). */
class SyncRepository {

    private val api get() = RetrofitClient.apiService

    suspend fun synchronize(userId: String): Result<SyncResponse> = runCatching {
        val response = api.syncData(SyncRequest(userId))
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            throw Exception(body?.message ?: "Sync failed")
        }
        body
    }
}
