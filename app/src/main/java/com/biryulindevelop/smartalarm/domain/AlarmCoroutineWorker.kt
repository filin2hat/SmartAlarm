package com.biryulindevelop.smartalarm.domain

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.biryulindevelop.smartalarm.R
import com.biryulindevelop.smartalarm.ui.AlarmActivity
import com.biryulindevelop.smartalarm.ui.MainActivity
import org.shredzone.commons.suncalc.SunTimes
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class AlarmCoroutineWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    private val locationProvider = LastLocationProvider(context)
    override suspend fun doWork(): Result {
        val hourUser = inputData.getInt("hour", 0)
        val minuteUser = inputData.getInt("minute", 0)
        getLocation { location ->
            if (location != null) {
                getSunriseTime(location) { hour, minute ->
                    setAlarm(hour, minute)
                }
            }
        }
        return Result.success()
    }

    private fun getLocation(callback: (Location?) -> Unit) {
        locationProvider.getLocation { result ->
            if (result.isSuccess) {
                val location = result.getOrNull()
                callback(location)
            } else {
                val exception = result.exceptionOrNull()
                Toast.makeText(applicationContext, exception?.message, Toast.LENGTH_SHORT).show()
                callback(null)
            }
        }
    }

    private fun getSunriseTime(location: Location, callback: (Int, Int) -> Unit) {
        val timeZone = TimeZone.getDefault()
        //Log.d("TAG", "timeZone: $timeZone")
        val calendar = Calendar.getInstance(timeZone)
        //Log.d("TAG", "calendar: $calendar")
        val calculator = SunTimes.compute()
            .localTime()
            .at(location.latitude, location.longitude)
            .execute()
        val sunrise = calculator.rise
        Log.d("TAG", "sunrise: $sunrise")
        calendar.timeInMillis = sunrise?.toInstant()?.toEpochMilli() as Long
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
       // Log.d("TAG", "hour: $hour, minute: $minute")
        callback(hour, minute)
    }

    private fun setAlarm(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val currentTimeMillis = System.currentTimeMillis()
        val alarmTimeMillis = calendar.timeInMillis
        if (alarmTimeMillis <= currentTimeMillis) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        val alarmManager: AlarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmClockInfo =
            AlarmManager.AlarmClockInfo(alarmTimeMillis, getAlarmInfoPendingIntent())
        alarmManager.setAlarmClock(alarmClockInfo, getAlarmActionPendingIntent())
        makeNotification(hour, minute)
    }


    @SuppressLint("MissingPermission")
    private fun makeNotification(hour: Int, minute: Int) {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val channelId = "alarm_channel"
        val channel = NotificationChannel(
            channelId,
            "Alarm Channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText(
                applicationContext.getString(R.string.alarm_clock_set_to) + " ${hour}:${minute}"
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(1, notification)
    }

    private fun getAlarmInfoPendingIntent(): PendingIntent {
        val alarmInfoIntent = Intent(applicationContext, MainActivity::class.java)
        alarmInfoIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(
            applicationContext,
            0,
            alarmInfoIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getAlarmActionPendingIntent(): PendingIntent {
        val intent = Intent(applicationContext, AlarmActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(
            applicationContext,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}