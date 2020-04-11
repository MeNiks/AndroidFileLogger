package com.niks.androidfilelogger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.niks.filelog.FileLogHelper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FileLogHelper.initialize(this.applicationContext)

        FileLogHelper.log("This is message")
        FileLogHelper.log("This is message", longInfo = "This is longInfo")
        FileLogHelper.log(
            message = "This is message",
            tag = "SomeTag",
            longInfo = "This is longInfo"
        )

        //startActivity(Intent(this, FileLogsPreviewActivity::class.java))
    }
}
