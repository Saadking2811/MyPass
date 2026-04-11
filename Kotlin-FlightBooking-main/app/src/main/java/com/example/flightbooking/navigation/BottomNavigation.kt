package com.example.flightbooking.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigation(navController: NavController){
//    val navController : NavHostController = rememberNavController()
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        var selectedItem by remember { mutableIntStateOf(0) }

        listOfNavItems.forEachIndexed { index, item ->
            NavigationBarItem(
                label = { Text(item.title) },
                selected = currentDestination?.hierarchy?.any{ it.route == item.route } == true,
                icon = {
                    Icon(imageVector = if(selectedItem == index) {
                        item.selectedIcon
                    } else {
                        item.unselectedIcon
                    },
                        contentDescription = item.title)
                },
                onClick = {
                    selectedItem = index
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}