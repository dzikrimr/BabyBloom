package com.example.bubtrack.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute

@Serializable
data object OnBoardingRoute : AppRoute

@Serializable
data object LoginRoute : AppRoute

@Serializable
data object RegisterRoute : AppRoute

@Serializable
data object CreateProfileRoute : AppRoute

@Serializable
data object MainRoute : AppRoute

@Serializable
data object HomeRoute : AppRoute

@Serializable
data object DiaryRoute : AppRoute

@Serializable
data object AiRoute : AppRoute

@Serializable
data object ArticleRoute : AppRoute

@Serializable
data object ArticleHomeRoute : AppRoute

@Serializable
data object ArticleDetailRoute : AppRoute

@Serializable
data object ProfileRoute : AppRoute

@Serializable
data object ActivitiesRoute : AppRoute

@Serializable
data object SleepMonitorRoute : AppRoute