package com.example.driving_assistant_app.ml

object ModelConfig {
    const val MODEL_ASSET_PATH = "models/final.tflite"
    const val MODEL_INPUT_SIZE = 640
    const val NUM_CLASSES = 18

    val LABELS = listOf(
        "Límite de velocidad",
        "STOP",
        "Prohibido el paso",
        "Prohibido cambio de sentido",
        "Prohibido girar a la izquierda",
        "Prohibido girar a la derecha",
        "Dirección obligatoria",
        "Rotonda",
        "Autovía",
        "Final de autovía",
        "Autopista",
        "Final de autopista",
        "Vía con prioridad",
        "Ceda el paso",
        "Peligro",
        "Resalto elevado o baches",
        "Peligro por nieve",
        "Obras"
    )
}