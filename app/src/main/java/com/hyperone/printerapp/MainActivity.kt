package com.hyperone.printerapp


import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.hyperone.printerapp.utils.ZebraPrinterDialogFragment
import java.util.EnumMap

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Generate QR code with text containing carriage return and line feed
        val text = "6224010488014\r\n6224010488014\r\n6224010488014\r\n6223001930549"

        // Generate QR code bitmap
        val bitmap = generateQRCode(text)

        // Display QR code in ImageView
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.setImageBitmap(bitmap)

        // Show Zebra Printer dialog
        showAWBPrintDialog(bitmap!!)
    }

    private fun showAWBPrintDialog(bitmap: Bitmap) {
        val fragmentManager = supportFragmentManager
        ZebraPrinterDialogFragment.showDialog(fragmentManager, bitmap)
    }

    private fun generateQRCode(text: String): Bitmap? {
        val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        hints[EncodeHintType.MARGIN] = 1

        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(
                        x, y,
                        if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                    )
                }
            }
            bmp
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

}