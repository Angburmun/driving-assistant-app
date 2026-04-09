package com.example.driving_assistant_app.ml

import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class FrameAnalyzer(
    private val modelInputSize: Int,
    private val minAnalysisIntervalMs: Long = 100L,
    private val onFramePrepared: (
        sourceWidth: Int,
        sourceHeight: Int,
        rotationDegrees: Int,
        modelInputWidth: Int,
        modelInputHeight: Int
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

            modelBitmap.recycle()
        } catch (e: Exception) {
            Log.e("FrameAnalyzer", "Failed to preprocess frame", e)
        } finally {
            imageProxy.close()
        }
    }
}