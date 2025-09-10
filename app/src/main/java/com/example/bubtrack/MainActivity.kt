package com.example.bubtrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.bubtrack.data.database.SleepDatabase
import com.example.bubtrack.repository.SleepRepository
import com.example.bubtrack.ui.screens.BabySleepMonitorScreen
import com.example.bubtrack.ui.screens.VoiceAnalyzerScreen
import com.example.bubtrack.utils.NotificationHelper
import com.example.bubtrack.viewmodel.SleepDetectionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Room database
        val database = Room.databaseBuilder(
            applicationContext,
            SleepDatabase::class.java,
            "sleep_database"
        ).build()

        // Initialize SleepRepository
        val repository = SleepRepository(database.sleepDao())

        // Initialize NotificationHelper
        val notificationHelper = NotificationHelper(applicationContext)

        // Create ViewModelFactory
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SleepDetectionViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return SleepDetectionViewModel(
                        repository = repository,
                        notificationHelper = notificationHelper,
                        context = applicationContext
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModelFactory = viewModelFactory)
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppNavigation(viewModelFactory: ViewModelProvider.Factory) {
    val navController = rememberNavController()
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.POST_NOTIFICATIONS
        )
    )

    if (permissionsState.allPermissionsGranted) {
        NavHost(
            navController = navController,
            startDestination = "sleep_monitor"
        ) {
            composable("sleep_monitor") {
                val viewModel: SleepDetectionViewModel = viewModel(factory = viewModelFactory)
                BabySleepMonitorScreen(
                    viewModel = viewModel,
                    onNavigateToVoiceAnalyzer = {
                        navController.navigate("voice_analyzer")
                    }
                )
            }
            composable("voice_analyzer") {
                VoiceAnalyzerScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    } else {
        LaunchedEffect(Unit) {
            permissionsState.launchMultiplePermissionRequest()
        }
        Text("Memerlukan izin kamera dan notifikasi untuk memulai monitoring")
    }
}