package com.example.flightbooking.di

import com.example.flightbooking.network.FlightApi
import com.example.flightbooking.repository.FlightRepository
import com.example.flightbooking.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providesFlightRepository(api: FlightApi) = FlightRepository(api)

    @Singleton
    @Provides
    fun providesFlightApi(): FlightApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FlightApi::class.java)
    }
}