package com.example.zxingcppdemo.utils

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.AudioManager
import android.media.MediaActionSound
import android.media.ToneGenerator
import android.os.Environment
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.io.File

fun ImageProxy.toJpeg(): ByteArray {
    //This converts the ImageProxy (from the imageAnalysis Use Case)
    //to a ByteArray (compressed as JPEG) for then to be saved for debugging purposes
    //This is the closest representation of the image that is passed to the
    //decoding algorithm.

    val yBuffer = planes[0].buffer // Y
    val vuBuffer = planes[2].buffer // VU

    val ySize = yBuffer.remaining()
    val vuSize = vuBuffer.remaining()

    val nv21 = ByteArray(ySize + vuSize)

    yBuffer.get(nv21, 0, ySize)
    vuBuffer.get(nv21, ySize, vuSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 90, out)
    return out.toByteArray()
}

fun saveImage(image: ImageProxy) {
    val beeper = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50)

    try {
        val currentMillis = System.currentTimeMillis().toString()
        val filename =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/" + currentMillis + "_ZXingCpp.jpg"

        File(filename).outputStream().use { out ->
            out.write(image.toJpeg())
        }
        MediaActionSound().play(MediaActionSound.SHUTTER_CLICK)
    } catch (e: Exception) {
        beeper.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE) //Fail Tone
    }
}
