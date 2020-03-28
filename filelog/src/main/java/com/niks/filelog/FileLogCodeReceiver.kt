package com.niks.filelog

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class FileLogCodeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SECRET_CODE") {
            Toast.makeText(context, "Please check notification", Toast.LENGTH_SHORT).show()
            showNotification(context)
        }
    }

    private fun showNotification(context: Context) {
        val channelId = "logs_channel"
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "File Logger",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "To show file logs"
            notificationManagerCompat.createNotificationChannel(channel)
        }
        val builder = NotificationCompat
            .Builder(context, channelId)
            .setContentTitle("Click here to check logs")
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_notification)

        val intent = Intent(context, FileLogsPreviewActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        builder.setContentIntent(
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        notificationManagerCompat.notify(0, builder.build())
    }
}