package com.niks.filelog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_file_logs_previewer.*

class FileLogsPreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_logs_previewer)
        FileLogHelper.context = this.applicationContext
    }

    override fun onStart() {
        super.onStart()
        var text = FileLogHelper.getLogs()
        text = if (text.isBlank()) {
            "None"
        } else {
            "<p>$text</p>"
        }
        webView.loadData(text, "text/html", "UTF-8")
    }
}