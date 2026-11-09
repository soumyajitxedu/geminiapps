package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FocusGroveApp
import com.example.ui.MainViewModel
import com.example.ui.theme.FocusGroveTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val reqConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(reqConfig)
            MobileAds.initialize(this) {}
        } catch (_: Exception) {}
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsStateWithLifecycle()
            val usePixelFont by mainViewModel.usePixelFont.collectAsStateWithLifecycle()
            val equippedTheme by mainViewModel.equippedTheme.collectAsStateWithLifecycle()
            val equippedFont by mainViewModel.equippedFont.collectAsStateWithLifecycle()

            FocusGroveTheme(
                darkTheme = isDarkTheme,
                usePixelFont = usePixelFont,
                equippedTheme = equippedTheme,
                equippedFont = equippedFont
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FocusGroveApp(viewModel = mainViewModel)
                }
            }
        }
    }
}
