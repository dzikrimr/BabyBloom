package com.example.bubtrack.presentation.navigation

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
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.bubtrack.presentation.ai.sleepmonitor.SleepMonitorScreen
import com.example.bubtrack.presentation.activities.ActivitiesScreen
import com.example.bubtrack.presentation.ai.AiScreen
import com.example.bubtrack.presentation.article.ArticleDetailScreen
import com.example.bubtrack.presentation.article.ArticleScreen
import com.example.bubtrack.presentation.article.ArticleSearchScreen
import com.example.bubtrack.presentation.article.ArticleViewModel
import com.example.bubtrack.presentation.diary.DiaryScreen
import com.example.bubtrack.presentation.home.HomeScreen
import com.example.bubtrack.presentation.profile.ProfileScreen
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun MainNavigation(
    modifier: Modifier = Modifier,
    navigateLogin: () -> Unit
) {
    val navController = rememberNavController()
    var selectedItem by rememberSaveable {
        mutableIntStateOf(0)
    }
    val insets = WindowInsets.statusBars.asPaddingValues()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val screenWithoutNavbar = listOf(
        ActivitiesRoute::class.qualifiedName!!,
        SleepMonitorRoute::class.qualifiedName!!
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
    ) {
        val bottomPadding = it.calculateBottomPadding()
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = modifier.padding(bottom = bottomPadding)
        ) {
            composable<HomeRoute> {
                HomeScreen(
                    navController = navController
                )
            }
            composable<ActivitiesRoute> {
                ActivitiesScreen(
                    navController = navController
                )
            }
            composable<DiaryRoute> {
                DiaryScreen()
            }
            composable<AiRoute> {
                AiScreen(navController = navController)
            }
            composable<SleepMonitorRoute> {
                SleepMonitorScreen(
                    navController = navController,
                    onBackClick = { navController.popBackStack() }
                )
            }
            navigation<ArticleRoute>(
                startDestination = ArticleHomeRoute) {
                composable<ArticleHomeRoute> {
                    ArticleScreen(
                        navController = navController,
                        navigateDetail = {
                            navController.navigate(
                                ArticleDetailRoute(it)
                            )
                        },
                        navigateSearch = {
                            navController.navigate(
                                ArticleSearchRoute(it)
                            )
                        }
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
                    ) {
                        navController.navigate(
                            ArticleDetailRoute(it)
                        )
                    }
                }
            }
            composable<ProfileRoute> {
                ProfileScreen(
                    navigateLogin = {
                        navigateLogin()
                    }
                )
            }
        }
    }
}