package com.hyperone.printerapp.utils

import android.content.Intent
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
import com.hyperone.printerapp.MainActivity
import com.hyperone.printerapp.R
import com.zebra.sdk.comm.BluetoothConnection
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.comm.TcpConnection
import com.zebra.sdk.printer.PrinterLanguage
import com.zebra.sdk.printer.SGD
import com.zebra.sdk.printer.ZebraPrinter
import com.zebra.sdk.printer.ZebraPrinterFactory
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException

class ZebraPrinterDialogFragment : DialogFragment() {

    private var printed = false
    private var configLabel = "".toByteArray()

    private var connection: Connection? = null
    private var btRadioButton: RadioButton? = null
    private var macInput: TextInputLayout? = null
    private var ipAddressInput: TextInputLayout? = null
    private var portInput: TextInputLayout? = null
    private var statusField: TextView? = null
    private var printButton: Button? = null
    private var printer: ZebraPrinter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize your variables here
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.zebra_printer, container, false)
        // Initialize your views and set listeners here
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup your views and their listeners here
        initializeViews(view)

        // Load saved settings when the dialog view is created
        loadSavedSettings()

        // Setup your views and their listeners here
        initializeViews(view)
    }

    private fun initializeViews(view: View) {
        btRadioButton = view.findViewById(R.id.bluetoothRadio)
        ipAddressInput = view.findViewById(R.id.ipAddressInput)
        portInput = view.findViewById(R.id.portInput)
        macInput = view.findViewById(R.id.macInput)
        statusField = view.findViewById(R.id.statusText)
        printButton = view.findViewById(R.id.button_print)

        val radioGroup = dialog!!.findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            if (checkedId == R.id.bluetoothRadio) {
                toggleEditField(macInput!!, true)
                toggleEditField(portInput!!, false)
                toggleEditField(ipAddressInput!!, false)
            } else {
                toggleEditField(portInput!!, true)
                toggleEditField(ipAddressInput!!, true)
                toggleEditField(macInput!!, false)
            }
        }

        printButton!!.setOnClickListener {
            Thread {
                enableTestButton(false)
                Looper.prepare()
                doConnectionTest()
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

    private fun doConnectionTest() {
        printer = connect()
        if (printer != null) {
            sendTestLabel()
        } else {
            disconnect()
            dialog?.dismiss()
        }
    }

    private fun connect(): ZebraPrinter? {
        setStatus("Connecting…", Color.YELLOW)
        connection = null
        if (isBluetoothSelected) {
            connection = BluetoothConnection(macAddressFieldText)
            // Save settings for (MAC address)
            saveSettings(macAddressFieldText)
        } else {
            try {
                val port = tcpPortNumber.toInt()
                connection = TcpConnection(tcpAddress, port)
                // Save settings for (IP address, port)
                saveSettings(tcpAddress, tcpPortNumber)
            } catch (e: NumberFormatException) {
                setStatus("Port Number Is Invalid", Color.RED)
                return null
            }
        }
        try {
            connection!!.open()
            setStatus("Connected", Color.GREEN)
        } catch (e: ConnectionException) {
            setStatus("Comm Error! Disconnecting", Color.RED)
            DemoSleeper.sleep(1000)
            disconnect()
        }
        var printer: ZebraPrinter? = null
        if (connection!!.isConnected) {
            try {
                printer = ZebraPrinterFactory.getInstance(connection)
                setStatus("Determining Printer Language", Color.YELLOW)
                val pl = SGD.GET("device.languages", connection)
                setStatus("Printer Language $pl", Color.BLUE)
            } catch (e: ConnectionException) {
                setStatus("Unknown Printer Language", Color.RED)
                printer = null
                DemoSleeper.sleep(1000)
                disconnect()
            } catch (e: ZebraPrinterLanguageUnknownException) {
                setStatus("Unknown Printer Language", Color.RED)
                printer = null
                DemoSleeper.sleep(1000)
                disconnect()
            }
        }
        return printer
    }

    private fun disconnect() {
        try {
            setStatus("Disconnecting", Color.RED)
            if (connection != null) {
                connection!!.close()
            }
            setStatus("Not Connected", Color.RED)
        } catch (e: ConnectionException) {
            setStatus("COMM Error! Disconnected", Color.RED)
        } finally {
            enableTestButton(true)
        }
    }

    private fun sendTestLabel() {
        try {
            val linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer)
            val printerStatus =
                if (linkOsPrinter != null) linkOsPrinter.currentStatus else printer!!.currentStatus
            if (printerStatus.isReadyToPrint) {

                configLabel = "".toByteArray()

                configLabel = getConfigLabel()!!
                connection!!.write(configLabel)
                printed = true
                setStatus("Sending Data", Color.BLUE)

                activity?.startActivity(Intent(activity, MainActivity::class.java))
                activity?.finish()

                dialog?.cancel()
            } else if (printerStatus.isHeadOpen) {
                setStatus("Printer Head Open", Color.RED)
                printed = false
            } else if (printerStatus.isPaused) {
                setStatus("Printer is Paused", Color.RED)
                printed = false
            } else if (printerStatus.isPaperOut) {
                setStatus("Printer Media Out", Color.RED)
                printed = false
            }
            DemoSleeper.sleep(1500)
            if (connection is BluetoothConnection) {
                val friendlyName = (connection as BluetoothConnection).friendlyName
                setStatus(friendlyName, Color.MAGENTA)
                DemoSleeper.sleep(500)
            }
        } catch (e: ConnectionException) {
            setStatus(e.message, Color.RED)
            printed = false
        } finally {
            disconnect()
        }
    }

    private fun setStatus(statusMessage: String?, color: Int) {
        activity?.runOnUiThread {
            statusField?.setBackgroundColor(color)
            statusField?.text = statusMessage
        }
        DemoSleeper.sleep(1000)
    }

    private fun enableTestButton(enabled: Boolean) {
        activity?.runOnUiThread { printButton?.isEnabled = enabled }
    }

    private fun getConfigLabel(): ByteArray? {
        var configLabel: ByteArray? = null
        try {
            val printerLanguage = printer?.printerControlLanguage
            if (printerLanguage == PrinterLanguage.ZPL) {
                try {
                    configLabel = generateZplCode(
                        qrCodeData = "123",
                        barcodeData = "123",
                        name = "item #1"
                    ).toByteArray()
                } catch (e: NullPointerException) {
                    // Handle exception
                }
            } else if (printerLanguage == PrinterLanguage.CPCL) {
                val cpclConfigLabel = """
                ! 0 200 200 406 1
                ON-FEED IGNORE
                BOX 20 20 380 380 8
                T 0 6 137 177 TEST
                PRINT
               
                """.trimIndent()
                configLabel = cpclConfigLabel.toByteArray()
            }
        } catch (e: ConnectionException) {
            // Handle exception
        }
        return configLabel
    }

    private fun generateZplCode(qrCodeData: String, barcodeData: String, name: String): String =
        """
        ^XA
        ^FO50,50^BQN,2,10
        ^FDMM,AAC-$qrCodeData^FS
        ^FO50,300^A0N,30,30^FD$name^FS
        ^XZ
    """.trimIndent()

    private fun loadSavedSettings() {
        val context = requireContext()
        ipAddressInput?.editText?.setText(SettingsHelper.getIp(context))
        portInput?.editText?.setText(SettingsHelper.getPort(context))
        macInput?.editText?.setText(SettingsHelper.getBluetoothAddress(context))
    }

    private fun saveSettings(ipAddress: String, port: String) {
        val context = requireContext()
        SettingsHelper.saveIp(context, ipAddress)
        SettingsHelper.savePort(context, port)
    }

    private fun saveSettings(mac: String) {
        val context = requireContext()
        SettingsHelper.saveBluetoothAddress(context, mac)
    }

    private val isBluetoothSelected: Boolean
        get() = btRadioButton?.isChecked ?: false

    private val macAddressFieldText: String
        get() = macInput?.editText?.text.toString()

    private val tcpAddress: String
        get() = ipAddressInput?.editText?.text.toString()

    private val tcpPortNumber: String
        get() = portInput?.editText?.text.toString()

    companion object {
        fun showDialog(fragmentManager: FragmentManager) {
            val dialog = ZebraPrinterDialogFragment()
            dialog.show(fragmentManager, "AWBPrintDialogFragment")
        }
    }
}