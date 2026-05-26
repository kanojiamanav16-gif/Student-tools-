package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SophisticatedPurplePrimary,
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4A4458),
    onPrimaryContainer = SophisticatedPurpleContainer,
    secondary = SophisticatedPurpleContainer,
    onSecondary = SophisticatedOnPurpleContainer,
    background = SophisticatedDarkBg,
    onBackground = Color(0xFFE6E1E5),
    surface = SophisticatedCardBg,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = SophisticatedBorder,
    onSurfaceVariant = SophisticatedMutedText,
    outline = Color(0xFF938F99),
    error = SophisticatedCoralWarning,
    onError = Color(0xFF690005)
)

private val LightColorScheme = DarkColorScheme // Force dark elegant styling everywhere

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark elegant styling everywhere
  dynamicColor: Boolean = false, // Disable system dynamic accent color overrides
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
