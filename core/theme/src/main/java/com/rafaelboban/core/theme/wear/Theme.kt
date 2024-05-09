package com.rafaelboban.core.theme.wear

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography
import com.rafaelboban.core.theme.mobile.DarkColors
import com.rafaelboban.core.theme.mobile.Montserrat

private val WearColors: ColorScheme
    get() {
        val phoneTheme = DarkColors

        return ColorScheme(
            primary = phoneTheme.primary,
            primaryContainer = phoneTheme.primaryContainer,
            onPrimary = phoneTheme.onPrimary,
            onPrimaryContainer = phoneTheme.onPrimaryContainer,
            secondary = phoneTheme.secondary,
            onSecondary = phoneTheme.onSecondary,
            secondaryContainer = phoneTheme.secondaryContainer,
            onSecondaryContainer = phoneTheme.onSecondaryContainer,
            tertiary = phoneTheme.tertiary,
            onTertiary = phoneTheme.onTertiary,
            tertiaryContainer = phoneTheme.tertiaryContainer,
            onTertiaryContainer = phoneTheme.onTertiaryContainer,
            surfaceContainer = phoneTheme.surface,
            onSurface = phoneTheme.onSurface,
            onSurfaceVariant = phoneTheme.onSurfaceVariant,
            background = phoneTheme.background,
            error = phoneTheme.error,
            onError = phoneTheme.onError,
            onBackground = phoneTheme.onBackground,
        )
    }

val WearTypography = Typography().defaultFontFamily(Montserrat)

private fun Typography.defaultFontFamily(fontFamily: FontFamily): Typography {
    return this.copy(
        displayLarge = this.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = this.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = this.displaySmall.copy(fontFamily = fontFamily),
        titleLarge = this.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = this.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = this.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = this.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = this.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = this.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = this.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = this.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = this.labelSmall.copy(fontFamily = fontFamily)
    )
}

@Composable
fun ActivityTrackerWearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WearColors,
        typography = WearTypography,
        content = content
    )
}
