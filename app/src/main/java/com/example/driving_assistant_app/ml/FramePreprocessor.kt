package com.example.driving_assistant_app.ml

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import androidx.camera.core.ImageProxy
import kotlin.math.roundToInt
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

object FramePreprocessor {

    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val plane = imageProxy.planes[0]
        val buffer = plane.buffer
        buffer.rewind()

        val width = imageProxy.width
        val height = imageProxy.height
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width

        val paddedBitmap = createBitmap(width + rowPadding / pixelStride, height)
        paddedBitmap.copyPixelsFromBuffer(buffer)

        return if (rowPadding == 0) {
            paddedBitmap
        } else {
            val cropped = Bitmap.createBitmap(paddedBitmap, 0, 0, width, height)
            paddedBitmap.recycle()
            cropped
        }
    }

    fun rotateBitmap(source: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return source

        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        }

        val rotated = Bitmap.createBitmap(
            source,
            0,
            0,
            source.width,
            source.height,
            matrix,
            true
        )

        source.recycle()
        return rotated
    }

    fun letterboxResize(source: Bitmap, size: Int): Bitmap {
        val srcWidth = source.width
        val srcHeight = source.height

        val scale = minOf(
            size / srcWidth.toFloat(),
            size / srcHeight.toFloat()
        )

        val dstWidth = (srcWidth * scale).roundToInt()
        val dstHeight = (srcHeight * scale).roundToInt()

        val left = (size - dstWidth) / 2
        val top = (size - dstHeight) / 2

        val output = createBitmap(size, size)
        val canvas = Canvas(output)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)

        // Gray padding works well for YOLO-style letterboxing
        canvas.drawARGB(255, 114, 114, 114)

        val resized = source.scale(dstWidth, dstHeight)
        canvas.drawBitmap(resized, left.toFloat(), top.toFloat(), paint)

        source.recycle()
        resized.recycle()

        return output
    }

    fun prepareModelBitmap(
        imageProxy: ImageProxy,
        modelInputSize: Int
    ): Bitmap {
        val rgbaBitmap = imageProxyToBitmap(imageProxy)
        val rotated = rotateBitmap(rgbaBitmap, imageProxy.imageInfo.rotationDegrees)
        return letterboxResize(rotated, modelInputSize)
    }
}