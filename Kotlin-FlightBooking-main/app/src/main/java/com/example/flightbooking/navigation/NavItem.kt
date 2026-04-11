package com.example.flightbooking.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AirplaneTicket
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AirplaneTicket
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(val title: String,
                   val selectedIcon: ImageVector,
                   val unselectedIcon: ImageVector,
                   val badgeCount: Int? = null,
                   val route: String = "")

val listOfNavItems : List<NavItem> = listOf(
    NavItem(
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        route = FlightScreens.HomeScreen.name
    ),
    NavItem(
        title = "Profile",
        selectedIcon = Icons.Default.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle,
        route = FlightScreens.BookingScreen.name
    ),
    NavItem(
        title = "Tickets",
        selectedIcon = Icons.Filled.AirplaneTicket,
        unselectedIcon = Icons.Outlined.AirplaneTicket,
        route = FlightScreens.TicketScreen.name
    ),
//    NavItem(
//        title = "Profile",
//        selectedIcon = Icons.Filled.AccountCircle,
//        unselectedIcon = Icons.Outlined.AccountCircle,
//        route = Screens.ProfileScreen.name
//    )
)
