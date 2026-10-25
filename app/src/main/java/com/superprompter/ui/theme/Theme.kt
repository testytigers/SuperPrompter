package com.superprompter.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private val darkScheme = darkColorScheme(
    primary = CyanPrimary,
    secondary = VioletAccent,
    tertiary = CyanLight,
    background = DarkSurface,
    surface = DarkSurfaceVariant,
    onPrimary = DarkSurface,
    onSecondary = Color.White,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
)

private val lightScheme = lightColorScheme(
    primary = CyanDark,
    secondary = VioletDark,
    tertiary = CyanPrimary,
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onSurface = Color(0xFF1A1D23),
    onSurfaceVariant = Color(0xFF5A6273),
)

@Immutable
data class ExtendedColorScheme(
    val glass: Color = Color.Unspecified,
)

@Composable
fun SuperPrompterTheme(
    darkTheme: Boolean = true, // The app is designed dark-first
    // Dynamic color would replace the cyan/violet brand palette, so it is opt-in

    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkScheme
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
