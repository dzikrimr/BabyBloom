package com.example.bubtrack.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute

@Serializable
data object HomeRoute : AppRoute

@Serializable
data object DiaryRoute : AppRoute

@Serializable
data object AiRoute : AppRoute

@Serializable
data object ArticleRoute : AppRoute

@Serializable
data object ProfileRoute : AppRoute