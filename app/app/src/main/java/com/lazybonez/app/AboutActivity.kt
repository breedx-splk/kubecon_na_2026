package com.lazybonez.app

import android.os.Bundle
import androidx.activity.ComponentActivity

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLazyBonezContent {
            AboutScreen(onBack = ::finish)
        }
    }
}
