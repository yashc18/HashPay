package com.example.hashpay.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.hashpay.R
import com.example.hashpay.navigation.NavDestinations
import com.example.hashpay.ui.theme.SpaceGrotesk

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    NavigationBar(
        containerColor = Color(0xFF1A1A1A),
        contentColor = Color.White
    ) {
        // Home tab
        NavigationBarItem(
            selected = currentRoute == NavDestinations.HOME,
            onClick = {
                navController.navigate(NavDestinations.HOME) {
                    popUpTo(NavDestinations.HOME) { inclusive = true }
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "Home"
                )
            },
            label = { Text("Home", fontFamily = SpaceGrotesk) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF9FE870),
                selectedTextColor = Color(0xFF9FE870),
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White,
                indicatorColor = Color(0xFF2A2A2A)
            )
        )

        // History tab
        NavigationBarItem(
            selected = currentRoute == NavDestinations.HISTORY,
            onClick = {
                navController.navigate(NavDestinations.HISTORY) {
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_history_24),
                    contentDescription = "History"
                )
            },
            label = { Text("History", fontFamily = SpaceGrotesk) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF9FE870),
                selectedTextColor = Color(0xFF9FE870),
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White,
                indicatorColor = Color(0xFF2A2A2A)
            )
        )

        // QR Scanner tab (adding it back in the middle)
        NavigationBarItem(
            selected = currentRoute == NavDestinations.QR_SCANNER,
            onClick = {
                navController.navigate(NavDestinations.QR_SCANNER) {
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.qr_scanner),
                    contentDescription = "QR Scanner"
                )
            },
            label = { Text("Scan", fontFamily = SpaceGrotesk) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF9FE870),
                selectedTextColor = Color(0xFF9FE870),
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White,
                indicatorColor = Color(0xFF2A2A2A)
            )
        )

        // Invoices tab
        NavigationBarItem(
            selected = currentRoute == NavDestinations.INVOICE,
            onClick = {
                navController.navigate(NavDestinations.INVOICE) {
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_money),
                    contentDescription = "Invoices"
                )
            },
            label = { Text("Invoices", fontFamily = SpaceGrotesk) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF9FE870),
                selectedTextColor = Color(0xFF9FE870),
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White,
                indicatorColor = Color(0xFF2A2A2A)
            )
        )

        // Profile tab
        NavigationBarItem(
            selected = currentRoute == NavDestinations.PROFILE,
            onClick = {
                navController.navigate(NavDestinations.PROFILE) {
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile", fontFamily = SpaceGrotesk) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF9FE870),
                selectedTextColor = Color(0xFF9FE870),
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White,
                indicatorColor = Color(0xFF2A2A2A)
            )
        )
    }
}