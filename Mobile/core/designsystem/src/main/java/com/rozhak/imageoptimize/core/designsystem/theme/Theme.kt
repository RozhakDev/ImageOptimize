package com.rozhak.imageoptimize.core.designsystem.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color

private val WorkspaceColorScheme = lightColorScheme(
    primary = WorkspaceBlue,
    onPrimary = Color.White,
    primaryContainer = WorkspaceBlueContainer,
    onPrimaryContainer = WorkspaceOnBlueContainer,
    background = WorkspaceBackground,
    onBackground = WorkspaceOnSurface,
    surface = WorkspaceSurface,
    onSurface = WorkspaceOnSurface,
    surfaceVariant = WorkspaceSurfaceVariant,
    onSurfaceVariant = WorkspaceOnSurfaceVariant,
    outline = WorkspaceOutline,
    outlineVariant = WorkspaceOutlineVariant
)

@Composable
fun ImageOptimizeTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = WorkspaceColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WorkspaceTypography,
        content = content
    )
}
