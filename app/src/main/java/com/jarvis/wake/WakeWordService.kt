
package com.jarvis.wake

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.openwakeword.OpenWakeWord
import java.net.ServerSocket
import java.net.Socket
import java.net.InetAddress

class WakeWordService : Service() {

    companion object {
        private const val CHANNEL_ID = "jarvis_wake_channel"
        private const val NOTIFICATION_ID = 101
        private const val TAG = "JARVIS_WAKE"
    }

    private var detector: OpenWakeWord? = null
    private var serverSocket: ServerSocket? = null

    @Volatile
    private var listening = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("JARVIS Wake")
            .setContentText("Waiting for JARVIS to sleep")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startCommandServer()
        return START_NOT_STICKY
    }

    private fun startCommandServer() {
        if (serverSocket != null) return

        Thread {
            try {
                val server = ServerSocket(
                    8766, 5, InetAddress.getByName("127.0.0.1")
                )
                serverSocket = server

                while (!server.isClosed) {
                    val client = server.accept()
                    client.use {
                        val message = it.getInputStream()
                            .bufferedReader().readLine()

                        if (message == "SLEEP") {
                            startDetector()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Command server stopped", e)
            }
        }.start()
    }

    @Synchronized
    private fun startDetector() {
        if (listening) return

        try {
            if (detector == null) {
                detector = OpenWakeWord.Builder(this)
                    .setModel(OpenWakeWord.BuiltInModel.HEY_JARVIS)
                    .setThreshold(0.5f)
                    .build()
            }

            listening = true
            updateNotification("Listening for Hey JARVIS")

            detector?.start {
                Log.d(TAG, "Hey JARVIS detected")
                listening = false
                detector?.stop()
                updateNotification("JARVIS is awake")
                sendWakeSignal()
            }

        } catch (e: Exception) {
            listening = false
            Log.e(TAG, "Detector failed", e)
        }
    }

    private fun sendWakeSignal() {
        Thread {
            try {
                Socket("127.0.0.1", 8765).use { socket ->
                    socket.getOutputStream().write("WAKE\n".toByteArray())
                    socket.getOutputStream().flush()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Could not wake Termux", e)
            }
        }.start()
    }

    private fun updateNotification(message: String) {
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("JARVIS Wake")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()

        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "JARVIS Wake Service",
                NotificationManager.IMPORTANCE_LOW
            )

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        listening = false
        detector?.release()
        detector = null

        try {
            serverSocket?.close()
        } catch (_: Exception) {
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
