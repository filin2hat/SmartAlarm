package com.biryulindevelop.smartalarm

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.biryulindevelop.smartalarm.databinding.FragmentAlarmBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch

class AlarmFragment : Fragment(R.layout.fragment_alarm) {
    private val binding by viewBinding(FragmentAlarmBinding::bind)
    private val viewModel: AlarmViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermissions()

        lifecycleScope.launch {
            viewModel.getTimeFlow().collect {
                binding.currentTimeTextView.text = it
            }
        }

        binding.alarmButton.setOnClickListener {
            setAlarmClock()
        }
    }

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
                requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val alarmIntent = Intent(requireContext(), AlarmReceiver::class.java)
            alarmIntent.putExtra("message", "Wake up!")
            val pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                0,
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            Toast.makeText(
                requireContext(),
                getString(R.string.alarm_clock_set_to) + " ${timePicker.hour}:${timePicker.minute}",
                Toast.LENGTH_SHORT
            ).show()
        }
        timePicker.show(childFragmentManager, "timePicker")
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
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
                    requireContext(),
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
                    requireContext(),
                    getString(R.string.permissions_granted_msg),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permissions_denied_msg),
                    Toast.LENGTH_SHORT
                ).show()
                requireActivity().finish()
            }
        }
}