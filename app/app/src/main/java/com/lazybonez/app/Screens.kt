package com.lazybonez.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

@Composable
fun SplashScreen(onFinished: () -> Unit, loadEmojiCatalog: suspend () -> Unit) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        coroutineScope {
            val catalogLoad = async { loadEmojiCatalog() }
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 5_000, easing = LinearEasing),
            )
            catalogLoad.await()
        }
        onFinished()
    }

    HorrorBackdrop {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "💀", fontSize = 68.sp)
            Spacer(modifier = Modifier.height(12.dp))
            AppWordmark()
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "IDEAS FROM THE DARK",
                color = Ectoplasm,
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 3.sp,
            )
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "SUMMONING INSPIRATION",
                color = BoneWhite.copy(alpha = 0.72f),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
            )
            Spacer(modifier = Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .height(8.dp)
                    .background(NightSurface, RoundedCornerShape(50)),
                color = BloodRed,
                trackColor = BoneWhite.copy(alpha = 0.14f),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "${(progress.value * 100).toInt()}%",
                color = Ectoplasm,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
fun LaunchScreen(onAbout: () -> Unit, onEnter: () -> Unit) {
    HorrorBackdrop {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "💀", fontSize = 58.sp)
            Spacer(modifier = Modifier.height(8.dp))
            AppWordmark()
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "LOW-BUDGET HORROR.\nUNREASONABLY GOOD NIGHTMARES.",
                color = BoneWhite.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 25.sp,
                letterSpacing = 1.1.sp,
            )
            Spacer(modifier = Modifier.height(40.dp))
            BonezButton(label = "ENTER", onClick = onEnter)
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onAbout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Ectoplasm),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BoneWhite),
            ) {
                Text("ABOUT", letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    HorrorBackdrop {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(text = "THE IDEA ::", color = Ectoplasm, letterSpacing = 3.sp)
            Text(text = "FROM YOUR POCKET\nTO THE BIG SCREEN", color = Ectoplasm, letterSpacing = 3.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "The stupidest,\nmost low-effort way\nto begin writing a horror film.",
                color = BoneWhite,
                fontSize = 34.sp,
                lineHeight = 39.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Lazy Bonez 💀 turns random emojis into low-budget horror movie inspiration. Pick a few strange symbols, keep the production scrappy, and let the nightmare do the rest.",
                color = BoneWhite.copy(alpha = 0.78f),
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 28.sp,
            )
            Spacer(modifier = Modifier.height(36.dp))
            BonezButton(label = "BACK", onClick = onBack)
        }
    }
}

@Composable
fun PlotifyScreen(onBack: () -> Unit) {
    EmojiSlotMachine(onBack = onBack)
}

@Composable
private fun AppWordmark() {
    Text(
        text = "LAZY BONEZ",
        color = BoneWhite,
        fontSize = 36.sp,
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Black,
        letterSpacing = 3.sp,
    )
}

@Composable
private fun BonezButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BloodRed,
            contentColor = BoneWhite,
        ),
    ) {
        Text(label, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HorrorBackdrop(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Obsidian, NightSurface, Obsidian),
                ),
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        content = content,
    )
}
