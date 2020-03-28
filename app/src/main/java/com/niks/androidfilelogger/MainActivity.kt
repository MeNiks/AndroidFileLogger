package com.niks.androidfilelogger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.niks.filelog.FileLogHelper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FileLogHelper.initialize(this.applicationContext)

        FileLogHelper.log("App Launched")
        FileLogHelper.log(tag = "Niks", message = "App Launched App Launched App Launched App Launched App Launched App Launched App Launched App Launched App Launched")
    }
}
