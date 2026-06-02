package com.example.myapplication.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.PreferencesManager
import com.example.myapplication.ui.auth.SignInScreen
import com.example.myapplication.ui.auth.SignUpScreen
import com.example.myapplication.ui.checkin.BaggageScreen
import com.example.myapplication.ui.checkin.BoardingPassScreen
import com.example.myapplication.ui.checkin.BookingLookupScreen
import com.example.myapplication.ui.checkin.DetailsReviewScreen
import com.example.myapplication.ui.checkin.PassportScanScreen
import com.example.myapplication.ui.checkin.SeatSelectionScreen
import com.example.myapplication.ui.checkin.SpecialRequestsScreen
import com.example.myapplication.ui.main.MainScaffold
import com.example.myapplication.ui.preferences.PreferencesScreen
import com.example.myapplication.ui.splash.SplashScreen
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.viewmodel.AppViewModel

/** Named NavController destinations for the entire app. */
private object Routes {
    const val SPLASH      = "splash"
    const val SIGN_IN     = "signin"
    const val SIGN_UP     = "signup"
    const val MAIN        = "main"
    const val LOOKUP      = "lookup"
    const val PASSPORT    = "passport"
    const val REVIEW      = "review"
    const val SEAT        = "seat"
    const val BAGGAGE     = "baggage"
    const val REQUESTS    = "requests"
    const val PASS        = "pass"
    const val PREFERENCES = "preferences"
}

/**
 * Root composable for the entire app — owns the [AppViewModel], the snackbar host,
 * and the [NavHost] that routes between every screen.
 *
 * Screens themselves live under feature packages: `ui/auth`, `ui/main`, `ui/home`,
 * `ui/trips`, `ui/explore`, `ui/profile`, `ui/checkin`, `ui/preferences`, `ui/splash`.
 */
@Composable
fun AirlineCheckInApp(
    preferencesManager: PreferencesManager? = null,
    isDarkMode: Boolean = false,
    currentLanguage: String = "en"
) {
    val navController = rememberNavController()
    val vm: AppViewModel = viewModel()
    val state by vm.uiState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    val lang = remember(currentLanguage) {
        when (currentLanguage) {
            "fr" -> AppStrings.Lang.FR
            "ar" -> AppStrings.Lang.AR
            else -> AppStrings.Lang.EN
        }
    }
    val s = { key: String -> AppStrings.get(key, lang) }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { snackbarHost.showSnackbar(it); vm.clearMessage() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost   = { SnackbarHost(hostState = snackbarHost) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController      = navController,
                startDestination   = Routes.SPLASH,
                modifier           = Modifier.padding(innerPadding),
                enterTransition    = { fadeIn(tween(280)) + slideInHorizontally(tween(280)) { it / 6 } },
                exitTransition     = { fadeOut(tween(220)) + slideOutHorizontally(tween(220)) { -it / 8 } },
                popEnterTransition = { fadeIn(tween(280)) + slideInHorizontally(tween(280)) { -it / 6 } },
                popExitTransition  = { fadeOut(tween(220)) + slideOutHorizontally(tween(220)) { it / 8 } }
            ) {
                composable(Routes.SPLASH, enterTransition = { EnterTransition.None }) {
                    SplashScreen {
                        navController.navigate(
                            if (state.currentUser != null) Routes.MAIN else Routes.SIGN_IN
                        ) { popUpTo(Routes.SPLASH) { inclusive = true } }
                    }
                }
                composable(Routes.SIGN_IN) {
                    SignInScreen(
                        state, vm,
                        onNavigateToHome = {
                            navController.navigate(Routes.MAIN) {
                                popUpTo(Routes.SIGN_IN) { inclusive = true }
                            }
                        },
                        onNavigateToSignUp = { navController.navigate(Routes.SIGN_UP) }
                    )
                }
                composable(Routes.SIGN_UP) {
                    SignUpScreen(
                        state, vm,
                        onNavigateToHome = {
                            navController.navigate(Routes.MAIN) { popUpTo(0) { inclusive = true } }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.MAIN) {
                    MainScaffold(
                        state = state, vm = vm, s = s,
                        onStartCheckIn = { navController.navigate(Routes.LOOKUP) },
                        onOpenPreferences = { navController.navigate(Routes.PREFERENCES) },
                        onLogout = {
                            vm.logout()
                            navController.navigate(Routes.SIGN_IN) { popUpTo(0) { inclusive = true } }
                        }
                    )
                }
                composable(Routes.LOOKUP) {
                    BookingLookupScreen(
                        state, vm,
                        onNext = { navController.navigate(Routes.PASSPORT) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.PASSPORT) {
                    PassportScanScreen(
                        vm, state,
                        onPassportScanned = { navController.navigate(Routes.REVIEW) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.REVIEW) {
                    DetailsReviewScreen(
                        state,
                        onNext = { navController.navigate(Routes.SEAT) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.SEAT) {
                    SeatSelectionScreen(
                        state, vm::updateSeat,
                        onNext = { navController.navigate(Routes.BAGGAGE) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.BAGGAGE) {
                    BaggageScreen(
                        state, vm::updateBaggage,
                        onNext = { navController.navigate(Routes.REQUESTS) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.REQUESTS) {
                    SpecialRequestsScreen(
                        state, vm::updateSpecialRequests,
                        onFinish = { vm.completeCheckIn(); navController.navigate(Routes.PASS) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.PASS) {
                    BoardingPassScreen(
                        state, vm, s,
                        onNewLookup = {
                            vm.resetForNewCheckIn()
                            navController.navigate(Routes.MAIN) { popUpTo(Routes.MAIN) { inclusive = true } }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.PREFERENCES) {
                    PreferencesScreen(preferencesManager, isDarkMode, currentLanguage, s) { navController.popBackStack() }
                }
            }

            if (state.isSyncing) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape    = RoundedCornerShape(20.dp),
                        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation= CardDefaults.cardElevation(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(color = EmeraldGreen, strokeWidth = 2.5.dp, modifier = Modifier.size(28.dp))
                            Text("Syncing your data...", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
