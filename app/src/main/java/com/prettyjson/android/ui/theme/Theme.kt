package com.prettyjson.android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun PrettyJSONTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeStyle: String = "default",
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Custom theme styles take precedence
        themeStyle == "dracula" && darkTheme -> DraculaTheme.Dark
        themeStyle == "solarized" && darkTheme -> SolarizedTheme.Dark
        themeStyle == "solarized" && !darkTheme -> SolarizedTheme.Light
        themeStyle == "onedark" && darkTheme -> OneDarkTheme.Dark
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && themeStyle == "default" -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                // Set status bar and navigation bar colors to match theme surface
                WindowCompat.setDecorFitsSystemWindows(it, true)
                it.statusBarColor = colorScheme.surface.toArgb()
                it.navigationBarColor = colorScheme.surface.toArgb()
                
                // Set light/dark status bar icons based on theme
                val insetsController = WindowCompat.getInsetsController(it, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun PrettyJSONTheme(
    themePreference: String,
    themeStyle: String = "default",
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme() // "system"
    }
    
    PrettyJSONTheme(darkTheme = darkTheme, themeStyle = themeStyle, dynamicColor = dynamicColor, content = content)
}