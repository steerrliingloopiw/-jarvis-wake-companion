
package com.jarvis.wake

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
            text = "Service stopped"
            textSize = 16f
            setPadding(0, 30, 0, 30)
        }

        val permissionButton = Button(this).apply {
            text = "Grant Permissions"
            setOnClickListener {
                requestPermissionsIfNeeded()
            }
        }

        val startButton = Button(this).apply {
            text = "Start Wake Service"
            setOnClickListener {
                if (hasMicrophonePermission()) {
                    val serviceIntent = Intent(
                        this@MainActivity,
                        WakeWordService::class.java
                    )

                    ContextCompat.startForegroundService(
                        this@MainActivity,
                        serviceIntent
                    )

                    statusText.text = "Background service started"
                } else {
                    requestPermissionsIfNeeded()
                }
            }
        }

        val stopButton = Button(this).apply {
            text = "Stop Wake Service"
            setOnClickListener {
                stopService(
                    Intent(this@MainActivity, WakeWordService::class.java)
                )
                statusText.text = "Service stopped"
            }
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(permissionButton)
        layout.addView(startButton)
        layout.addView(stopButton)

        setContentView(layout)
    }

    private fun hasMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionsIfNeeded() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO
        )

        if (Build.VERSION.SDK_INT >= 33) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        ActivityCompat.requestPermissions(
            this,
            permissions.toTypedArray(),
            100
        )
    }
}
