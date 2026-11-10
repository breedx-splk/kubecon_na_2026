package com.lazybonez.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLazyBonezContent {
            val context = LocalContext.current
            SplashScreen(
                onFinished = {
                    startActivity(Intent(this, LaunchActivity::class.java))
                    finish()
                },
                loadEmojiCatalog = { EmojiCatalog.loadDuringStartup(context) },
            )
        }
    }
}
