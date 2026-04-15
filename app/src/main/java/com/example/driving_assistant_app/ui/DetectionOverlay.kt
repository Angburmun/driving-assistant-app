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
import com.example.driving_assistant_app.ml.ModelConfig
import com.example.driving_assistant_app.ml.YoloDetection
import kotlin.math.max
import kotlin.math.min

@Composable
fun DetectionOverlay(
    detections: List<YoloDetection>,
    sourceFrameWidth: Int,
    sourceFrameHeight: Int,
    rotationDegrees: Int,
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
        if (sourceFrameWidth <= 0 || sourceFrameHeight <= 0) return@Canvas

        val rotatedFrameWidth =
            if (rotationDegrees == 90 || rotationDegrees == 270) sourceFrameHeight.toFloat()
            else sourceFrameWidth.toFloat()

        val rotatedFrameHeight =
            if (rotationDegrees == 90 || rotationDegrees == 270) sourceFrameWidth.toFloat()
            else sourceFrameHeight.toFloat()

        val squareSide = min(rotatedFrameWidth, rotatedFrameHeight)
        val cropLeft = (rotatedFrameWidth - squareSide) / 2f
        val cropTop = (rotatedFrameHeight - squareSide) / 2f

        val previewScale = max(
            size.width / rotatedFrameWidth,
            size.height / rotatedFrameHeight
        )

        val previewOffsetX = (size.width - rotatedFrameWidth * previewScale) / 2f
        val previewOffsetY = (size.height - rotatedFrameHeight * previewScale) / 2f

        detections.forEach { det ->
            val imageLeft = cropLeft + det.left * squareSide / ModelConfig.MODEL_INPUT_SIZE
            val imageTop = cropTop + det.top * squareSide / ModelConfig.MODEL_INPUT_SIZE
            val imageRight = cropLeft + det.right * squareSide / ModelConfig.MODEL_INPUT_SIZE
            val imageBottom = cropTop + det.bottom * squareSide / ModelConfig.MODEL_INPUT_SIZE

            val viewLeft = previewOffsetX + imageLeft * previewScale
            val viewTop = previewOffsetY + imageTop * previewScale
            val viewRight = previewOffsetX + imageRight * previewScale
            val viewBottom = previewOffsetY + imageBottom * previewScale

            drawRect(
                color = boxColor,
                topLeft = Offset(viewLeft, viewTop),
                size = Size(viewRight - viewLeft, viewBottom - viewTop),
                style = Stroke(width = 4f)
            )

            val label = "${det.className} ${"%.2f".format(det.score)}"
            val textWidth = textPaint.measureText(label)
            val textHeight = textPaint.textSize

            val labelTop = (viewTop - textHeight - 12f).coerceAtLeast(0f)

            drawRect(
                color = labelBg,
                topLeft = Offset(viewLeft, labelTop),
                size = Size(textWidth + 16f, textHeight + 12f)
            )

            drawContext.canvas.nativeCanvas.drawText(
                label,
                viewLeft + 8f,
                (labelTop + textHeight),
                textPaint
            )
        }
    }
}