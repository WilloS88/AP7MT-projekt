package com.example.financetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.financetracker.ui.navigation.VydajnikApp
import com.example.financetracker.ui.theme.VydajnikTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VydajnikTheme(dynamicColor = false) {
                VydajnikApp()
            }
        }
    }
}
