package com.biryulindevelop.smartalarm.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AlarmViewModel : ViewModel() {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun getTimeFlow(): Flow<String> = flow {
        while (true) {
            emit(LocalTime.now().format(timeFormatter))
            kotlinx.coroutines.delay(1000)
        }
    }.flowOn(Dispatchers.IO)
}