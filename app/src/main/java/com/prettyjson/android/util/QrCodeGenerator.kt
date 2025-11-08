package com.prettyjson.android.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Utility for generating QR codes from text
 */
object QrCodeGenerator {
    
    /**
     * Generate a QR code bitmap from text
     * @param text The text to encode in the QR code
     * @param size The size of the QR code in pixels (default: 512)
     * @return Bitmap of the QR code, or null if generation fails
     */
    fun generateQrCode(text: String, size: Int = 512): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 1)
            }
            
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints)
            
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            
            bitmap
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if text is too long for QR code generation
     * QR codes have practical limits based on error correction level
     * @param text The text to check
     * @return true if text is too long, false otherwise
     */
    fun isTextTooLong(text: String): Boolean {
        // QR codes can handle up to ~3000 characters with high error correction
        // For practical purposes, we'll limit to 2000 characters
        return text.length > 2000
    }
}

