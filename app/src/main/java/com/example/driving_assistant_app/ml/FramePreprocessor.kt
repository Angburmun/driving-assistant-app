package com.example.driving_assistant_app.ml

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy

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

        val paddedBitmap = Bitmap.createBitmap(
            width + rowPadding / pixelStride,
            height,
            Bitmap.Config.ARGB_8888
        )
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

    fun centerCropToSquare(source: Bitmap): Bitmap {
        val side = minOf(source.width, source.height)
        val x = (source.width - side) / 2
        val y = (source.height - side) / 2

        val cropped = Bitmap.createBitmap(source, x, y, side, side)
        source.recycle()
        return cropped
    }

    fun resize(source: Bitmap, size: Int): Bitmap {
        val resized = Bitmap.createScaledBitmap(source, size, size, true)
        source.recycle()
        return resized
    }

    fun prepareModelBitmap(
        imageProxy: ImageProxy,
        modelInputSize: Int
    ): Bitmap {
        val rgbaBitmap = imageProxyToBitmap(imageProxy)
        val rotated = rotateBitmap(rgbaBitmap, imageProxy.imageInfo.rotationDegrees)
        val square = centerCropToSquare(rotated)
        return resize(square, modelInputSize)
    }
}