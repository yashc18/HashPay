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
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.hashpay.WalletConnectionManager
import com.example.hashpay.ui.components.BottomNavigationBar
import com.example.hashpay.ui.screens.*
import com.example.hashpay.ui.screens.invoice.CreateInvoiceScreen
import com.example.hashpay.ui.screens.invoice.InvoiceDetailScreen
import com.example.hashpay.ui.screens.invoice.InvoiceListScreen
import com.example.hashpay.ui.viewmodels.ProfileViewModel
import com.example.hashpay.ui.viewmodels.SendMoneyViewModel
import com.example.hashpay.ui.viewmodels.TransactionHistoryViewModel

object NavDestinations {
    const val HOME = "home"
    const val HISTORY = "history"
    const val QR_SCANNER = "qr_scanner"
    const val WITHDRAW = "withdraw"
    const val PROFILE = "profile"
    const val SEND_MONEY = "send_money"
    const val INVOICE = "invoice"
    const val CREATE_INVOICE = "create_invoice"
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
            if (currentRoute != NavDestinations.SEND_MONEY && currentRoute != NavDestinations.CREATE_INVOICE) {
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

        composable(NavDestinations.PROFILE) {
            // Create ProfileViewModel
            val profileViewModel: ProfileViewModel = viewModel()

            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onViewTransactions = {
                    navController.navigate(NavDestinations.HISTORY)
                },
                onLogout = {
                    // Handle logout - navigate back to home
                    navController.navigate(NavDestinations.HOME) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                },
                viewModel = profileViewModel
            )
        }

        composable(NavDestinations.INVOICE) {
            val context = LocalContext.current
            val walletConnectionManager = WalletConnectionManager.getInstance(context)
            val walletAddress = walletConnectionManager.walletAddress.collectAsState().value

            InvoiceListScreen(
                onCreateInvoice = { navController.navigate(NavDestinations.CREATE_INVOICE) },
                onInvoiceClick = { invoiceId ->
                    navController.navigate("invoice_detail/$invoiceId")
                },
                walletAddress = walletAddress
            )
        }

        composable(NavDestinations.CREATE_INVOICE) {
            val context = LocalContext.current
            val walletConnectionManager = WalletConnectionManager.getInstance(context)
            val walletAddress = walletConnectionManager.walletAddress.collectAsState().value

            CreateInvoiceScreen(
                onNavigateBack = { navController.popBackStack() },
                onContactSelect = { callback -> /* TODO: Implement contact selection */ },
                walletAddress = walletAddress
            )
        }

        composable(
            route = "invoice_detail/{invoiceId}",
            arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
            val context = LocalContext.current
            val walletConnectionManager = WalletConnectionManager.getInstance(context)
            val walletAddress = walletConnectionManager.walletAddress.collectAsState().value

            InvoiceDetailScreen(
                invoiceId = invoiceId,
                onNavigateBack = { navController.popBackStack() },
                onPayInvoice = { id, amount, receiverAddress ->
                    // Handle payment navigation
                },
                walletAddress = walletAddress // Use real wallet address
            )
        }

        composable(NavDestinations.WITHDRAW) {
            // TODO: Implement WithdrawScreen
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