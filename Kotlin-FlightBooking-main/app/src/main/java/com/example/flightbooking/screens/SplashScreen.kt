package com.example.flightbooking.screens

import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.flightbooking.R

@Composable
fun SplashScreen(){
    Image( painter = painterResource(id = R.drawable.welcome_screen),
        contentDescription = "",
        contentScale = ContentScale.FillBounds)
}