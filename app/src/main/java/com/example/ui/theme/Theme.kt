package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF86B2F9),
    primaryContainer = Color(0xFF041E49),
    onPrimaryContainer = Color(0xFFD3E3FD),
    secondary = Color(0xFFD6BCFA),
    secondaryContainer = Color(0xFF21005D),
    onSecondaryContainer = Color(0xFFF3E8FF),
    tertiary = Color(0xFFFA8E8E),
    tertiaryContainer = Color(0xFF450A0A),
    onTertiaryContainer = Color(0xFFFDF2F2),
    background = Color(0xFF121214),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF1F1F24),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF29292F),
    onSurfaceVariant = Color(0xFFC7C6CA),
    outline = Color(0xFF43474E)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BentoBlue,
    primaryContainer = BentoBlueContainer,
    onPrimaryContainer = BentoBlueOnContainer,
    secondary = BentoPurple,
    secondaryContainer = BentoPurpleContainer,
    onSecondaryContainer = BentoPurpleOnContainer,
    tertiary = BentoRed,
    tertiaryContainer = BentoRedContainer,
    onTertiaryContainer = BentoRedOnContainer,
    background = BentoBg,
    onBackground = BentoText,
    surface = Color.White,
    onSurface = BentoText,
    surfaceVariant = BentoNavBg,
    onSurfaceVariant = BentoSubText,
    outline = BentoBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled by default to locks in our signature Bento theme across all devices
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
