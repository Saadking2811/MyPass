package com.example.myapplication.network

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Backend connector. Reads the user-configured backend URL & auth token
 * from SharedPreferences. The URL/token can be updated at runtime.
 */
object RetrofitClient {

    // Default fallback URL — only used if user hasn't configured anything
    // 10.0.2.2 = special address that points to host machine from Android emulator
    const val DEFAULT_BASE_URL = "http://10.0.2.2:8082/api/"

    private const val PREF_NAME       = "mypass_network"
    private const val PREF_BASE_URL   = "backend_base_url"
    private const val PREF_AUTH_TOKEN = "auth_token"
    private const val PREF_USER_ID    = "user_id"

    @Volatile private var prefs: SharedPreferences? = null
    @Volatile private var cachedRetrofit: Retrofit? = null
    @Volatile private var cachedBaseUrl: String? = null

    /** Must be called once from MainActivity / Application before first use. */
    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    fun getBaseUrl(): String =
        prefs?.getString(PREF_BASE_URL, null)?.takeIf { it.isNotBlank() } ?: DEFAULT_BASE_URL

    fun setBaseUrl(url: String) {
        val cleaned = url.trim().let { if (it.endsWith("/")) it else "$it/" }
        prefs?.edit()?.putString(PREF_BASE_URL, cleaned)?.apply()
        // Force the next apiService access to rebuild
        cachedRetrofit = null
        cachedBaseUrl = null
    }

    fun setAuthToken(token: String?) {
        val editor = prefs?.edit() ?: return
        if (token.isNullOrBlank()) editor.remove(PREF_AUTH_TOKEN) else editor.putString(PREF_AUTH_TOKEN, token)
        editor.apply()
    }

    fun setUserId(userId: String?) {
        val editor = prefs?.edit() ?: return
        if (userId.isNullOrBlank()) editor.remove(PREF_USER_ID) else editor.putString(PREF_USER_ID, userId)
        editor.apply()
    }

    fun getUserId(): String = prefs?.getString(PREF_USER_ID, null).orEmpty()

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token  = prefs?.getString(PREF_AUTH_TOKEN, null).orEmpty()
        val userId = prefs?.getString(PREF_USER_ID, null).orEmpty()
        val builder = original.newBuilder()
        if (token.isNotBlank())  builder.header("Authorization", "Bearer $token")
        if (userId.isNotBlank()) builder.header("X-User-Id", userId)
        chain.proceed(builder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService
        get() {
            val current = getBaseUrl()
            val existing = cachedRetrofit
            if (existing != null && cachedBaseUrl == current) {
                return existing.create(ApiService::class.java)
            }
            val newRetrofit = Retrofit.Builder()
                .baseUrl(current)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            cachedRetrofit = newRetrofit
            cachedBaseUrl  = current
            return newRetrofit.create(ApiService::class.java)
        }

    /** Quick health probe — returns null on success, error message on failure. */
    suspend fun probe(): String? = try {
        val resp = apiService.healthCheck()
        if (resp.isSuccessful) null else "Server returned ${resp.code()}"
    } catch (e: Exception) {
        e.message ?: e.javaClass.simpleName
    }
}
