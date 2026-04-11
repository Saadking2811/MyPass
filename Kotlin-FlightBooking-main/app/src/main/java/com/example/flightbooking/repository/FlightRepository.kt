package com.example.flightbooking.repository

import android.content.ContentValues
import android.util.Log
import com.example.flightbooking.data.DataOrException
import com.example.flightbooking.model.Data
import com.example.flightbooking.model.MFlights
import com.example.flightbooking.network.FlightApi
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firestore.v1.FirestoreGrpc.FirestoreStub
import javax.inject.Inject

class FlightRepository @Inject constructor(private val api: FlightApi) {
    private val dataOrException = DataOrException<List<Data>, Boolean, Exception>()
    suspend fun getFlights(): DataOrException<List<Data>, Boolean, Exception> {
        try {
            dataOrException.loading = true
            dataOrException.data = api.getAllFlight().data
            if (dataOrException.data!!.isNotEmpty()) dataOrException.loading = false


        } catch (e: Exception) {
            dataOrException.e = e
//            val db = FirebaseFirestore.getInstance()
//
//            db.collection("flights")
//                .document("saved")
//                .get()
//                .addOnSuccessListener { documents ->
//                    Log.d("firebase suc", documents.data.toString())
//
//                }
//                .addOnFailureListener { exception ->
//                    Log.d( "firebase err", "Error getting documents: ${exception.message}")
//                }
        }

        return dataOrException
    }



}