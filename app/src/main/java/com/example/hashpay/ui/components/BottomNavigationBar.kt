package com.example.hashpay.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.hashpay.R

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf("home", "history", "qr_scanner", "withdraw", "profile")
    val icons = listOf(
        R.drawable.ic_home,
        R.drawable.baseline_history_24,
        R.drawable.qr_scanner,
        R.drawable.ic_money,
        R.drawable.ic_person
    )

    // Main container with better height handling
    Surface(
        color = Color(0xFF1E1E1E),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            // Top separator line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF333333))
            )

            // Navigation content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp), // Fixed height for the content area only
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, route ->
                    val selected = currentRoute == route

                    // Animated color for selected state
                    val tint by animateColorAsState(
                        targetValue = when {
                            selected && route == "home" -> Color(0xFF7CFF50)
                            selected -> Color.White
                            else -> Color.Gray
                        },
                        animationSpec = tween(300),
                        label = "color"
                    )

                    // Special QR button in center
                    if (route == "qr_scanner") {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .size(56.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color(0xFF121212))
                                .clickable {
                                    if (currentRoute != route) {
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // QR Scanner content
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) Color(0xFF7CFF50) else Color(0xFF2D2D2D)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = icons[index]),
                                    contentDescription = "QR Scanner",
                                    colorFilter = ColorFilter.tint(
                                        if (selected) Color.Black else Color.White
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    } else {
                        // Regular nav items
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable {
                                    if (currentRoute != route) {
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = icons[index]),
                                    contentDescription = route,
                                    colorFilter = ColorFilter.tint(tint),
                                    modifier = Modifier.size(24.dp)
                                )

                                // Small dot indicator for selected item (only for Home)
                                if (selected && route == "home") {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .offset(y = 16.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF7CFF50))
                                    )
                                }
                            }

                            Text(
                                text = route.replace("_", " ").replaceFirstChar { it.uppercase() },
                                color = if (selected) Color(0xFF7CFF50) else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}