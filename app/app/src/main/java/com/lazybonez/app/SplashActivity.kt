package com.lazybonez.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext

class SplashActivity : ComponentActivity() {
    private val requestLocalNetworkPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        showSplash(canAccessLocalNetwork = granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 37 &&
            checkSelfPermission(Manifest.permission.ACCESS_LOCAL_NETWORK) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocalNetworkPermission.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
        } else {
            showSplash(canAccessLocalNetwork = true)
        }
    }

    private fun showSplash(canAccessLocalNetwork: Boolean) {
        setLazyBonezContent {
            val context = LocalContext.current
            SplashScreen(
                onFinished = {
                    startActivity(Intent(this, LaunchActivity::class.java))
                    finish()
                },
                loadEmojiCatalog = {
                    if (canAccessLocalNetwork) {
                        EmojiCatalog.loadDuringStartup(context)
                    } else {
                        EmojiCatalog.load(context)
                    }
                },
            )
        }
    }
}
