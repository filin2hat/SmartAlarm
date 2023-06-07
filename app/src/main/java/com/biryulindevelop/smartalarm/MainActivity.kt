package com.biryulindevelop.smartalarm

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.biryulindevelop.smartalarm.databinding.ActivityMainBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val binding by viewBinding(ActivityMainBinding::bind)
    private val viewModel: AlarmViewModel by viewModels()

    private val overlayPermissionResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            if (Settings.canDrawOverlays(this)) {
                checkOverlayPermission()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.overlay_msg),
                    Toast.LENGTH_SHORT
                ).show()
                this.finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions()

        lifecycleScope.launch {
            viewModel.getTimeFlow().collect {
                binding.currentTimeTextView.text = it
            }
        }

        binding.alarmButton.setOnClickListener {
            checkOverlayPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun setAlarmClock() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12)
            .setMinute(0)
            .setTitleText(getString(R.string.set_time))
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendar.set(Calendar.MINUTE, timePicker.minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val currentTimeMillis = System.currentTimeMillis()
            val alarmTimeMillis = calendar.timeInMillis

            if (alarmTimeMillis <= currentTimeMillis) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val alarmManager: AlarmManager =
                getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val alarmClockInfo =
                AlarmManager.AlarmClockInfo(calendar.timeInMillis, getAlarmInfoPendingIntent())

            alarmManager.setAlarmClock(alarmClockInfo, getAlarmActionPendingIntent())

            val notificationManager = NotificationManagerCompat.from(this)
            val channelId = "alarm_channel"
            val channel = NotificationChannel(
                channelId,
                "Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.alarm_clock_set_to) + " ${timePicker.hour}:${timePicker.minute}")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(1, notification)
        }
        timePicker.show(supportFragmentManager, "timePicker")
    }

    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${this.packageName}")
            )
            overlayPermissionResult.launch(intent)
        }
        setAlarmClock()
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissions.isNotEmpty()) {
            val shouldShowRationale = permissions.any {
                shouldShowRequestPermissionRationale(it)
            }
            if (shouldShowRationale) {
                Toast.makeText(
                    this,
                    getString(R.string.permissions_rationale_msg),
                    Toast.LENGTH_SHORT
                ).show()
            }
            requestPermissions.launch(permissions.toTypedArray())
        }
    }

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                Toast.makeText(
                    this,
                    getString(R.string.permissions_granted_msg),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.permissions_denied_msg),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun getAlarmInfoPendingIntent(): PendingIntent {
        val alarmInfoIntent = Intent(this, MainActivity::class.java)
        alarmInfoIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(
            this,
            0,
            alarmInfoIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getAlarmActionPendingIntent(): PendingIntent {
        val intent = Intent(this, AlarmActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(
            this,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}