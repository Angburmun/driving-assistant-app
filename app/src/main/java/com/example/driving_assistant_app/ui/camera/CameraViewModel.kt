package com.example.driving_assistant_app.ui.camera

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driving_assistant_app.ml.LiteRtModelInspector
import com.example.driving_assistant_app.ml.YoloDetection
import com.example.driving_assistant_app.ml.ModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private var modelLoadAttempted = false

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

    fun loadModelIfNeeded(context: Context) {
        if (modelLoadAttempted) return
        modelLoadAttempted = true

        _uiState.value = _uiState.value.copy(
            isModelLoading = true,
            modelError = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val modelInfo = LiteRtModelInspector.inspectModel(
                    context = context.applicationContext,
                    assetPath = ModelConfig.MODEL_ASSET_PATH
                )

                _uiState.value = _uiState.value.copy(
                    isModelLoading = false,
                    modelInfo = modelInfo,
                    modelError = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isModelLoading = false,
                    modelInfo = null,
                    modelError = e.message ?: "Unknown model loading error"
                )
            }
        }
    }

    fun onDetectionsReady(
        detections: List<YoloDetection>,
        inferenceTimeMs: Float
    ) {
        val current = _uiState.value
        _uiState.value = current.copy(
            detections = detections,
            inferenceTimeMs = inferenceTimeMs
        )
    }
}