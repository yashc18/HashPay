package com.example.hashpay

import com.example.hashpay.ui.components.BottomNavigationBar
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.hashpay.ui.screens.HomeScreen
import com.example.hashpay.ui.screens.QRScannerScreen
import com.example.hashpay.ui.screens.SendMoneyScreen
import com.example.hashpay.ui.theme.HashPayTheme
import com.example.hashpay.ui.viewmodels.SendMoneyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HashPayTheme {
                HashPayApp()
            }
        }
    }
}

@Composable
fun HashPayApp() {
    val navController = rememberNavController()

    // Track the current route
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: ""

    // Debug the current route value
    LaunchedEffect(currentRoute) {
        Log.d("Navigation", "Current route: $currentRoute")
    }

    Scaffold(
        bottomBar = {
            // Only show the BottomNavigationBar if we're not on the "send_money" screen
            if (currentRoute != "send_money") {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen { route -> navController.navigate(route) }
            }
            composable("qr_scanner") {
                QRScannerScreen { navController.popBackStack() }
            }
            composable("send_money") {
                val context = LocalContext.current
                val factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return SendMoneyViewModel(context) as T
                    }
                }
                val viewModel: SendMoneyViewModel = viewModel(factory = factory)
                SendMoneyScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onScanQrCode = { navController.navigate("qr_scanner") }
                )
            }
        }
    }
}