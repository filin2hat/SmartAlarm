package com.biryulindevelop.smartalarm.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.biryulindevelop.smartalarm.R
import com.biryulindevelop.smartalarm.databinding.ActivityMainBinding
import com.biryulindevelop.smartalarm.domain.AlarmCoroutineWorker
import com.biryulindevelop.smartalarm.ui.viewmodel.AlarmViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val binding by viewBinding(ActivityMainBinding::bind)
    private val viewModel: AlarmViewModel by viewModels()
    private var hour: Int = 0
    private var minute: Int = 0
    private val hourCounterListener = { value: Int -> hour = value }
    private val minuteCounterListener = { value: Int -> minute = value }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkOverlayPermission()

        lifecycleScope.launch {
            viewModel.getTimeFlow().collect {
                binding.currentTimeTextView.text = it
            }
        }

        binding.alarmButton.setOnClickListener {
            checkPermissions()
        }

        binding.counterView.addHourCounterListener(hourCounterListener)
        binding.counterView.addMinuteCounterListener(minuteCounterListener)
    }

    override fun onDestroy() {
        binding.counterView.removeHourCounterListener(hourCounterListener)
        binding.counterView.removeMinuteCounterListener(minuteCounterListener)
        super.onDestroy()
    }

    private fun makeWorker() {
        val inputData = Data.Builder()
            .putInt("hour", hour)
            .putInt("minute", minute)
            .build()

        val alarmCoroutineWorker = OneTimeWorkRequestBuilder<AlarmCoroutineWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this).enqueue(alarmCoroutineWorker)
    }

    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${this.packageName}")
            )
            overlayPermissionResult.launch(intent)
        }
    }

    private val overlayPermissionResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
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
        }else {
            makeWorker()
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
}