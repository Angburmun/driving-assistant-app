package com.example.driving_assistant_app.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object LiteRtModelInspector {

    fun inspectModel(
        context: Context,
        assetPath: String
    ): ModelInfo {
        val modelBuffer = loadModelFile(context, assetPath)

        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }

        val interpreter = Interpreter(modelBuffer, options)

        try {
            interpreter.allocateTensors()

            val inputTensors = (0 until interpreter.inputTensorCount).map { index ->
                val tensor = interpreter.getInputTensor(index)
                TensorInfo(
                    index = index,
                    shape = tensor.shape(),
                    dataType = tensor.dataType().name,
                    numElements = tensor.numElements()
                )
            }

            val outputTensors = (0 until interpreter.outputTensorCount).map { index ->
                val tensor = interpreter.getOutputTensor(index)
                TensorInfo(
                    index = index,
                    shape = tensor.shape(),
                    dataType = tensor.dataType().name,
                    numElements = tensor.numElements()
                )
            }

            return ModelInfo(
                modelPath = assetPath,
                inputTensors = inputTensors,
                outputTensors = outputTensors
            )
        } finally {
            interpreter.close()
        }
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