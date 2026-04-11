package com.example.flightbooking.screens.ProfileScreen

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flightbooking.navigation.BottomNavigation
import com.example.flightbooking.navigation.FlightScreens
import com.example.flightbooking.ui.theme.PrimaryColor2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun BookingScreen(navController: NavController) {
    val state = rememberScrollState()
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    var name by remember {
        mutableStateOf("...")
    }

    var phoneNumber by remember {
        mutableStateOf("...")
    }



    db.collection("user")
        .whereEqualTo("email", user?.email.toString())
        .get()
        .addOnSuccessListener { documents ->
            for (document in documents) {
                name = document.data["displayName"].toString()
                phoneNumber = document.data["phoneNumber"].toString()
                Log.d("test2", "${name}/${phoneNumber}")
            }
        }
        .addOnFailureListener { exception ->
            Log.w(TAG, "Error getting documents: ", exception)
        }

    Scaffold(
        bottomBar = { BottomNavigation(navController) },
    ) { paddingValues ->
        Column(
            modifier =
            Modifier
                .padding(paddingValues)
                .fillMaxHeight()
                .fillMaxWidth()
                .verticalScroll(state)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(bottomStartPercent = 9, bottomEndPercent = 9)
                ),
            verticalArrangement = Arrangement.Center
        ) {

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 16.dp)
            ) {

                Text(
                    text = "Account Information",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    style = MaterialTheme.typography.displaySmall.copy(
                        textAlign = TextAlign.Center
                    ),

                    )
                UserDetails("Name", name)
                UserDetails("Email",user?.email.toString())
                UserDetails("Phone Number",phoneNumber)


                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 50.dp, vertical = 20.dp)
                        .height(50.dp)
                        .background(color = PrimaryColor2,shape = RoundedCornerShape(10.dp))
                        .clickable(onClick = {
                            Log.d("logout", "logout")
                            FirebaseAuth.getInstance().signOut().run {
                                navController.navigate(FlightScreens.LoginScreen.name)
                            }}),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(text="Logout", style=MaterialTheme.typography.titleLarge.copy(
                        color = Color.White
                    ))
                    Icon(imageVector = Icons.Default.Logout, contentDescription = "" , tint = Color.White)
                }


            }


        }

    }

}

@Composable
private fun UserDetails(label:String,text: String) {
    Column {
        HorizontalDivider(thickness = 1.dp, color = PrimaryColor2)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 40.dp, vertical = 5.dp,
                ),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "${label}:", modifier = Modifier.padding(3.dp))
            Text(text = text , modifier = Modifier.padding(3.dp))
        }
    }
}