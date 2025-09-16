package com.example.bubtrack

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.WorkManager
import com.example.bubtrack.presentation.navigation.OnBoardingNavigation
import com.example.bubtrack.presentation.navigation.OnBoardingRoute
import com.example.bubtrack.ui.theme.BubTrackTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WorkManager.initialize(this, workManagerConfiguration)
        enableEdgeToEdge()
        setContent {
            BubTrackTheme {
                val systemUiController = rememberSystemUiController()
                val splashViewModel: SplashViewModel = hiltViewModel()
                val splashCondition = splashViewModel.splashCondition
                val startDestination = splashViewModel.startDestination.collectAsState()

                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.White,
                        darkIcons = true
                    )
                }

                if (splashCondition) {
                    SplashScreen(
                        onAnimationComplete = { splashViewModel.completeSplash() }
                    )
                } else if (startDestination.value != null) {
                    OnBoardingNavigation(
                        startDestination = startDestination.value!!
                    )
                } else
                    OnBoardingNavigation(
                        startDestination = OnBoardingRoute
                    )
            }
        }
    }
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}