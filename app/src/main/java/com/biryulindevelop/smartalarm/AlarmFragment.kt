package com.biryulindevelop.smartalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

    private val overlayPermissionResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Settings.canDrawOverlays(requireContext())) {
                checkOverlayPermission()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Разрешение на отображение наложений не предоставлено",
                    Toast.LENGTH_SHORT
                ).show()
                requireActivity().finish()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.getTimeFlow().collect {
                binding.currentTimeTextView.text = it
            }
        }

        binding.alarmButton.setOnClickListener {
            checkOverlayPermission()
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Выберите время")
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                calendar.set(Calendar.MINUTE, timePicker.minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val alarmManager: AlarmManager =
                    requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val alarmClockInfo =
                    AlarmManager.AlarmClockInfo(
                        calendar.timeInMillis,
                        getAlarmInfoPendingIntent()
                    )

                alarmManager.setAlarmClock(alarmClockInfo, getAlarmActionPendingIntent())

                Toast.makeText(
                    requireContext(),
                    "Будильни установлен на ${timePicker.hour}:${timePicker.minute}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            timePicker.show(childFragmentManager, "timePicker")
        }
    }

    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(requireContext())) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${requireContext().packageName}")
            )
            overlayPermissionResult.launch(intent)
        }
    }

    private fun getAlarmInfoPendingIntent(): PendingIntent {
        val alarmInfoIntent = Intent(requireContext(), MainActivity::class.java)
        alarmInfoIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(
            requireContext(),
            0,
            alarmInfoIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getAlarmActionPendingIntent(): PendingIntent {
        val intent = Intent(requireContext(), AlarmActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(
            requireContext(),
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}