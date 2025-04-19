package com.example.hashpay.ui.components

import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.hashpay.R

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf("home", "history", "qr_scanner", "withdraw", "profile")
    val labels = listOf("Home", "History", "QR", "Withdraw", "Profile")
    val icons = listOf(
        R.drawable.ic_home,
        R.drawable.baseline_history_24,
        R.drawable.qr_scanner,
        R.drawable.ic_money,
        R.drawable.ic_person
    )

    NavigationBar(containerColor = Color.DarkGray) {
        items.forEachIndexed { index, route ->
            NavigationBarItem(
                icon = {
                    Image(
                        painter = painterResource(id = icons[index]),
                        contentDescription = labels[index]
                    )
                },
                label = { Text(labels[index], color = Color.White) },
                selected = currentRoute == route,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}