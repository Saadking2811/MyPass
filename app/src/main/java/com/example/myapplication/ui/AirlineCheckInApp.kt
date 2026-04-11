package com.example.myapplication.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.components.LoadingOverlay
import com.example.myapplication.ui.screens.*

private object Routes {
    const val AUTH = "auth"
    const val HOME = "home"
    const val PASSPORT = "passport"
    const val REVIEW = "review"
    const val SEAT = "seat"
    const val BAGGAGE = "baggage"
    const val REQUESTS = "requests"
    const val PASS = "pass"
}

@Composable
fun AirlineCheckInApp() {
    val navController = rememberNavController()
    val vm: AppViewModel = viewModel()
    val state by vm.uiState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let {
            snackbarHost.showSnackbar(it)
            vm.clearMessage()
        }
    }

    // Navigate to home when auth succeeds
    LaunchedEffect(state.currentUser) {
        if (state.currentUser != null) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute == Routes.AUTH) {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.AUTH) { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHost) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = if (state.currentUser == null) Routes.AUTH else Routes.HOME,
                enterTransition = { fadeIn(tween(300)) + slideInVertically { it / 6 } },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(300)) + slideInVertically { -it / 6 } },
                popExitTransition = { fadeOut(tween(200)) + slideOutVertically { it / 6 } }
            ) {
                composable(Routes.AUTH) {
                    AuthScreen(
                        isLoginMode = state.isAuthMode,
                        isLoading = state.isLoading,
                        onToggleMode = vm::toggleAuthMode,
                        onRegister = vm::register,
                        onLogin = vm::login,
                        onGoogleSignIn = vm::signInWithGoogle
                    )
                }
                composable(Routes.HOME) {
                    HomeScreen(
                        state = state,
                        onLookup = vm::lookupFlight,
                        onSync = vm::synchronize,
                        onContinue = {
                            if (state.lookupResult != null) {
                                vm.setCheckInStep(0)
                                navController.navigate(Routes.PASSPORT)
                            }
                        },
                        onBoardingPassClick = { navController.navigate(Routes.PASS) },
                        onLogout = {
                            vm.logout()
                            navController.navigate(Routes.AUTH) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.PASSPORT) {
                    PassportScanScreen(
                        state = state,
                        vm = vm,
                        onNext = {
                            vm.setCheckInStep(1)
                            navController.navigate(Routes.REVIEW)
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.REVIEW) {
                    DetailsReviewScreen(
                        state = state,
                        onNext = {
                            vm.setCheckInStep(2)
                            navController.navigate(Routes.SEAT)
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.SEAT) {
                    SeatSelectionScreen(
                        state = state,
                        onSeatSelected = vm::updateSeat,
                        onNext = {
                            vm.setCheckInStep(3)
                            navController.navigate(Routes.BAGGAGE)
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.BAGGAGE) {
                    BaggageScreen(
                        state = state,
                        onUpdate = vm::updateBaggage,
                        onNext = {
                            vm.setCheckInStep(4)
                            navController.navigate(Routes.REQUESTS)
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.REQUESTS) {
                    SpecialRequestsScreen(
                        state = state,
                        onUpdate = vm::updateSpecialRequests,
                        onFinish = {
                            vm.completeCheckIn()
                            navController.navigate(Routes.PASS) {
                                popUpTo(Routes.HOME)
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.PASS) {
                    BoardingPassScreen(
                        state = state,
                        vm = vm,
                        onNewCheckIn = {
                            vm.resetForNewCheckIn()
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        },
                        onHome = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        }
                    )
                }
            }

            LoadingOverlay(isLoading = state.isSyncing)
        }
    }
}
