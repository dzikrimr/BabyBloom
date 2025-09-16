package com.example.bubtrack.presentation.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.example.bubtrack.presentation.ai.AiScreen
import com.example.bubtrack.presentation.ai.cobamonitor.BabyScreen
import com.example.bubtrack.presentation.ai.cobamonitor.LandingScreen
import com.example.bubtrack.presentation.ai.cobamonitor.ParentScreen
import com.example.bubtrack.presentation.ai.cryanalyzer.CryAnalyzerScreen
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


    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .padding(insets),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    selectedItem = when (currentDestination?.route) {
                        HomeRoute::class.qualifiedName -> 0
                        DiaryRoute::class.qualifiedName -> 1
                        AiRoute::class.qualifiedName -> 2
                        ArticleRoute::class.qualifiedName -> 3
                        ProfileRoute::class.qualifiedName -> 4
                        else -> -1 // tidak ada yang dipilih
                    },
                    onItemClick = {
                        selectedItem = it
                        when (it) {
                            0 -> navigateToTab(navController, HomeRoute)
                            1 -> navigateToTab(navController, DiaryRoute)
                            2 -> navigateToTab(navController, AiRoute)
                            3 -> navigateToTab(navController, ArticleRoute)
                            4 -> navigateToTab(navController, ProfileRoute)
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
                HomeScreen(navController = navController,)
            }
            composable<ActivitiesRoute> {
                ActivitiesScreen(navController = navController)
            }
            composable<DiaryRoute> {
                DiaryScreen()
            }
            composable<AiRoute> {
                AiScreen(
                    navController = navController,
                    onStartCryAnalysis = { navController.navigate(CryAnalyzerRoute) },
                    onStartMonitoring = { navController.navigate(SleepMonitorRoute) }
                )
            }
            composable<SleepMonitorRoute> {
                LandingScreen(
                    navController = navController,
                )
            }
            composable<CryAnalyzerRoute> {
                CryAnalyzerScreen(
                    onNavigateBack = { navController.popBackStack() }
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
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable<BabyScreenRoute> {
                BabyScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun navigateToTab(
    navController: NavController,
    route: AppRoute
) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}