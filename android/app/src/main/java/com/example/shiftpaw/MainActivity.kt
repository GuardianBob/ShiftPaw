package com.example.shiftpaw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.shiftpaw.ui.navigation.ShiftPawNavHost
import com.example.shiftpaw.ui.theme.ShiftPawTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShiftPawTheme {
                ShiftPawNavHost()
            }
        }
    }
}
