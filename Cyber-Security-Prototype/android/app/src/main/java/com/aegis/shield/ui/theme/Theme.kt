package com.aegis.shield.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AegisColorScheme = darkColorScheme(
    primary         = AccentBlue,
    onPrimary       = TextPrimary,
    secondary       = PurpleAccent,
    onSecondary     = TextPrimary,
    error           = DangerRed,
    onError         = TextPrimary,
    background      = BackgroundDeepNavy,
    onBackground    = TextPrimary,
    surface         = BackgroundDark,
    onSurface       = TextPrimary,
    surfaceVariant  = BackgroundHeader,
    onSurfaceVariant = TextSecondary,
    outline         = BorderBlue,
)

@Composable
fun AegisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AegisColorScheme,
        content = content
    )
}
