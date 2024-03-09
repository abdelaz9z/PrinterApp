package com.hyperone.printerapp


import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.hyperone.printerapp.utils.ZebraPrinterDialogFragment
import java.util.Date
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

        val createLabelBitmap = createLabelBitmap(imageBitmap = bitmap!!)
        imageView.setImageBitmap(createLabelBitmap)

        // Show Zebra Printer dialog
        showAWBPrintDialog(createLabelBitmap)
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
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 400, 400, hints)
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

    private fun createLabelBitmap(
        branchName: String = "HyperOne",
        dateString: String = Date().toString(),
        numberOfItems: Int = 3,
        imageBitmap: Bitmap // Bitmap object
    ): Bitmap {
        // Calculate width and height based on image size and text content
        val imageWidth = imageBitmap.width
        val imageHeight = imageBitmap.height
        val textHeight = calculateTextHeight(branchName, dateString, numberOfItems)

        val height = imageHeight + textHeight // Height includes image height and text height

        // Create a bitmap with the calculated width and height
        val bitmap = Bitmap.createBitmap(imageWidth, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw white background
        canvas.drawColor(Color.WHITE)

        // Draw image bitmap
        canvas.drawBitmap(imageBitmap, 0f, 0f, null)

        // Draw branch name
        val branchNamePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        branchNamePaint.textSize = 25f
        branchNamePaint.color = Color.BLACK
        branchNamePaint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(branchName, 0f, imageHeight.toFloat() + 40f, branchNamePaint)

        // Draw date
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        datePaint.textSize = 20f
        datePaint.color = Color.BLACK
        canvas.drawText(dateString, 0f, imageHeight.toFloat() + 70f, datePaint)

        // Draw number of items
        val itemsPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        itemsPaint.textSize = 25f
        itemsPaint.color = Color.BLACK
        canvas.drawText(
            "Number of items: $numberOfItems",
            0f,
            imageHeight.toFloat() + 110f,
            itemsPaint
        )

        return bitmap
    }

    private fun calculateTextHeight(
        branchName: String,
        dateString: String,
        numberOfItems: Int
    ): Int {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textSize = 25f

        val branchNameWidth = textPaint.measureText(branchName)
        val dateWidth = textPaint.measureText(dateString)
        val itemsWidth = textPaint.measureText("Number of items: $numberOfItems")

        // Calculate the maximum width among the three text elements
        val maxWidth = maxOf(branchNameWidth, dateWidth, itemsWidth)

        // Estimate the height of one line of text
        val lineHeight = textPaint.fontMetrics.bottom - textPaint.fontMetrics.top

        // Calculate the number of lines needed for text
        val numLines =
            4 // Assuming each of the three elements (branch name, date, number of items) is on its own line

        return (lineHeight * numLines).toInt()
    }
}