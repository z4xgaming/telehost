package com.hopweb.telehost

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hopweb.telehost.databinding.ActivityEditorBinding
import java.io.File

class EditorActivity : AppCompatActivity() {

    private lateinit var b: ActivityEditorBinding
    private lateinit var file: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(b.root)

        val path = intent.getStringExtra("path") ?: run { finish(); return }
        file = File(path)
        b.editor.setText(file.readText())

        b.btnSave.setOnClickListener {
            file.writeText(b.editor.text.toString())
            Toast.makeText(this, "✅ Saved", Toast.LENGTH_SHORT).show()
        }
        b.btnClose.setOnClickListener { finish() }
    }
}
