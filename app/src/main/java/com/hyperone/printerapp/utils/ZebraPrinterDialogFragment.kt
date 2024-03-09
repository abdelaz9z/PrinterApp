package com.hyperone.printerapp.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputLayout
import com.hyperone.printerapp.R
import com.zebra.sdk.comm.BluetoothConnection
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.comm.TcpConnection
import com.zebra.sdk.graphics.ZebraImageFactory
import com.zebra.sdk.graphics.ZebraImageI
import com.zebra.sdk.printer.ZebraPrinter
import com.zebra.sdk.printer.ZebraPrinterFactory
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ZebraPrinterDialogFragment : DialogFragment() {

    private var connection: Connection? = null
    private var printer: ZebraPrinter? = null
    private lateinit var bitmap: Bitmap
    private lateinit var btRadioButton: RadioButton
    private lateinit var macInput: TextInputLayout
    private lateinit var ipAddressInput: TextInputLayout
    private lateinit var portInput: TextInputLayout
    private lateinit var statusField: TextView
    private lateinit var printButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bitmap = it.getParcelable(ARG_BITMAP)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.zebra_printer, container, false)
        initializeViews(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSavedSettings()
    }

    private fun initializeViews(view: View) {
        btRadioButton = view.findViewById(R.id.bluetoothRadio)
        macInput = view.findViewById(R.id.macInput)
        ipAddressInput = view.findViewById(R.id.ipAddressInput)
        portInput = view.findViewById(R.id.portInput)
        statusField = view.findViewById(R.id.statusText)
        printButton = view.findViewById(R.id.button_print)

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.bluetoothRadio -> {
                    toggleEditField(macInput, true)
                    toggleEditField(portInput, false)
                    toggleEditField(ipAddressInput, false)
                }

                else -> {
                    toggleEditField(macInput, false)
                    toggleEditField(portInput, true)
                    toggleEditField(ipAddressInput, true)
                }
            }
        }

        printButton.setOnClickListener {
            Thread {
                enablePrintButton(false)
                Looper.prepare()
                doPrint()
                Looper.loop()
                Looper.myLooper()!!.quit()
            }.start()
        }
    }

    private fun toggleEditField(textInputLayout: TextInputLayout, set: Boolean) {
        textInputLayout.isEnabled = set
        textInputLayout.isFocusable = set
        textInputLayout.isFocusableInTouchMode = set
    }

    private fun doPrint() {
        if (connect()) {
            if (printer != null) {
                sendTestLabel()
            } else {
                disconnect()
                dialog?.dismiss()
            }
        } else {
            setStatus("Failed to connect", Color.RED)
            enablePrintButton(true)
        }
    }

    private fun connect(): Boolean {
        setStatus("Connecting…", Color.YELLOW)
        connection = null
        return try {
            connection = if (btRadioButton.isChecked) {
                val macAddress = macInput.editText?.text.toString()
                BluetoothConnection(macAddress)
            } else {
                val ipAddress = ipAddressInput.editText?.text.toString()
                val port = portInput.editText?.text.toString().toInt()
                TcpConnection(ipAddress, port)
            }
            connection!!.open()
            setStatus("Connected", Color.GREEN)
            printer = ZebraPrinterFactory.getInstance(connection)
            true
        } catch (e: ConnectionException) {
            setStatus("Connection Error: ${e.message}", Color.RED)
            false
        } catch (e: NumberFormatException) {
            setStatus("Invalid Port Number", Color.RED)
            false
        }
    }

    private fun disconnect() {
        try {
            connection?.close()
        } catch (e: ConnectionException) {
            setStatus("Error: ${e.message}", Color.RED)
        }
    }

    private fun sendTestLabel() {
        try {
            val printerStatus = printer?.currentStatus
            if (printerStatus?.isReadyToPrint == true) {
                val printer = ZebraPrinterFactory.getInstance(connection)
                printer.printImage(convertBitmapToZebraImage(bitmap), 75, 100, 250, 250, false)

                // connection?.write(trimIndent.toByteArray())
                setStatus("Sending Data", Color.BLUE)
                dialog?.cancel()
            } else {
                setStatus("Printer Error: ${printerStatus?.printMode}", Color.RED)
            }
        } catch (e: ConnectionException) {
            setStatus("Error: ${e.message}", Color.RED)
        } finally {
            disconnect()
        }
    }

    fun generateZPLCode(part1: String, part2: String): String {
        return "$part1\n$part2"
    }

    fun formatDate(date: Date): String {
        val format = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())
        return format.format(date)
    }

    private fun convertBitmapToZebraImage(bitmap: Bitmap): ZebraImageI {
        return ZebraImageFactory.getImage(convertToMonochrome(bitmap))
    }

    private fun convertToMonochrome(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val monochromeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val grayscale =
                    (Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114).toInt()
                val monochromeColor = if (grayscale > 128) Color.WHITE else Color.BLACK
                monochromeBitmap.setPixel(x, y, monochromeColor)
            }
        }
        return monochromeBitmap
    }

    private fun setStatus(statusMessage: String, color: Int) {
        activity?.runOnUiThread {
            statusField.setBackgroundColor(color)
            statusField.text = statusMessage
        }
    }

    private fun enablePrintButton(enabled: Boolean) {
        activity?.runOnUiThread {
            printButton.isEnabled = enabled
        }
    }

    private fun loadSavedSettings() {
        val context = requireContext()
        ipAddressInput.editText?.setText(SettingsHelper.getIp(context))
        portInput.editText?.setText(SettingsHelper.getPort(context))
        macInput.editText?.setText(SettingsHelper.getBluetoothAddress(context))
    }

    private fun convertBitmapToBinary(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    companion object {
        private const val ARG_BITMAP = "bitmap"

        fun showDialog(fragmentManager: FragmentManager, bitmap: Bitmap) {
            val dialog = ZebraPrinterDialogFragment()
            val args = Bundle().apply {
                putParcelable(ARG_BITMAP, bitmap)
            }
            dialog.arguments = args
            dialog.show(fragmentManager, "ZebraPrinterDialogFragment")
        }
    }
}
