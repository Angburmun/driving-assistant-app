package com.example.driving_assistant_app.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.driving_assistant_app.ml.YoloDetection

@Composable
fun DetectionOverlay(
    detections: List<YoloDetection>,
    modifier: Modifier = Modifier
) {
    val boxColor = Color(0xFF00E676)
    val labelBg = Color(0xCC000000)
    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 32f
            isAntiAlias = true
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val squareSize = size.width.coerceAtMost(size.height)
        val offsetX = (size.width - squareSize) / 2f
        val offsetY = (size.height - squareSize) / 2f
        val scale = squareSize / 640f

        detections.forEach { det ->
            val left = offsetX + det.left * scale
            val top = offsetY + det.top * scale
            val right = offsetX + det.right * scale
            val bottom = offsetY + det.bottom * scale

            drawRect(
                color = boxColor,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                style = Stroke(width = 4f)
            )

            val label = "${det.className} ${"%.2f".format(det.score)}"
            val textWidth = textPaint.measureText(label)
            val textHeight = textPaint.textSize

            drawRect(
                color = labelBg,
                topLeft = Offset(left, (top - textHeight - 12f).coerceAtLeast(0f)),
                size = Size(textWidth + 16f, textHeight + 12f)
            )

            drawContext.canvas.nativeCanvas.drawText(
                label,
                left + 8f,
                (top - 12f).coerceAtLeast(textHeight),
                textPaint
            )
        }
    }
}