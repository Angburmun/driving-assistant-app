package com.example.driving_assistant_app.ui.camera

import com.example.driving_assistant_app.ml.ModelInfo

data class CameraUiState(
    val frameCount: Long = 0,
    val frameWidth: Int = 0,
    val frameHeight: Int = 0,
    val rotationDegrees: Int = 0,
    val preparedFrameCount: Long = 0,
    val modelInputWidth: Int = 0,
    val modelInputHeight: Int = 0,
    val isModelLoading: Boolean = false,
    val modelInfo: ModelInfo? = null,
    val modelError: String? = null,
    val topClassIndex: Int = -1,
    val topScore: Float = 0f,
    val inferenceTimeMs: Float = 0f
)