package com.example.zxingcppdemo

import android.graphics.PointF
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.MutableState
import androidx.core.graphics.toPointF
import com.example.zxingcppdemo.utils.saveImage
import com.zxingcpp.BarcodeReader

class ZXingBarcodeAnalyzer(
    private val scanResult: (resultText: String, points: List<PointF>?, image: ImageProxy) -> Unit,
    private val previewOverlay: PreviewOverlay,
    private val scanOptions: MutableState<ScanOptions>
) : ImageAnalysis.Analyzer {

    private val reader = BarcodeReader()

    override fun analyze(imageProxy: ImageProxy) {
        if (scanOptions.value.readerOptions != reader.options) {
            reader.apply {
                this.options = scanOptions.value.readerOptions
            }
        }

        // Early exit: image analysis is in paused state
        if (scanOptions.value.pauseEnabled) {
            imageProxy.close()
            return
        }

        if (scanOptions.value.doSaveImage) {
            scanOptions.value = scanOptions.value.copy(doSaveImage = false)
            saveImage(imageProxy)
        }

        val cropSize = imageProxy.height / 3 * 2
        val cropRect = if (scanOptions.value.cropEnabled)
            Rect(
                (imageProxy.width - cropSize) / 2,
                (imageProxy.height - cropSize) / 2,
                (imageProxy.width - cropSize) / 2 + cropSize,
                (imageProxy.height - cropSize) / 2 + cropSize
            )
        else
            Rect(0, 0, imageProxy.width, imageProxy.height)

        imageProxy.setCropRect(cropRect)

        var resultPoints: List<PointF>? = null

        val resultText = try {
            val result = reader.read(imageProxy)
            resultPoints = result?.position?.let {
                listOf(
                    it.topLeft,
                    it.topRight,
                    it.bottomRight,
                    it.bottomLeft
                ).map { p ->
                    p.toPointF()
                }
            }
            (result?.let {
                "${it.format} (${it.contentType}): " +
                        "${
                            if (it.contentType != BarcodeReader.ContentType.BINARY) it.text else it.bytes!!.joinToString(
                                separator = ""
                            ) { v -> "%02x".format(v) }
                        }"
            }
                ?: "")
        } catch (e: Throwable) {
            e.message ?: "Error"
        }

        //previewOverlay.update(, image, points)

        if (resultText.isNotEmpty()) {
            scanResult(resultText, resultPoints, imageProxy)
        }

        imageProxy.close()
    }
}
