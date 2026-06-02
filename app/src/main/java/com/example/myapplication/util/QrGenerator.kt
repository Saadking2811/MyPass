package com.example.myapplication.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

/** Encodes a payload as a QR code bitmap. Used for the boarding-pass scan code. */
object QrGenerator {

    fun generate(content: String, size: Int = 800): Bitmap {
        val matrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        return matrix.toBitmap()
    }

    private fun BitMatrix.toBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (get(x, y)) android.graphics.Color.parseColor("#212529") else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }
}
