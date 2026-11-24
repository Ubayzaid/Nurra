package com.nurra.islamic.ui.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
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
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.nurra.islamic.repository.AppSettings
import com.nurra.islamic.repository.AppThemeMode

// Enforce High Contrast on Dynamic Dark Color Schemes (Prevents MIUI white-on-white text)
private fun ColorScheme.enforceHighContrastDark(): ColorScheme {
    return this.copy(
        primaryContainer = Color(0xFF132A4C),   // Deep Dark Slate Container
        onPrimaryContainer = Color(0xFF93C5FD), // High Contrast Light Blue Text
        onBackground = Color(0xFFFFFFFF),
        onSurface = Color(0xFFFFFFFF),
        onSurfaceVariant = Color(0xFFCBD5E1),
        onPrimary = Color(0xFF000000),
        onSecondary = Color(0xFF000000),
        onSecondaryContainer = Color(0xFFFFFBEB)
    )
}

// Enforce High Contrast on Dynamic Light Color Schemes (Prevents washed-out dark-on-dark text)
private fun ColorScheme.enforceHighContrastLight(): ColorScheme {
    return this.copy(
        primaryContainer = Color(0xFFDBEAFE),   // Crisp Light Slate/Blue Container
        onPrimaryContainer = Color(0xFF1E3A8A), // Deep Navy Readable Text
        onBackground = Color(0xFF0F172A),
        onSurface = Color(0xFF0F172A),
        onSurfaceVariant = Color(0xFF475569),
        onPrimary = Color(0xFFFFFFFF),
        onSecondary = Color(0xFFFFFFFF),
        onSecondaryContainer = Color(0xFF78350F)
    )
}

// Pure AMOLED Black (#000000 Background) + Dynamic Wallpaper Accent Colors
@Composable
private fun getAmoledColorScheme(context: Context, isDynamicColorEnabled: Boolean): ColorScheme {
    val dynamicDark = if (isDynamicColorEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context)
    } else null

    return darkColorScheme(
        primary = dynamicDark?.primary ?: EmeraldPrimaryDark,
        onPrimary = Color.Black,
        primaryContainer = Color(0xFF132A4C),   // Deep Dark Slate Container (Prevents MIUI white-out)
        onPrimaryContainer = Color(0xFF93C5FD), // High Contrast Light Blue Text
        secondary = dynamicDark?.secondary ?: AmberSecondaryDark,
        onSecondary = Color.Black,
        secondaryContainer = dynamicDark?.secondaryContainer ?: Color(0xFF452003),
        onSecondaryContainer = Color(0xFFFEF3C7),
        tertiary = dynamicDark?.tertiary ?: SageTertiaryDark,
        onTertiary = Color.Black,
        tertiaryContainer = SageContainerDark,
        onTertiaryContainer = Color(0xFFD1FAE5),
        background = Color(0xFF000000),         // PURE AMOLED BLACK (#000000)
        onBackground = Color(0xFFFFFFFF),       // High contrast crisp text
        surface = Color(0xFF000000),            // PURE AMOLED BLACK (#000000)
        onSurface = Color(0xFFFFFFFF),          // High contrast crisp text
        surfaceVariant = Color(0xFF1E242B),     // Raised AMOLED card container (#1E242B)
        onSurfaceVariant = Color(0xFFCBD5E1),   // Crisp Muted Text
        outline = Color(0xFF3B4754),            // Crisp dark border
        outlineVariant = Color(0xFF232B34)
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimaryDark,
    onPrimary = EmeraldOnPrimaryDark,
    primaryContainer = Color(0xFF132A4C),
    onPrimaryContainer = Color(0xFF93C5FD),
    secondary = AmberSecondaryDark,
    onSecondary = AmberOnSecondaryDark,
    secondaryContainer = AmberContainerDark,
    onSecondaryContainer = OnAmberContainerDark,
    tertiary = SageTertiaryDark,
    onTertiary = Color.Black,
    tertiaryContainer = SageContainerDark,
    onTertiaryContainer = OnSageContainerDark,
    background = BackgroundDark,
    onBackground = Color(0xFFFFFFFF),
    surface = SurfaceDark,
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimaryLight,
    onPrimary = EmeraldOnPrimaryLight,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = AmberSecondaryLight,
    onSecondary = AmberOnSecondaryLight,
    secondaryContainer = AmberContainerLight,
    onSecondaryContainer = OnAmberContainerLight,
    tertiary = SageTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = SageContainerLight,
    onTertiaryContainer = OnSageContainerLight,
    background = Color(0xFFF1F5F9),        // Soft Modern Off-White Slate
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),           // Crisp White Card Surface
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),    // Subtle Slate Container
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),           // Delicate M3 Outline Border
    outlineVariant = Color(0xFFE2E8F0)
)

val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun NurraTheme(
    settings: AppSettings = AppSettings(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    val isDark = when {
        settings.isAmoledBlackEnabled -> true
        settings.themeMode == AppThemeMode.DARK || settings.themeMode == AppThemeMode.DYNAMIC_DARK || settings.themeMode == AppThemeMode.AMOLED -> true
        settings.themeMode == AppThemeMode.LIGHT || settings.themeMode == AppThemeMode.DYNAMIC_LIGHT -> false
        else -> systemInDark
    }

    val colorScheme = when {
        // Pure AMOLED Black Mode (#000000 background + Wallpaper Dynamic Accent Colors)
        settings.isAmoledBlackEnabled || settings.themeMode == AppThemeMode.AMOLED -> {
            getAmoledColorScheme(context, settings.isDynamicColorEnabled)
        }

        // Dynamic Light & Dynamic Dark Modes on Android 12+ (API 31+) if enabled
        settings.isDynamicColorEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            when (settings.themeMode) {
                AppThemeMode.DYNAMIC_LIGHT -> dynamicLightColorScheme(context).enforceHighContrastLight()
                AppThemeMode.DYNAMIC_DARK -> dynamicDarkColorScheme(context).enforceHighContrastDark()
                else -> {
                    if (isDark) {
                        dynamicDarkColorScheme(context).enforceHighContrastDark()
                    } else {
                        dynamicLightColorScheme(context).enforceHighContrastLight()
                    }
                }
            }
        }

        // Fallback Material 3 Expressive Color Schemes when Dynamic Color is disabled or unavailable
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ExpressiveShapes,
        content = content
    )
}
