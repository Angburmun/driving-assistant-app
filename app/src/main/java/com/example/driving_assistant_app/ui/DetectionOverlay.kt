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

        val rotatedWidth =
            if (rotationDegrees == 90 || rotationDegrees == 270) {
                sourceFrameHeight.toFloat()
            } else {
                sourceFrameWidth.toFloat()
            }

        val rotatedHeight =
            if (rotationDegrees == 90 || rotationDegrees == 270) {
                sourceFrameWidth.toFloat()
            } else {
                sourceFrameHeight.toFloat()
            }

        val modelSize = ModelConfig.MODEL_INPUT_SIZE.toFloat()

        // This must match the new preprocessing step
        val modelScale = min(
            modelSize / rotatedWidth,
            modelSize / rotatedHeight
        )
        val padX = (modelSize - rotatedWidth * modelScale) / 2f
        val padY = (modelSize - rotatedHeight * modelScale) / 2f

        // This must match PreviewView.ScaleType.FILL_CENTER
        val previewScale = max(
            size.width / rotatedWidth,
            size.height / rotatedHeight
        )
        val previewOffsetX = (size.width - rotatedWidth * previewScale) / 2f
        val previewOffsetY = (size.height - rotatedHeight * previewScale) / 2f

        detections.forEach { det ->
            // Undo letterbox: model space -> rotated image space
            val imageLeft = ((det.left - padX) / modelScale).coerceIn(0f, rotatedWidth)
            val imageTop = ((det.top - padY) / modelScale).coerceIn(0f, rotatedHeight)
            val imageRight = ((det.right - padX) / modelScale).coerceIn(0f, rotatedWidth)
            val imageBottom = ((det.bottom - padY) / modelScale).coerceIn(0f, rotatedHeight)

            if (imageRight <= imageLeft || imageBottom <= imageTop) return@forEach

            // Rotated image space -> preview space
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
                labelTop + textHeight,
                textPaint
            )
        }
    }
}