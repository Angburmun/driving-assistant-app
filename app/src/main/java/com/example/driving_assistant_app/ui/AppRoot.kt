package com.example.driving_assistant_app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppRoot() {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        CameraScreen(
            modifier = Modifier.padding(innerPadding)
        )
    }
}