package com.example.shiftpaw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.shiftpaw.ui.navigation.ShiftPawNavHost
import com.example.shiftpaw.ui.screens.settings.ThemeViewModel
import com.example.shiftpaw.ui.theme.ShiftPawTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()
            ShiftPawTheme(darkTheme = isDarkMode) {
                ShiftPawNavHost(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = themeViewModel::toggleDarkMode
                )
            }
        }
    }
}
