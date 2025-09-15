package com.example.bubtrack.presentation.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.example.bubtrack.data.ai.AudioRepoImpl
import com.example.bubtrack.data.ai.ModelRepoImpl
import com.example.bubtrack.domain.ai.audio.MFCCExtractor
import com.example.bubtrack.domain.usecase.ClassifyAudioUseCase
import com.example.bubtrack.domain.usecase.RecordAudioUseCase
import com.example.bubtrack.presentation.ai.AiScreen
import com.example.bubtrack.presentation.ai.cobamonitor.BabyScreen
import com.example.bubtrack.presentation.ai.cobamonitor.LandingScreen
import com.example.bubtrack.presentation.ai.cobamonitor.ParentScreen
import com.example.bubtrack.presentation.ai.cryanalyzer.CryAnalyzerScreen
import com.example.bubtrack.presentation.ai.cryanalyzer.CryAnalyzerViewModel
import com.example.bubtrack.presentation.ai.sleepmonitor.SleepMonitorScreen
import com.example.bubtrack.presentation.ai.sleepmonitor.SleepMonitorViewModel
import com.example.bubtrack.presentation.activities.ActivitiesScreen
import com.example.bubtrack.presentation.ai.growthanalysis.GrowthAnalysisScreen
import com.example.bubtrack.presentation.article.ArticleDetailScreen
import com.example.bubtrack.presentation.article.ArticleScreen
import com.example.bubtrack.presentation.article.ArticleSearchScreen
import com.example.bubtrack.presentation.diary.DiaryScreen
import com.example.bubtrack.presentation.home.HomeScreen
import com.example.bubtrack.presentation.notification.NotificationScreen
import com.example.bubtrack.presentation.profile.ProfileScreen
import com.example.bubtrack.presentation.profile.comps.EditProfileScreen
import com.example.bubtrack.presentation.profile.comps.HelpAndReportScreen
import com.example.bubtrack.presentation.profile.comps.SettingScreen
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun MainNavigation(
    modifier: Modifier = Modifier,
    navigateLogin: () -> Unit
) {
    val navController = rememberNavController()
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    val insets = WindowInsets.statusBars.asPaddingValues()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val screenWithoutNavbar = listOf(
        ActivitiesRoute::class.qualifiedName!!,
        SleepMonitorRoute::class.qualifiedName!!,
        CryAnalyzerRoute::class.qualifiedName!!,
        GrowthAnalysisRoute::class.qualifiedName!!,
        ParentScreenRoute::class.qualifiedName!!,
        BabyScreenRoute::class.qualifiedName!!
    )
    val showBottomBar = currentDestination?.route?.let { currentRoute ->
        !screenWithoutNavbar.any { routeWithoutNavbar ->
            currentRoute.startsWith(routeWithoutNavbar)
        }
    } ?: true

    // Dependencies from your branch for AiScreen and CryAnalyzerScreen
    val context = LocalContext.current
    val sleepViewModel: SleepMonitorViewModel = hiltViewModel()
    val audioRepository = AudioRepoImpl(context) { true }
    val modelRepository = ModelRepoImpl(context, MFCCExtractor())
    val recordAudioUseCase = RecordAudioUseCase(audioRepository)
    val classifyAudioUseCase = ClassifyAudioUseCase(modelRepository)
    val cryAnalyzerViewModel = CryAnalyzerViewModel(recordAudioUseCase, classifyAudioUseCase)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .padding(insets),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    selectedItem = selectedItem,
                    onItemClick = {
                        selectedItem = it
                        when (it) {
                            0 -> navController.navigate(HomeRoute)
                            1 -> navController.navigate(DiaryRoute)
                            2 -> navController.navigate(AiRoute)
                            3 -> navController.navigate(ArticleRoute)
                            4 -> navController.navigate(ProfileRoute)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        val bottomPadding = innerPadding.calculateBottomPadding()
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = modifier.padding(bottom = bottomPadding)
        ) {
            composable<HomeRoute> {
                HomeScreen(navController = navController)
            }
            composable<ActivitiesRoute> {
                ActivitiesScreen(navController = navController)
            }
            composable<DiaryRoute> {
                DiaryScreen()
            }
            composable(
                route = "diary/{tab}",
                arguments = listOf(navArgument("tab") { type = NavType.StringType })
            ) { backStackEntry ->
                val tab = backStackEntry.arguments?.getString("tab") ?: ""
                DiaryScreen(initialTab = tab)
            }
            composable<AiRoute> {
                AiScreen(
                    navController = navController,
                    onStartCryAnalysis = { navController.navigate(CryAnalyzerRoute) },
                    onStartMonitoring = { navController.navigate(SleepMonitorRoute) }
                )
            }
            composable<SleepMonitorRoute> {
                // Commented out SleepMonitorScreen; uncomment if needed
                /*
                SleepMonitorScreen(
                    navController = navController,
                    sleepViewModel = sleepViewModel,
                    webRTCService = webRTCService,
                    onBackClick = { navController.popBackStack() },
                    onStopMonitor = { navController.popBackStack() },
                    onCryModeClick = { navController.navigate(CryAnalyzerRoute) }
                )
                */
                LandingScreen(
                    navController = navController,
                )
            }
            composable<CryAnalyzerRoute> {
                CryAnalyzerScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = cryAnalyzerViewModel
                )
            }
            composable<GrowthAnalysisRoute> { backStackEntry ->
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                    ?: backStackEntry.arguments?.getString("userId")
                    ?: throw IllegalStateException("User ID is required for GrowthAnalysisScreen")
                GrowthAnalysisScreen(
                    userId = userId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            navigation<ArticleRoute>(startDestination = ArticleHomeRoute) {
                composable<ArticleHomeRoute> {
                    ArticleScreen(
                        navController = navController,
                        navigateDetail = { navController.navigate(ArticleDetailRoute(it)) },
                        navigateSearch = { navController.navigate(ArticleSearchRoute(it)) }
                    )
                }
                composable<ArticleDetailRoute> {
                    val id = it.toRoute<ArticleDetailRoute>()
                    ArticleDetailScreen(
                        navController = navController,
                        articleId = id.id
                    )
                }
                composable<ArticleSearchRoute> {
                    val query = it.toRoute<ArticleSearchRoute>()
                    ArticleSearchScreen(
                        navController = navController,
                        searchQuery = query.query
                    ) { navController.navigate(ArticleDetailRoute(it)) }
                }
            }
            navigation<InitialProfileRoute>(startDestination = ProfileRoute) {
                composable<ProfileRoute> {
                    ProfileScreen(
                        navController = navController,
                        navigateLogin = { navigateLogin() })
                }
                composable<EditProfileRoute> {
                    EditProfileScreen(navigateBack = { navController.popBackStack() })
                }
                composable<SettingRoute> {
                    SettingScreen(navigateBack = {navController.popBackStack()})
                }
                composable<HelpAndReportRoute>{
                    HelpAndReportScreen(navigateBack = {navController.popBackStack()})
                }
            }
            composable<NotificationRoute> {
                NotificationScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable<ParentScreenRoute> {
                ParentScreen(
                )
            }
            composable<BabyScreenRoute> {
                BabyScreen(
                )
            }
        }
    }
}