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

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsStateWithLifecycle()
            val userProfile by mainViewModel.userProfile.collectAsStateWithLifecycle()
            val usePixelFont = userProfile?.usePixelFont ?: true

            FocusGroveTheme(darkTheme = isDarkTheme, usePixelFont = usePixelFont) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FocusGroveApp(viewModel = mainViewModel)
                }
            }
        }
    }
}
