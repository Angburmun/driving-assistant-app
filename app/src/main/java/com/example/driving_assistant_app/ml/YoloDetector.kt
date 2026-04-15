package com.example.driving_assistant_app.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.SystemClock
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min

class YoloDetector(
    context: Context,
    private val modelPath: String
) {
    private val interpreter: Interpreter

    init {
        val modelBuffer = loadModelFile(context.applicationContext, modelPath)
        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }
        interpreter = Interpreter(modelBuffer, options)
    }

    fun detect(
        bitmap: Bitmap,
        confidenceThreshold: Float = 0.35f,
        iouThreshold: Float = 0.45f,
        maxDetections: Int = 20
    ): YoloInferenceResult {
        require(bitmap.width == 640 && bitmap.height == 640) {
            "Expected 640x640 bitmap, got ${bitmap.width}x${bitmap.height}"
        }

        val inputBuffer = bitmapToInputBuffer(bitmap)

        val output = Array(1) {
            Array(84) {
                FloatArray(8400)
            }
        }

        val start = SystemClock.elapsedRealtimeNanos()
        interpreter.run(inputBuffer, output)
        val inferenceTimeMs = (SystemClock.elapsedRealtimeNanos() - start) / 1_000_000f

        val rawDetections = parseOutput(output[0], confidenceThreshold)
        val finalDetections = nonMaxSuppression(
            detections = rawDetections,
            iouThreshold = iouThreshold,
            maxDetections = maxDetections
        )

        return YoloInferenceResult(
            detections = finalDetections,
            inferenceTimeMs = inferenceTimeMs
        )
    }

    fun close() {
        interpreter.close()
    }

    private fun bitmapToInputBuffer(bitmap: Bitmap): ByteBuffer {
        val inputBuffer = ByteBuffer.allocateDirect(1 * 640 * 640 * 3 * 4)
            .order(ByteOrder.nativeOrder())

        val pixels = IntArray(640 * 640)
        bitmap.getPixels(pixels, 0, 640, 0, 0, 640, 640)

        for (pixel in pixels) {
            inputBuffer.putFloat(Color.red(pixel) / 255f)
            inputBuffer.putFloat(Color.green(pixel) / 255f)
            inputBuffer.putFloat(Color.blue(pixel) / 255f)
        }

        inputBuffer.rewind()
        return inputBuffer
    }

    private fun parseOutput(
        output: Array<FloatArray>,
        confidenceThreshold: Float
    ): List<YoloDetection> {
        val detections = mutableListOf<YoloDetection>()
        val numClasses = 80
        val numCandidates = 8400

        for (i in 0 until numCandidates) {
            val centerX = output[0][i]
            val centerY = output[1][i]
            val width = output[2][i]
            val height = output[3][i]

            var bestClassIndex = -1
            var bestScore = 0f

            for (classIndex in 0 until numClasses) {
                val score = output[4 + classIndex][i]
                if (score > bestScore) {
                    bestScore = score
                    bestClassIndex = classIndex
                }
            }

            if (bestScore < confidenceThreshold || bestClassIndex < 0) continue

            val left = (centerX - width / 2f).coerceIn(0f, 640f)
            val top = (centerY - height / 2f).coerceIn(0f, 640f)
            val right = (centerX + width / 2f).coerceIn(0f, 640f)
            val bottom = (centerY + height / 2f).coerceIn(0f, 640f)

            if (right <= left || bottom <= top) continue

            detections.add(
                YoloDetection(
                    classIndex = bestClassIndex,
                    className = CocoLabels.labels.getOrElse(bestClassIndex) { "class_$bestClassIndex" },
                    score = bestScore,
                    left = left,
                    top = top,
                    right = right,
                    bottom = bottom
                )
            )
        }

        return detections.sortedByDescending { it.score }
    }

    private fun nonMaxSuppression(
        detections: List<YoloDetection>,
        iouThreshold: Float,
        maxDetections: Int
    ): List<YoloDetection> {
        val selected = mutableListOf<YoloDetection>()

        for (candidate in detections) {
            val shouldKeep = selected.none { kept ->
                kept.classIndex == candidate.classIndex &&
                        intersectionOverUnion(kept, candidate) > iouThreshold
            }

            if (shouldKeep) {
                selected.add(candidate)
            }

            if (selected.size >= maxDetections) break
        }

        return selected
    }

    private fun intersectionOverUnion(a: YoloDetection, b: YoloDetection): Float {
        val interLeft = max(a.left, b.left)
        val interTop = max(a.top, b.top)
        val interRight = min(a.right, b.right)
        val interBottom = min(a.bottom, b.bottom)

        val interWidth = max(0f, interRight - interLeft)
        val interHeight = max(0f, interBottom - interTop)
        val interArea = interWidth * interHeight

        val areaA = (a.right - a.left) * (a.bottom - a.top)
        val areaB = (b.right - b.left) * (b.bottom - b.top)
        val union = areaA + areaB - interArea

        return if (union <= 0f) 0f else interArea / union
    }

    private fun loadModelFile(
        context: Context,
        assetPath: String
    ): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(assetPath)
        FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel = inputStream.channel
            return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                fileDescriptor.startOffset,
                fileDescriptor.declaredLength
            )
        }
    }
}