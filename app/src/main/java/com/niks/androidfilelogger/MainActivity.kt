package com.niks.androidfilelogger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.niks.filelog.FileLogHelper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FileLogHelper.context = this.applicationContext
        FileLogHelper.appendLog(
            "super.onCreate(savedInstanceState)super.onCreate(savedInstanceState)super.onCreate(savedInstanceState)super.onCreate(savedInstanceState)super.onCreate(savedInstanceState)super.onCreate(savedInstanceState)super.onCreate(savedInstanceState)super.onCreate(savedInstanceState)super.onCreate(savedInstanceState)super.onCreate(savedInstanceState)super.onCreate(savedInstanceState)super.onCreate(savedInstanceState)\n" +
                "        setContentView(R.layout.activity_main)\n" +
                "        FileLogHelper.context = this.applicationContext"
        )
    }
}
