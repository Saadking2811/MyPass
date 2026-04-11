package com.example.flightbooking.screens.HomeScreen

import android.nfc.Tag
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightbooking.data.DataOrException
import com.example.flightbooking.model.Data
import com.example.flightbooking.model.MFlights
import com.example.flightbooking.repository.FlightRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: FlightRepository) : ViewModel() {
    var listOfFlights: MutableState<DataOrException<List<Data>, Boolean, Exception>> =
        mutableStateOf(DataOrException(null, true, Exception("")))

    init {
getFlights()
    }

     fun getFlights() {
        viewModelScope.launch(Dispatchers.IO) {

            listOfFlights.value = repository.getFlights()
            listOfFlights.value.loading = true
Log.d("tag", "get flights: ${listOfFlights.value}")

            Log.d("api worked1", "get flights: ${listOfFlights.value.e.toString()}")
            if (listOfFlights.value.data?.isNotEmpty() == true) {
                Log.d("api worked", "get flights: ${listOfFlights.value.data.toString()}")
                listOfFlights.value.loading = false
                listOfFlights.value.data?.let {
                    saveFlightsFirebase(it) }
            }

            listOfFlights.value.loading = false

//           else if(listOfFlights.value.e.toString().isNotEmpty()){
//                Log.d("fetching from fb", "get flights: ${listOfFlights.value.e?.message}")
//
//                val db = FirebaseFirestore.getInstance()
//
//                db.collection("flights")
//                    .document("saved")
//                    .get()
//                    .addOnSuccessListener { documents ->
//
//                        val flightData = documents.data?.get("flightData")
//
//                        val myDataObjects = flightDataToDataObjects(flightData as List<Map<String, Any>>?)
//                        listOfFlights.value.loading = false
//                        Log.d("firebase suc", documents.data.toString())
//
//
//
//
//                    }
//                    .addOnFailureListener { exception ->
//                        Log.d( "firebase err", "Error getting documents: ${exception.message}")
//                    }
//            }
        }
    }

    private  fun saveFlightsFirebase(flightData: List<Data>) {
val data: MutableMap<String, Any> = mutableMapOf("data" to flightData)
        Log.d("firebase call", "called firebase")
        FirebaseFirestore.getInstance()
            .collection("flights")
            .document("saved")
            .set(data)
            .addOnSuccessListener { Log.d("firbase s", "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w("firebase e", "Error writing document", e) }



    }
}

fun flightDataToDataObjects(fbData: List<Map<String, Any?>>?) : List<Data> {
    val myData = mutableListOf<Data>()

//    fbData.map

    return myData
}