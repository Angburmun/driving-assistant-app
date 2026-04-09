package com.example.driving_assistant_app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.driving_assistant_app.ui.camera.CameraViewModel

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        Box(modifier = modifier.fillMaxSize()) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onFrameAnalyzed = { width, height, rotation ->
                    viewModel.onFrameAnalyzed(width, height, rotation)
                },
                onFramePrepared = { modelWidth, modelHeight ->
                    viewModel.onFramePrepared(modelWidth, modelHeight)
                }
            )

            CameraInfoCard(
                frameCount = uiState.frameCount,
                width = uiState.frameWidth,
                height = uiState.frameHeight,
                rotation = uiState.rotationDegrees,
                preparedFrameCount = uiState.preparedFrameCount,
                modelInputWidth = uiState.modelInputWidth,
                modelInputHeight = uiState.modelInputHeight,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(16.dp)
            )
        }
    } else {
        PermissionContent(
            modifier = modifier.fillMaxSize(),
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )
    }
}

@Composable
private fun PermissionContent(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Camera access is required to show the live preview.",
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onRequestPermission) {
                Text("Grant camera permission")
            }
        }
    }
}

@Composable
private fun CameraInfoCard(
    frameCount: Long,
    width: Int,
    height: Int,
    rotation: Int,
    preparedFrameCount: Long,
    modelInputWidth: Int,
    modelInputHeight: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Live analysis",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Frames analyzed: $frameCount",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Camera frame: ${width}×${height}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Rotation: $rotation°",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Prepared frames: $preparedFrameCount",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Model input: ${modelInputWidth}×${modelInputHeight}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}