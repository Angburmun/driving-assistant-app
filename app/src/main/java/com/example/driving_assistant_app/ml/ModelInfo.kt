package com.example.driving_assistant_app.ml

data class ModelInfo(
    val modelPath: String,
    val inputTensors: List<TensorInfo>,
    val outputTensors: List<TensorInfo>
)