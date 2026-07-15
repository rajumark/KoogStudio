package com.koog.studio

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun App() {
    MaterialTheme(
        colorScheme = lightColorScheme()
    ) {
        ChatScreen()
    }
}
