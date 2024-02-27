# ZebraPrinterDialogFragment

## Description
This class represents a dialog fragment used for printing with a Zebra printer. It provides functionality for connecting to the printer via Bluetooth or TCP/IP, sending a test label, and saving printer settings.

## Properties
- `printed`: Boolean - Indicates whether a label has been successfully printed.
- `configLabel`: ByteArray - Configuration label data.
- `connection`: Connection - Connection object for communication with the printer.
- `btRadioButton`: RadioButton - RadioButton for Bluetooth connection.
- `macInput`: TextInputLayout - Input field for MAC address.
- `ipAddressInput`: TextInputLayout - Input field for IP address.
- `portInput`: TextInputLayout - Input field for port number.
- `statusField`: TextView - TextView for displaying connection status.
- `printButton`: Button - Button for initiating the printing process.
- `printer`: ZebraPrinter - Zebra printer object.

## Methods
- `showDialog(fragmentManager: FragmentManager)`: Static method to show the dialog fragment.

## Public Functions
- `onCreate(savedInstanceState: Bundle?)`: Override method called when the fragment is created.
- `onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?`: Override method called to create the view for the fragment.
- `onViewCreated(view: View, savedInstanceState: Bundle?)`: Override method called after the fragment view is created.
- `initializeViews(view: View)`: Method to initialize views and set listeners.
- `toggleEditField(textInputLayout: TextInputLayout, set: Boolean)`: Method to enable/disable and set focus on input fields.
- `doConnectionTest()`: Method to perform the printer connection test.
- `connect()`: Method to establish a connection with the printer.
- `disconnect()`: Method to disconnect from the printer.
- `sendTestLabel()`: Method to send a test label to the printer.
- `setStatus(statusMessage: String?, color: Int)`: Method to set the status message.
- `enableTestButton(enabled: Boolean)`: Method to enable/disable the print button.
- `getConfigLabel()`: Method to get the configuration label data.
- `generateZplCode(qrCodeData: String, barcodeData: String, name: String)`: Method to generate ZPL code for label printing.
- `loadSavedSettings()`: Method to load saved printer settings.
- `saveSettings(ipAddress: String, port: String)`: Method to save printer settings.
- `saveSettings(mac: String)`: Method to save Bluetooth MAC address.
- Getters for radio button state, MAC address, IP address, and port number.

# SettingsHelper

## Description
This object provides helper functions for managing printer settings such as IP address, port number, and Bluetooth address.

## Properties
- `PREFS_NAME`: String - Name of the shared preferences file.
- `BLUETOOTH_ADDRESS_KEY`: String - Key for saving Bluetooth address in shared preferences.
- `TCP_ADDRESS_KEY`: String - Key for saving TCP/IP address in shared preferences.
- `TCP_PORT_KEY`: String - Key for saving TCP/IP port number in shared preferences.

## Methods
- `getIp(context: Context)`: Method to get the saved TCP/IP address.
- `getPort(context: Context)`: Method to get the saved TCP/IP port number.
- `getBluetoothAddress(context: Context)`: Method to get the saved Bluetooth address.
- `saveIp(context: Context, ip: String)`: Method to save the TCP/IP address.
- `savePort(context: Context, port: String)`: Method to save the TCP/IP port number.
- `saveBluetoothAddress(context: Context, address: String)`: Method to save the Bluetooth address.

## Dependencies
- **Zebra SDK**: Zebra SDK is required for printer communication.
    - Implementation: `implementation(files("libs/ZSDK_ANDROID_API.jar"))`
- **Jackson Databind**: Jackson Databind is used for JSON data binding.
    - Implementation: `implementation("com.fasterxml.jackson.core:jackson-databind:2.11.1")`

You can copy and paste this documentation into a Markdown file and upload it to a GitHub Gist. Let me know if you need any further assistance!
