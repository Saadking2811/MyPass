package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.model.BoardingPass
import com.example.myapplication.model.FlightItinerary
import com.example.myapplication.model.OfflineCache
import com.example.myapplication.model.UserAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

/**
 * Lightweight offline cache for boarding passes, flights and the logged-in user.
 * Backs offline access and post-reconnect synchronization as required by the spec.
 *
 * Single DataStore preference holds a serialized OfflineCache blob.
 */
private val Context.offlineDataStore: DataStore<Preferences> by preferencesDataStore(name = "mypass_offline_cache")

class OfflineCacheManager(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = false }
    private val key  = stringPreferencesKey("cache_blob")

    val cache: Flow<OfflineCache> = context.offlineDataStore.data.map { prefs ->
        val blob = prefs[key]
        if (blob.isNullOrBlank()) OfflineCache()
        else runCatching { json.decodeFromString<OfflineCache>(blob) }.getOrElse { OfflineCache() }
    }

    suspend fun current(): OfflineCache = cache.first()

    suspend fun saveUser(user: UserAccount?) {
        val cur = current()
        persist(cur.copy(user = user))
    }

    suspend fun saveBoardingPasses(passes: List<BoardingPass>) {
        val cur = current()
        persist(cur.copy(boardingPasses = passes, lastSyncTimestamp = Instant.now().toString()))
    }

    suspend fun saveFlights(flights: List<FlightItinerary>) {
        val cur = current()
        persist(cur.copy(flights = flights, lastSyncTimestamp = Instant.now().toString()))
    }

    /** Atomic full update — used when sync returns both. */
    suspend fun saveAll(user: UserAccount?, passes: List<BoardingPass>, flights: List<FlightItinerary>) {
        persist(OfflineCache(
            user = user,
            boardingPasses = passes,
            flights = flights,
            lastSyncTimestamp = Instant.now().toString()
        ))
    }

    suspend fun clear() {
        context.offlineDataStore.edit { it.remove(key) }
    }

    private suspend fun persist(c: OfflineCache) {
        context.offlineDataStore.edit { it[key] = json.encodeToString(c) }
    }
}
