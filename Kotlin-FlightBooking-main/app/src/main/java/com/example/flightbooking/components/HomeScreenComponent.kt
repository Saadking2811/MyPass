package com.example.flightbooking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.flightbooking.ui.theme.PrimaryColor

@Composable
fun FlightLocation(countryAb: String, country: String, onClick: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable{onClick()}
    ) {
        Text(
            text = countryAb, style = MaterialTheme.typography.titleLarge
        )
        Text(text = country, style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp)
                .width(100.dp)

        )
    }
}

@Composable
fun FlightDate(title: String, date: String) {
    Column(
        modifier = Modifier.background(color = Color.Transparent)
            .height(90.dp),
//            .border(width = 1.dp, color = PrimaryColor, shape = RoundedCornerShape(10.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Icon(imageVector = Icons.Default.DateRange,
            contentDescription = "" )
        Text(text=title, style= MaterialTheme.typography.labelLarge)
        Text(text=date, style= MaterialTheme.typography.labelLarge
            .copy(
                fontWeight = FontWeight.Bold
            ))
    }
}