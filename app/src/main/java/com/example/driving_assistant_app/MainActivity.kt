package com.example.driving_assistant_app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.driving_assistant_app.ui.theme.DrivingassistantappTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted

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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen() {
    val locationPermissionState =
        rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Lanzamos la petición de permiso al entrar en la pantalla
    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    if (locationPermissionState.status.isGranted) {
        OsmMapFullScreen(
            modifier = Modifier.fillMaxSize(),
            showMyLocation = true
        )
    } else {
        OsmMapFullScreen(
            modifier = Modifier.fillMaxSize(),
            showMyLocation = false
        )
    }

}


@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    DrivingassistantappTheme {
        MapScreen()
    }
}
