package com.LCM.lifereplayapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.LCM.lifereplayapp.ui.navigation.AppNavigation
import com.LCM.lifereplayapp.ui.theme.LifereplayappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LifereplayappTheme {
                val navController = rememberNavController()
                // AppNavigation handles showing the HomePageScreen by default
                AppNavigation(navController = navController)
            }
        }
    }
}
