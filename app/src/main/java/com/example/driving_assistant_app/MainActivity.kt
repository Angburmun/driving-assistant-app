package com.example.driving_assistant_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.driving_assistant_app.ui.AppRoot
import com.example.driving_assistant_app.ui.theme.DrivingassistantappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DrivingassistantappTheme  {
                AppRoot()
            }
        }
    }
}