package com.hopweb.telehost

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.chaquo.python.Python
import kotlinx.coroutines.*
import java.io.File

class BotService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var botJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> {
                val botPath = intent.getStringExtra("bot_path") ?: return START_NOT_STICKY
                startForeground(1, buildNotif("Bot chal raha hai..."))
                runBot(botPath)
            }
            "STOP" -> {
                botJob?.cancel()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun runBot(path: String) {
        botJob = scope.launch {
            try {
                val logDir = File(filesDir, "logs").apply { mkdirs() }
                val logFile = File(logDir, "bot.log")

                val py = Python.getInstance()
                val botModule = py.getModule("bot_runner")
                botModule.callAttr("run_bot", path, logFile.absolutePath)

            } catch (e: Exception) {
                val logDir = File(filesDir, "logs").apply { mkdirs() }
                File(logDir, "bot.log").appendText("\n❌ Error: ${e.message}\n")
            }
        }
    }

    private fun buildNotif(text: String): Notification {
        val channelId = "telebot_host"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Bot Service", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("TeleBot Host")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
