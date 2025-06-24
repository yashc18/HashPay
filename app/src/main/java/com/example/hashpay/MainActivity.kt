package com.example.hashpay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.hashpay.data.AppDatabaseProvider
import com.example.hashpay.navigation.AppNavigation
import com.example.hashpay.ui.theme.HashPayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        AppDatabaseProvider.initializeDatabase(this)
        setContent {
            HashPayTheme {
                AppNavigation()

            }
        }
    }
}