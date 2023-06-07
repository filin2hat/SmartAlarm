package com.biryulindevelop.smartalarm

import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AlarmActivity : AppCompatActivity(R.layout.activity_alarm) {

    private var ringtone: Ringtone? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startAlarm()
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }

    private fun startAlarm() {
        var ringtoneUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ringtone = RingtoneManager.getRingtone(this, ringtoneUri)
        if (ringtone == null) {
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(this, ringtoneUri)
        }
        if (ringtone != null) {
            ringtone!!.play()
        }
    }

    private fun stopAlarm() {
        if (ringtone != null && ringtone!!.isPlaying) {
            ringtone!!.stop()
        }
    }
}