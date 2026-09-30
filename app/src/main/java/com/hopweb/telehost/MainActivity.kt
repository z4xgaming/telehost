package com.hopweb.telehost

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hopweb.telehost.databinding.ActivityMainBinding
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var botFile: File? = null
    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Init Python
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        b.btnNewBot.setOnClickListener { createNewBot() }
        b.btnStartBot.setOnClickListener { startBot() }
        b.btnStopBot.setOnClickListener { stopBot() }
        b.btnInstallLibs.setOnClickListener { installLibs() }
        b.btnViewLog.setOnClickListener { viewLog() }
        b.btnClearLog.setOnClickListener { clearLog() }
        b.btnOpenEditor.setOnClickListener { openEditor() }

        loadBotList()
    }

    private fun createNewBot() {
        val template = """
            from telegram import Update
            from telegram.ext import Application, CommandHandler, MessageHandler, filters, ContextTypes

            TOKEN = "YOUR_BOT_TOKEN_HERE"

            async def start(update: Update, context: ContextTypes.DEFAULT_TYPE):
                await update.message.reply_text("Namaste! Bot chalu hai ✅")

            async def echo(update: Update, context: ContextTypes.DEFAULT_TYPE):
                await update.message.reply_text(update.message.text)

            def main():
                app = Application.builder().token(TOKEN).build()
                app.add_handler(CommandHandler("start", start))
                app.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, echo))
                print("Bot started...")
                app.run_polling()

            if __name__ == "__main__":
                main()
        """.trimIndent()

        val botDir = File(filesDir, "bots").apply { mkdirs() }
        val newBot = File(botDir, "bot_${System.currentTimeMillis()}.py")
        newBot.writeText(template)
        botFile = newBot
        b.tvBotPath.text = "📄 ${newBot.name}"
        toast("Naya bot template banaya")
        openEditor()
    }

    private fun openEditor() {
        val f = botFile ?: return toast("Pehle bot banao")
        val i = Intent(this, EditorActivity::class.java)
        i.putExtra("path", f.absolutePath)
        startActivity(i)
    }

    private fun startBot() {
        val f = botFile ?: return toast("Pehle bot select karo")
        if (isRunning) return toast("Bot already chal raha hai")

        val intent = Intent(this, BotService::class.java).apply {
            putExtra("bot_path", f.absolutePath)
            action = "START"
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else startService(intent)
        isRunning = true
        b.tvStatus.text = "🟢 Bot Running"
        toast("Bot start ho raha hai...")
    }

    private fun stopBot() {
        if (!isRunning) return toast("Bot chal nahi raha")
        val intent = Intent(this, BotService::class.java).apply { action = "STOP" }
        startService(intent)
        isRunning = false
        b.tvStatus.text = "🔴 Bot Stopped"
        toast("Bot stopped")
    }

    private fun installLibs() {
        scope.launch {
            try {
                withContext(Dispatchers.Main) { b.tvStatus.text = "📦 Libraries install ho rahi hain..." }
                val py = Python.getInstance()
                val result = py.getModule("auto_install").callAttr("install_all").toString()
                withContext(Dispatchers.Main) {
                    b.tvStatus.text = "✅ Libraries ready"
                    Toast.makeText(this@MainActivity, result, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    b.tvStatus.text = "❌ Install fail: ${e.message}"
                }
            }
        }
    }

    private fun viewLog() {
        val logFile = File(filesDir, "logs/bot.log")
        if (!logFile.exists()) return toast("Log empty hai")
        val log = logFile.readText()
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📜 Live Log")
            .setMessage(log.takeLast(3000))
            .setPositiveButton("Refresh") { _, _ -> viewLog() }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun clearLog() {
        val logFile = File(filesDir, "logs/bot.log")
        if (logFile.exists()) logFile.delete()
        toast("Log clear ho gaya")
    }

    private fun loadBotList() {
        val botDir = File(filesDir, "bots")
        if (!botDir.exists()) botDir.mkdirs()
        val bots = botDir.listFiles()?.filter { it.extension == "py" } ?: emptyList()
        if (bots.isNotEmpty()) {
            botFile = bots.last()
            b.tvBotPath.text = "📄 ${bots.last().name}"
        } else {
            b.tvBotPath.text = "Koi bot nahi — naya banao"
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
