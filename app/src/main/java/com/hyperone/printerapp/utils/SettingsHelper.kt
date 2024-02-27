package com.hyperone.printerapp.utils

import android.content.Context

object SettingsHelper {

    private const val PREFS_NAME = "OurSavedAddress"
    private const val BLUETOOTH_ADDRESS_KEY = "ZEBRA_DEMO_BLUETOOTH_ADDRESS"
    private const val TCP_ADDRESS_KEY = "ZEBRA_DEMO_TCP_ADDRESS"
    private const val TCP_PORT_KEY = "ZEBRA_DEMO_TCP_PORT"

    private fun sharedPreferences(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)


    fun getIp(context: Context) = sharedPreferences(context).getString(TCP_ADDRESS_KEY, "") ?: ""
    fun getPort(context: Context) = sharedPreferences(context).getString(TCP_PORT_KEY, "") ?: ""
    fun getBluetoothAddress(context: Context) =
        sharedPreferences(context).getString(BLUETOOTH_ADDRESS_KEY, "") ?: ""

    fun saveIp(context: Context, ip: String) {
        sharedPreferences(context).edit().putString(TCP_ADDRESS_KEY, ip).apply()
    }

    fun savePort(context: Context, port: String) {
        sharedPreferences(context).edit().putString(TCP_PORT_KEY, port).apply()
    }

    fun saveBluetoothAddress(context: Context, address: String) {
        sharedPreferences(context).edit().putString(BLUETOOTH_ADDRESS_KEY, address).apply()
    }
}
