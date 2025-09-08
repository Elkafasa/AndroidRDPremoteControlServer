package com.example.remotecontrolserver

import android.graphics.Bitmap
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.google.zxing.BarcodeFormat

object QRCodeHelper {
    fun generateQRCode(text: String): Bitmap {
        val barcodeEncoder = BarcodeEncoder()
        return barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400)
    }
}
