package com.example.flightbooking.navigation

import com.example.flightbooking.screens.SplashScreen

enum class FlightScreens {
    HomeScreen,
    BookingScreen,
    CheckoutScreen,
    HotelScreen,
    LoginScreen,
    OnboardScreen,
    SignUpScreen,
    SplashScreen,
    DetailScreen,
    TicketScreen;
    companion object {
        fun fromRoute(route: String?): FlightScreens =
            when (route?.substringBefore("/")){
                HomeScreen.name -> HomeScreen
                DetailScreen.name -> DetailScreen
                BookingScreen.name -> BookingScreen
                CheckoutScreen.name -> CheckoutScreen
                HotelScreen.name -> HotelScreen
                LoginScreen.name -> LoginScreen
                OnboardScreen.name -> OnboardScreen
                SignUpScreen.name -> SignUpScreen
                SplashScreen.name -> SplashScreen
                TicketScreen.name -> TicketScreen
            null -> HomeScreen
                else -> throw IllegalArgumentException("Route $route is not found" )
            }
    }
}