package com.example.bubtrack.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bubtrack.presentation.onboarding.CreateProfileScreen
import com.example.bubtrack.presentation.onboarding.ForgotPasswordScreen
import com.example.bubtrack.presentation.onboarding.login.LoginScreen
import com.example.bubtrack.presentation.onboarding.OnBoardingScreen
import com.example.bubtrack.presentation.onboarding.register.RegisterScreen

@Composable
fun OnBoardingNavigation(
    modifier: Modifier = Modifier,
    startDestination : AppRoute
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination,
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
                navController = navController,
                navigate = {
                    navController.navigate(it){
                        popUpTo(LoginRoute){
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable<RegisterRoute>{
            RegisterScreen(
                navController = navController
            )
        }
        composable<CreateProfileRoute>{
            CreateProfileScreen(
                navController = navController
            )
        }
        composable<ForgotPasswordRoute> {
            ForgotPasswordScreen(
                navigate = {
                    navController.popBackStack()
                }
            )
        }
        composable<MainRoute>{
            MainNavigation(
                navigateLogin = {
                    navController.navigate(LoginRoute){
                        popUpTo(MainRoute){
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}