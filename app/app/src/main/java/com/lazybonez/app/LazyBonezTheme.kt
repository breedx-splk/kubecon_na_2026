package com.lazybonez.app

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme

fun ComponentActivity.setLazyBonezContent(content: @Composable () -> Unit) {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
    )
    setContent {
        LazyBonezTheme(content = content)
    }
}

@Composable
private fun LazyBonezTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = BloodRed,
            onPrimary = BoneWhite,
            secondary = Ectoplasm,
            onSecondary = Obsidian,
            background = Obsidian,
            onBackground = BoneWhite,
            surface = NightSurface,
            onSurface = BoneWhite,
        ),
        content = content,
    )
}

val Obsidian = Color(0xFF100B10)
val NightSurface = Color(0xFF1A121A)
val BoneWhite = Color(0xFFF4EDE4)
val BloodRed = Color(0xFFD7263D)
val Ectoplasm = Color(0xFF9B6B9E)
