package com.uniktek.meshmarket.ui.theme

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

@Composable
fun MeshMarketTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    // Color theme from ColorThemeManager (vim themes)
    val colorTheme by ColorThemeManager.themeFlow.collectAsState()

    // Legacy light/dark preference - used only when color theme is DEFAULT or LIGHT
    val themePref by ThemePreferenceManager.themeFlow.collectAsState(initial = ThemePreference.System)

    // Determine the color scheme to use
    val colorScheme = when (colorTheme) {
        // For DEFAULT and LIGHT, respect the legacy System/Light/Dark toggle
        ColorTheme.DEFAULT, ColorTheme.LIGHT -> {
            val shouldUseDark = when (darkTheme) {
                true -> true
                false -> false
                null -> when (themePref) {
                    ThemePreference.Dark -> true
                    ThemePreference.Light -> false
                    ThemePreference.System -> isSystemInDarkTheme()
                }
            }
            if (shouldUseDark) ColorTheme.DEFAULT.colorScheme() else ColorTheme.LIGHT.colorScheme()
        }
        // For all other themes, use their specific color scheme directly
        else -> colorTheme.colorScheme()
    }

    val isDark = colorTheme.isDark || (colorTheme == ColorTheme.DEFAULT && when (darkTheme) {
        true -> true
        false -> false
        null -> when (themePref) {
            ThemePreference.Dark -> true
            ThemePreference.Light -> false
            ThemePreference.System -> isSystemInDarkTheme()
        }
    })

    val view = LocalView.current
    SideEffect {
        (view.context as? Activity)?.window?.let { window ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.setSystemBarsAppearance(
                    if (!isDark) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = if (!isDark) {
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else 0
            }
            window.navigationBarColor = colorScheme.background.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    // Dynamic typography based on user's display scale preference
    val displayScale by DisplayScaleManager.scaleFlow.collectAsState()
    val dynamicTypography = scaledTypography(displayScale.factor)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = dynamicTypography,
        content = content
    )
}
