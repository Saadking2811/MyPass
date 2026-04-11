package com.example.flightbooking.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flightbooking.ui.theme.PrimaryColor

@Composable
fun HotelScreen(navController: NavController){
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Column (  modifier = Modifier.padding(vertical = 16.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "",
                modifier = Modifier.size(30.dp),
                tint = PrimaryColor
            )
            Text(
                text = "Where would",
                style = MaterialTheme.typography.headlineMedium
                    .copy(color = PrimaryColor, fontWeight = FontWeight.Bold)
                )
            Text(
                text = "you like to stay ?",
                style = MaterialTheme.typography.headlineMedium
                    .copy(color = PrimaryColor, fontWeight = FontWeight.Bold),

            )

        }
    }
}