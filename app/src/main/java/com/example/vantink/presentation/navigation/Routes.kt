package com.example.vantink.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Home : Route

    @Serializable
    data object Search : Route

    @Serializable
    data object Favorites : Route

    @Serializable
    data object History : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Sources : Route

    @Serializable
    data object Directory : Route

    @Serializable
    data object Downloads : Route

    @Serializable
    data class Details(val webtoonId: String) : Route

    @Serializable
    data class Reader(val webtoonId: String, val chapterId: String) : Route
}
