package com.example.driving_assistant_app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun CameraScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Camera screen placeholder",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}