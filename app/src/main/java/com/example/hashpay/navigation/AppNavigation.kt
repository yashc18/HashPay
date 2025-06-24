package com.example.hashpay.navigation
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import android.util.Log
import com.example.hashpay.ui.components.BottomNavigationBar
import com.example.hashpay.ui.screens.HomeScreen
import com.example.hashpay.ui.screens.QRScannerScreen
import com.example.hashpay.ui.screens.SendMoneyScreen
import com.example.hashpay.ui.screens.TransactionHistoryScreen
import com.example.hashpay.ui.viewmodels.SendMoneyViewModel
import com.example.hashpay.ui.viewmodels.TransactionHistoryViewModel

object NavDestinations {
    const val HOME = "home"
    const val HISTORY = "history"
    const val QR_SCANNER = "qr_scanner"
    const val WITHDRAW = "withdraw"
    const val PROFILE = "profile"
    const val SEND_MONEY = "send_money"
}

/**
 * Main navigation component for the app
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: ""

    LaunchedEffect(currentRoute) {
        Log.d("Navigation", "Current route: $currentRoute")
    }

    Scaffold(
        bottomBar = {
            // Only show the BottomNavigationBar if we're not on the "send_money" screen
            if (currentRoute != NavDestinations.SEND_MONEY) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * NavHost for the app
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = NavDestinations.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(NavDestinations.HOME) {
            HomeScreen { route -> navController.navigate(route) }
        }

        composable(NavDestinations.QR_SCANNER) {
            QRScannerScreen { navController.popBackStack() }
        }

        composable(NavDestinations.HISTORY) {
            val context = LocalContext.current
            val historyViewModel: TransactionHistoryViewModel = viewModel(
                factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as android.app.Application)
            )
            TransactionHistoryScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = historyViewModel
            )
        }

        composable(NavDestinations.SEND_MONEY) {
            val context = LocalContext.current
            val factory = object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return SendMoneyViewModel(context) as T
                }
            }
            val viewModel: SendMoneyViewModel = viewModel(factory = factory)
            SendMoneyScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onScanQrCode = { navController.navigate(NavDestinations.QR_SCANNER) }
            )
        }


        composable(NavDestinations.WITHDRAW) {
            // TODO: Implement WithdrawScreen
        }

        composable(NavDestinations.PROFILE) {
            // TODO: Implement ProfileScreen
        }
    }
}

/**
 * Extension function to navigate with options
 */
fun NavController.navigateWithOptions(
    route: String,
    popUpToRoute: String? = null,
    inclusive: Boolean = false,
    singleTop: Boolean = true
) {
    navigate(route) {
        if (popUpToRoute != null) {
            popUpTo(popUpToRoute) { this.inclusive = inclusive }
        }
        launchSingleTop = singleTop
        restoreState = true
    }
}