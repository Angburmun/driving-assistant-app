package com.example.driving_assistant_app.ml

data class YoloDetection(
    val classIndex: Int,
    val className: String,
    val score: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

data class YoloInferenceResult(
    val detections: List<YoloDetection>,
    val inferenceTimeMs: Float
)