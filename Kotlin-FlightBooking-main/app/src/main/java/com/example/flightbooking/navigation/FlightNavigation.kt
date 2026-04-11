package com.example.flightbooking.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.flightbooking.screens.ProfileScreen.BookingScreen
import com.example.flightbooking.screens.CheckoutScreen
import com.example.flightbooking.screens.HomeScreen.HomeScreen
import com.example.flightbooking.screens.HotelScreen
import com.example.flightbooking.screens.authScreens.LoginScreen
import com.example.flightbooking.screens.OnBoardScreen
import com.example.flightbooking.screens.TicketScreen
import com.example.flightbooking.screens.authScreens.SignUpScreen

@Composable
fun FlightNavigation(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = FlightScreens.OnboardScreen.name) {
        composable(FlightScreens.BookingScreen.name) { BookingScreen(navController) }
        composable(FlightScreens.HomeScreen.name) { HomeScreen(navController) }
        composable(FlightScreens.OnboardScreen.name) { OnBoardScreen(navController) }
        composable(FlightScreens.SignUpScreen.name) { SignUpScreen(navController) }
        composable(FlightScreens.LoginScreen.name) { LoginScreen(navController) }
        composable(FlightScreens.HotelScreen.name) { HotelScreen(navController) }
        composable(FlightScreens.CheckoutScreen.name) { CheckoutScreen(navController) }
        composable(FlightScreens.TicketScreen.name) { TicketScreen(navController) }

        // Add more destinations similarly.
    }
}