package com.example.flightbooking.screens.ProfileScreen

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class ProfileScreenViewModel : ViewModel() {
    private val user = Firebase.auth.currentUser
    private val db = FirebaseFirestore.getInstance()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading



    val items = mutableMapOf("Box" to 12, "Books" to 18, "Table" to 13)

    fun userDetails() = viewModelScope.launch {
        db.collection("user")
            .whereEqualTo("email", user?.email.toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                  val name = document.data["displayName"].toString()
                  val  phoneNumber = document.data["phoneNumber"].toString()
                    val email = user?.email.toString()
//                    return "test"
                    Log.d("test2", "${name}/${phoneNumber}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }
}