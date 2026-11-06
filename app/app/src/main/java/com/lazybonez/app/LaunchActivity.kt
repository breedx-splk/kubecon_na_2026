package com.lazybonez.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class LaunchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLazyBonezContent {
            LaunchScreen(
                onAbout = { startActivity(Intent(this, AboutActivity::class.java)) },
                onEnter = { startActivity(Intent(this, PlotifyActivity::class.java)) },
            )
        }
    }
}
