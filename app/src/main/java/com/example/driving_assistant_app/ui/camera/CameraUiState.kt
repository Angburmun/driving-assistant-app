package com.example.driving_assistant_app.ui.camera

data class CameraUiState(
    val frameCount: Long = 0,
    val frameWidth: Int = 0,
    val frameHeight: Int = 0,
    val rotationDegrees: Int = 0,
    val preparedFrameCount: Long = 0,
    val modelInputWidth: Int = 0,
    val modelInputHeight: Int = 0
)