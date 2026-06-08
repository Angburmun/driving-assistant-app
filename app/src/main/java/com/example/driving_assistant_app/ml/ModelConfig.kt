package com.example.driving_assistant_app.ml

object ModelConfig {
    const val MODEL_ASSET_PATH = "models/hybrid_float32.tflite"
    const val MODEL_INPUT_SIZE = 640
    const val NUM_CLASSES = 22

    val LABELS = listOf(
        "Max speed 20",
        "Max speed 30",
        "Max speed 40",
        "Max speed 50",
        "Max speed 60",
        "Max speed 70",
        "Max speed 80",
        "Max speed 90",
        "Max speed 100",
        "Max speed 120",
        "Stop",
        "Yield",
        "Prohibition",
        "Mandatory direction",
        "Warning",
        "Pedestrian crossing",
        "Work zone",
        "Roundabout",
        "Priority",
        "End restriction",
        "No right turn",
        "Dual carriageway",
        "Speed bump"

    )
}