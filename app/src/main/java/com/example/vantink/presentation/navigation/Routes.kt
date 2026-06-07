package com.example.vantink.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable data object Library : Route
    @Serializable data object Updates : Route
    @Serializable data object History : Route
    @Serializable data object Browse : Route
    @Serializable data object More : Route
    
    @Serializable data object Search : Route
    @Serializable data object Extensions : Route
    @Serializable data object Directory : Route
    @Serializable data class Details(val webtoonId: String) : Route
    @Serializable data class Reader(val webtoonId: String, val chapterId: String) : Route
    @Serializable data class Web(val url: String, val title: String) : Route
}
