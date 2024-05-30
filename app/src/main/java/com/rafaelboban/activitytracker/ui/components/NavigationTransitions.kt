package com.rafaelboban.activitytracker.ui.components

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import kotlin.reflect.KType

fun NavGraphBuilder.composableFade(
    route: String,
    content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {
    composable(
        route = route,
        enterTransition = { fadeIn(tween(200)) },
        exitTransition = { fadeOut(tween(200)) },
        content = content
    )
}

fun NavGraphBuilder.composableSlide(
    route: String,
    content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {
    composable(
        route = route,
        enterTransition = { slideInHorizontally(tween(200), initialOffsetX = { x -> x }) },
        exitTransition = { slideOutHorizontally(tween(200), targetOffsetX = { x -> -x }) },
        content = content
    )
}

inline fun <reified T : Any> NavGraphBuilder.composableFade(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {
    composable<T>(
        deepLinks = deepLinks,
        enterTransition = { fadeIn(tween(200)) },
        exitTransition = { fadeOut(tween(200)) },
        content = content
    )
}

inline fun <reified T : Any> NavGraphBuilder.composableSlide(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {
    composable<T>(
        deepLinks = deepLinks,
        enterTransition = { slideInHorizontally(tween(200), initialOffsetX = { x -> x }) },
        exitTransition = { slideOutHorizontally(tween(200), targetOffsetX = { x -> -x }) },
        content = content
    )
}
