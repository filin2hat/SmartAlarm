package com.biryulindevelop.smartalarm.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.biryulindevelop.smartalarm.R

class CounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val leftHourButton: Button
    private val rightHourButton: Button
    private val counterHourView: TextView
    private var counterHourListeners = mutableSetOf<(Int) -> Unit>()

    private val leftMinuteButton: Button
    private val rightMinuteButton: Button
    private val counterMinuteView: TextView
    private var counterMinuteListeners = mutableSetOf<(Int) -> Unit>()

    init {
        val root = inflate(context, R.layout.counter_widget, this)
        leftHourButton = root.findViewById(R.id.button_hour_left)
        rightHourButton = root.findViewById(R.id.button_hour_right)
        counterHourView = root.findViewById(R.id.counter_hour_text)
        leftMinuteButton = root.findViewById(R.id.button_minute_left)
        rightMinuteButton = root.findViewById(R.id.button_minute_right)
        counterMinuteView = root.findViewById(R.id.counter_minute_text)

        leftHourButton.setOnClickListener { counterHours-- }
        rightHourButton.setOnClickListener { counterHours++ }

        leftMinuteButton.setOnClickListener { counterMinutes-- }
        rightMinuteButton.setOnClickListener { counterMinutes++ }

    }

    var counterHours = 0
        set(value) {
            if (value == field) return
            field = value
            counterHourView.text = value.toString()
            counterHourListeners.forEach { it(value) }
        }

    var counterMinutes = 0
        set(value) {
            if (value == field) return
            field = value
            counterHourView.text = value.toString()
            counterHourListeners.forEach { it(value) }
        }

    fun addHourCounterListener(listener: (Int) -> Unit) {
        counterHourListeners.add(listener)
        listener(counterHours)
    }

    fun removeHourCounterListener(listener: (Int) -> Unit) {
        counterHourListeners.remove(listener)
    }

    fun addMinuteCounterListener(listener: (Int) -> Unit) {
        counterMinuteListeners.add(listener)
        listener(counterMinutes)
    }

    fun removeMinuteCounterListener(listener: (Int) -> Unit) {
        counterMinuteListeners.remove(listener)
    }
}