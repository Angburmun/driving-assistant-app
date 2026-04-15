package com.example.driving_assistant_app.ml

import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class FrameAnalyzer(
    private val modelInputSize: Int,
    private val detector: YoloDetector,
    private val minAnalysisIntervalMs: Long = 150L,
    private val onFramePrepared: (
        sourceWidth: Int,
        sourceHeight: Int,
        rotationDegrees: Int,
        modelInputWidth: Int,
        modelInputHeight: Int
    ) -> Unit,
    private val onDetectionsReady: (
        detections: List<YoloDetection>,
        inferenceTimeMs: Float
    ) -> Unit
) : ImageAnalysis.Analyzer {

    private var lastAnalysisTimestamp = 0L

    override fun analyze(imageProxy: ImageProxy) {
        val now = SystemClock.elapsedRealtime()

        if (now - lastAnalysisTimestamp < minAnalysisIntervalMs) {
            imageProxy.close()
            return
        }

        lastAnalysisTimestamp = now

        try {
            val sourceWidth = imageProxy.width
            val sourceHeight = imageProxy.height
            val rotation = imageProxy.imageInfo.rotationDegrees

            val modelBitmap = FramePreprocessor.prepareModelBitmap(
                imageProxy = imageProxy,
                modelInputSize = modelInputSize
            )

            onFramePrepared(
                sourceWidth,
                sourceHeight,
                rotation,
                modelBitmap.width,
                modelBitmap.height
            )

            val result = detector.detect(modelBitmap)
            onDetectionsReady(result.detections, result.inferenceTimeMs)

            modelBitmap.recycle()
        } catch (e: Exception) {
            Log.e("FrameAnalyzer", "Failed to analyze frame", e)
        } finally {
            imageProxy.close()
        }
    }
}