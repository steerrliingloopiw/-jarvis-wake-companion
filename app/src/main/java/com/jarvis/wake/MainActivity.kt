
package com.jarvis.wake

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
        }

        val title = TextView(this).apply {
            text = "JARVIS WAKE COMPANION"
            textSize = 24f
        }

        statusText = TextView(this).apply {
            text = "Wake detector not started"
            textSize = 16f
            setPadding(0, 30, 0, 30)
        }

        val startButton = Button(this).apply {
            text = "Enable microphone permission"
            setOnClickListener {
                requestMicrophonePermission()
            }
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(startButton)

        setContentView(layout)

        updateStatus()
    }

    private fun requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
        } else {
            statusText.text = "Microphone permission granted"
        }
    }

    private fun updateStatus() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        statusText.text = if (granted) {
            "Microphone permission granted"
        } else {
            "Microphone permission required"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        updateStatus()
    }
}

