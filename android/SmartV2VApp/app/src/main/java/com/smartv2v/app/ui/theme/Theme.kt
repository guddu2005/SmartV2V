package com.smartv2v.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = NeoObsidian,
    secondary = EmeraldAccent,
    background = NeoLightBg,
    surface = NeoCardSurface,
    onPrimary = NeoCardSurface,
    onSecondary = NeoCardSurface,
    onBackground = NeoTextPrimary,
    onSurface = NeoTextPrimary,
    error = AlertRed
)

@Composable
fun SmartV2VTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
