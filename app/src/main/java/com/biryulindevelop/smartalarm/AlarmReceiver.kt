package com.biryulindevelop.smartalarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    private var ringtone: Ringtone? = null

    override fun onReceive(context: Context, intent: Intent) {
        startRingtone(context)

        val message = intent.getStringExtra("message") ?: "Wake up!"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            "alarm_channel",
            "Alarm",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
        val notificationBuilder = NotificationCompat.Builder(context, "alarm_channel")
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("Alarm")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun startRingtone(context: Context) {
        var ringtoneUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
        if (ringtone == null) {
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
        }
        if (ringtone != null) {
            ringtone!!.play()
        }
    }

    fun stopRingtone() {
        if (ringtone != null && ringtone!!.isPlaying) {
            ringtone!!.stop()
        }
    }
}