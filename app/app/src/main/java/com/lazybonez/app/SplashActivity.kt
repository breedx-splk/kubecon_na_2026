package com.lazybonez.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLazyBonezContent {
            SplashScreen(
                onFinished = {
                    startActivity(Intent(this, LaunchActivity::class.java))
                    finish()
                },
            )
        }
    }
}
