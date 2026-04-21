package com.example.driving_assistant_app.ui

import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.driving_assistant_app.ml.FrameAnalyzer
import com.example.driving_assistant_app.ml.ModelConfig
import com.example.driving_assistant_app.ml.YoloDetection
import com.example.driving_assistant_app.ml.YoloDetector
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onFrameAnalyzed: (width: Int, height: Int, rotationDegrees: Int) -> Unit,
    onFramePrepared: (modelInputWidth: Int, modelInputHeight: Int) -> Unit,
    onDetectionsReady: (detections: List<YoloDetection>, inferenceTimeMs: Float) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val detector = remember { YoloDetector(context.applicationContext, ModelConfig.MODEL_ASSET_PATH) }

    DisposableEffect(lifecycleOwner, previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        var imageAnalysis: ImageAnalysis? = null

        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.surfaceProvider = previewView.surfaceProvider }

            val frameAnalyzer = FrameAnalyzer(
                modelInputSize = ModelConfig.MODEL_INPUT_SIZE,
                detector = detector,
                minAnalysisIntervalMs = 150L,
                onFramePrepared = { sourceWidth, sourceHeight, rotation, modelWidth, modelHeight ->
                    onFrameAnalyzed(sourceWidth, sourceHeight, rotation)
                    onFramePrepared(modelWidth, modelHeight)
                },
                onDetectionsReady = onDetectionsReady
            )

            val localImageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            localImageAnalysis.setAnalyzer(cameraExecutor, frameAnalyzer)
            imageAnalysis = localImageAnalysis

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    localImageAnalysis
                )
            } catch (e: Exception) {
                Log.e("CameraPreview", "Failed to bind camera use cases", e)
            }
        }

        cameraProviderFuture.addListener(
            listener,
            ContextCompat.getMainExecutor(context)
        )

        onDispose {
            try {
                imageAnalysis?.clearAnalyzer()
            } catch (e: Exception) {
                Log.e("CameraPreview", "Failed to clear analyzer", e)
            }

            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            } catch (e: Exception) {
                Log.e("CameraPreview", "Failed to unbind camera", e)
            }

            cameraExecutor.shutdown()
            try {
                cameraExecutor.awaitTermination(1, TimeUnit.SECONDS)
            } catch (e: Exception) {
                Log.e("CameraPreview", "Failed while waiting executor shutdown", e)
            }

            detector.close()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { previewView }
    )
}