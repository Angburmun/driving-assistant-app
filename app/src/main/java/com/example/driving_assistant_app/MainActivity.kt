package com.example.driving_assistant_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.driving_assistant_app.ui.theme.DrivingassistantappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrivingassistantappTheme {
                MapScreen()
            }
        }
    }
}

@Composable
fun MapScreen() {
    OsmMapFullScreen(
        modifier = Modifier.fillMaxSize()
    )
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    DrivingassistantappTheme {
        MapScreen()
    }
}
