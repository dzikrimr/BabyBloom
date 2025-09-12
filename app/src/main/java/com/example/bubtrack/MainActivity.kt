package com.example.bubtrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bubtrack.presentation.activities.ActivitiesScreen
import com.example.bubtrack.presentation.home.HomeScreen
import com.example.bubtrack.presentation.navigation.MainNavigation
import com.example.bubtrack.presentation.navigation.OnBoardingNavigation
import com.example.bubtrack.presentation.onboarding.CreateProfileScreen
import com.example.bubtrack.presentation.onboarding.OnBoardingScreen
import com.example.bubtrack.ui.theme.BubTrackTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BubTrackTheme {
                val systemUiController = rememberSystemUiController()
                val splashViewModel : SplashViewModel = hiltViewModel()
                val splashCondition = splashViewModel.splashCondition
                val startDestination = splashViewModel.startDestination.collectAsState()
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.White,
                        darkIcons = true
                    )
                }
                if (splashCondition){
                    SplashScreen()
                } else {
                    OnBoardingNavigation(
                        startDestination = startDestination.value
                    )
                }
            }
        }
    }
}
