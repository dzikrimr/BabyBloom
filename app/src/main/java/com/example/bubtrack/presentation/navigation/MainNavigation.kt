package com.example.bubtrack.presentation.navigation

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bubtrack.presentation.ai.AiScreen
import com.example.bubtrack.presentation.article.ArticleScreen
import com.example.bubtrack.presentation.diary.DiaryScreen
import com.example.bubtrack.presentation.home.HomeScreen
import com.example.bubtrack.presentation.profile.ProfileScreen

@Composable
fun MainNavigation(modifier: Modifier = Modifier) {

    val navController = rememberNavController()
    var selectedItem by rememberSaveable {
        mutableIntStateOf(0)
    }
    val insets = WindowInsets.statusBars.asPaddingValues()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .padding(insets),
        bottomBar = {
            BottomNavBar(
                selectedItem = selectedItem,
                onItemClick = {
                    selectedItem = it
                    when (it){
                        0 -> navController.navigate(HomeRoute)
                        1 -> navController.navigate(DiaryRoute)
                        2 -> navController.navigate(AiRoute)
                        3 -> navController.navigate(ArticleRoute)
                        4 -> navController.navigate(ProfileRoute)
                    }
                }
            )
        }
    ) {
        val bottomPadding = it.calculateBottomPadding()
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = modifier.padding(bottom = bottomPadding)
        ) {
            composable<HomeRoute>{
                HomeScreen()
            }
            composable<DiaryRoute>{
                DiaryScreen()
            }
            composable<AiRoute>{
                AiScreen()
            }
            composable<ArticleRoute>{
                ArticleScreen()
            }
            composable<ProfileRoute>{
                ProfileScreen()
            }
        }
    }
}