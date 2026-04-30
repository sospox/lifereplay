package com.LCM.lifereplayapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.LCM.lifereplayapp.ui.screens.authentication.ChangePasswordScreen
import com.LCM.lifereplayapp.ui.screens.authentication.HomePageScreen
import com.LCM.lifereplayapp.ui.screens.authentication.LoginScreen
import com.LCM.lifereplayapp.ui.screens.authentication.ProfileScreen
import com.LCM.lifereplayapp.ui.screens.authentication.SignupScreen

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = ROUTES.HomePage.name,
        modifier = modifier
    ) {
        composable(ROUTES.HomePage.name) {
            HomePageScreen(navController, modifier)
        }
        composable(ROUTES.Login.name) {
            LoginScreen(navController, modifier)
        }
        composable(ROUTES.ChangePassword.name) {
            ChangePasswordScreen(navController, modifier)
        }
        composable(ROUTES.Signup.name) {
            SignupScreen(navController, modifier)
        }
        composable(ROUTES.Profile.name) {
            ProfileScreen(navController, modifier)
        }
    }
}
