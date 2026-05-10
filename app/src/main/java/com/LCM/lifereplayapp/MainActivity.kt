package com.LCM.lifereplayapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.LCM.lifereplayapp.data.UserPreferencesRepository
import com.LCM.lifereplayapp.repositories.GenerativeAiRepository
import com.LCM.lifereplayapp.repositories.AuthRepository
import com.LCM.lifereplayapp.ui.navigation.AppNavigation
import com.LCM.lifereplayapp.ui.theme.LifereplayappTheme
import com.LCM.lifereplayapp.viewmodel.UserViewModel

class UserViewModelFactory(
    private val repository: UserPreferencesRepository,
    private val aiRepository: GenerativeAiRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository, aiRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = UserPreferencesRepository(applicationContext)
        val aiRepository = GenerativeAiRepository(applicationContext)
        val authRepository = AuthRepository()
        val factory = UserViewModelFactory(repository, aiRepository, authRepository)
        
        setContent {
            LifereplayappTheme {
                val navController = rememberNavController()
                val userViewModel: UserViewModel = viewModel(factory = factory)
                // AppNavigation handles showing the HomePageScreen by default
                AppNavigation(navController = navController, userViewModel = userViewModel)
            }
        }
    }
}
