package com.example.driving_assistant_app.ui.camera

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun onFrameAnalyzed(
        width: Int,
        height: Int,
        rotationDegrees: Int
    ) {
        val current = _uiState.value
        _uiState.value = current.copy(
            frameCount = current.frameCount + 1,
            frameWidth = width,
            frameHeight = height,
            rotationDegrees = rotationDegrees
        )
    }

    fun onFramePrepared(
        modelInputWidth: Int,
        modelInputHeight: Int
    ) {
        val current = _uiState.value
        _uiState.value = current.copy(
            preparedFrameCount = current.preparedFrameCount + 1,
            modelInputWidth = modelInputWidth,
            modelInputHeight = modelInputHeight
        )
    }
}