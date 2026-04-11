package com.example.flightbooking.network

import com.example.flightbooking.model.Arrival
import com.example.flightbooking.model.Data
import com.example.flightbooking.model.MFlights
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Singleton


@Singleton
interface FlightApi {
    @GET("flights?access_key=78bc165bd352143122cd940a3cdd14d6")

//    http://api.aviationstack.com/v1/flights?access_key=78bc165bd352143122cd940a3cdd14d6
    suspend fun getAllFlight():MFlights
}