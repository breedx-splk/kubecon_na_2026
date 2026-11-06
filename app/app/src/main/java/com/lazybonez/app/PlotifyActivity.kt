package com.lazybonez.app

import android.os.Bundle
import androidx.activity.ComponentActivity

class PlotifyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLazyBonezContent {
            PlotifyScreen(onBack = ::finish)
        }
    }
}
