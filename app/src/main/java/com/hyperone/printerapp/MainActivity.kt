package com.hyperone.printerapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.hyperone.printerapp.utils.ZebraPrinterDialogFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        ZebraPrinter().showDialog(this)

        showAWBPrintDialog()
    }

    private fun showAWBPrintDialog() {
        val fragmentManager: FragmentManager = supportFragmentManager
        ZebraPrinterDialogFragment.showDialog(fragmentManager)
    }
}