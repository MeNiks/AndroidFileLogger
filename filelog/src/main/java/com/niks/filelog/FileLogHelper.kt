package com.niks.filelog

import android.content.Context
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object FileLogHelper {

    lateinit var context: Context

    fun appendLog(text: String) {
        val df = SimpleDateFormat("dd:MM:yyyy HH:mm:ss", Locale.getDefault())
        try {
            val bufferedWriter = BufferedWriter(FileWriter(getLogFile(context), true))
            bufferedWriter.append(df.format(Calendar.getInstance().time) + " : " + text + "<br/><br/>")
            bufferedWriter.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLogs(): String {
        val reader = BufferedReader(FileReader(getLogFile(context)))
        val stringBuilder = StringBuilder()
        var line: String?
        val ls = System.getProperty("line.separator")
        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
            stringBuilder.append(ls)
        }
        reader.close()

        return stringBuilder.toString()
    }

    private fun getLogFile(context: Context): File {
        val logFile = File(context.cacheDir.path + "/log.file")
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
        return logFile
    }
}