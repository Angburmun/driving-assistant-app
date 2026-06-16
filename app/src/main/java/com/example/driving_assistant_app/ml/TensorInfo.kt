package com.example.driving_assistant_app.ml

data class TensorInfo(
    val index: Int,
    val shape: IntArray,
    val dataType: String,
    val numElements: Int
) {
    fun shapeAsString(): String = shape.joinToString(
        prefix = "[",
        postfix = "]",
        separator = ", "
    )
}