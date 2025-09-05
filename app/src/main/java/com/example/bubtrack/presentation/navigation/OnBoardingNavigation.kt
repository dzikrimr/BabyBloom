package com.example.bubtrack.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bubtrack.presentation.onboarding.LoginScreen
import com.example.bubtrack.presentation.onboarding.OnBoardingScreen
import com.example.bubtrack.presentation.onboarding.RegisterScreen

@Composable
fun OnBoardingNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = OnBoardingRoute,
        modifier = modifier.fillMaxSize()
    ){
        composable<OnBoardingRoute>{
            OnBoardingScreen(
                navigateLogin = {
                    navController.navigate(LoginRoute)
                }
            )
        }
        composable<LoginRoute>{
            LoginScreen(
                navController = navController
            )
        }
        composable<RegisterRoute>{
            RegisterScreen(
                navController = navController
            )
        }
        composable<MainRoute>{
            MainNavigation()
        }
    }
}