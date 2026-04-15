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
import com.example.driving_assistant_app.ml.YoloDetection
import com.example.driving_assistant_app.ml.YoloDetector
import java.util.concurrent.Executors

private const val MODEL_INPUT_SIZE = 640
private const val MODEL_ASSET_PATH = "models/yolo11n_float32.tflite"

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
    val detector = remember { YoloDetector(context, MODEL_ASSET_PATH) }

    DisposableEffect(lifecycleOwner, previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.surfaceProvider = previewView.surfaceProvider }

            val frameAnalyzer = FrameAnalyzer(
                modelInputSize = MODEL_INPUT_SIZE,
                detector = detector,
                minAnalysisIntervalMs = 150L,
                onFramePrepared = { sourceWidth, sourceHeight, rotation, modelWidth, modelHeight ->
                    onFrameAnalyzed(sourceWidth, sourceHeight, rotation)
                    onFramePrepared(modelWidth, modelHeight)
                },
                onDetectionsReady = onDetectionsReady
            )

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor, frameAnalyzer)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
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
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            } catch (e: Exception) {
                Log.e("CameraPreview", "Failed to unbind camera", e)
            }
            detector.close()
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { previewView }
    )
}