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
        viewModel.loadModelIfNeeded(context)
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
                },
                onDetectionsReady = { detections, inferenceTimeMs ->
                    viewModel.onDetectionsReady(detections, inferenceTimeMs)
                }
            )

            DetectionOverlay(
                detections = uiState.detections,
                sourceFrameWidth = uiState.frameWidth,
                sourceFrameHeight = uiState.frameHeight,
                rotationDegrees = uiState.rotationDegrees,
                modifier = Modifier.fillMaxSize()
            )

            CameraInfoCard(
                frameCount = uiState.frameCount,
                width = uiState.frameWidth,
                height = uiState.frameHeight,
                rotation = uiState.rotationDegrees,
                preparedFrameCount = uiState.preparedFrameCount,
                modelInputWidth = uiState.modelInputWidth,
                modelInputHeight = uiState.modelInputHeight,
                isModelLoading = uiState.isModelLoading,
                modelInfo = uiState.modelInfo,
                modelError = uiState.modelError,
                detections = uiState.detections,
                inferenceTimeMs = uiState.inferenceTimeMs,
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
    isModelLoading: Boolean,
    modelInfo: com.example.driving_assistant_app.ml.ModelInfo?,
    modelError: String?,
    detections: List<com.example.driving_assistant_app.ml.YoloDetection>,
    inferenceTimeMs: Float,
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
                text = "Prepared input: ${modelInputWidth}×${modelInputHeight}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Model",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Detections: ${detections.size}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Inference: ${"%.1f".format(inferenceTimeMs)} ms",
                style = MaterialTheme.typography.bodyMedium
            )

            detections.take(3).forEach { det ->
                Text(
                    text = "${det.className} ${"%.2f".format(det.score)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "Inference: ${"%.1f".format(inferenceTimeMs)} ms",
                style = MaterialTheme.typography.bodyMedium
            )

            when {
                isModelLoading -> {
                    Text(
                        text = "Loading model...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                modelError != null -> {
                    Text(
                        text = "Model error: $modelError",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                modelInfo != null -> {
                    Text(
                        text = "Path: ${modelInfo.modelPath}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "Inputs: ${modelInfo.inputTensors.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    modelInfo.inputTensors.forEach { tensor ->
                        Text(
                            text = "In[${tensor.index}] ${tensor.dataType} ${tensor.shapeAsString()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        text = "Outputs: ${modelInfo.outputTensors.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    modelInfo.outputTensors.forEach { tensor ->
                        Text(
                            text = "Out[${tensor.index}] ${tensor.dataType} ${tensor.shapeAsString()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                else -> {
                    Text(
                        text = "Model not loaded yet.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}