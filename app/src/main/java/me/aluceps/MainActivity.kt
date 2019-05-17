package me.aluceps

import android.annotation.SuppressLint
import android.databinding.DataBindingUtil
import android.databinding.ObservableField
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import me.aluceps.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private var message = ObservableField("")

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.message = message
        CustomClickEvent(binding.shutter).setOnClickListener(object :
            CustomClickEvent.OnClickListener {
            override fun onClick() {
                message.set("onClick")
            }

            override fun onLongClick(isFinished: Boolean) {
                if (!isFinished) {
                    message.set("onLongClickStart")
                } else {
                    message.set("onLongClickFinished")
                }
            }
        })
    }

    class CustomClickEvent(view: View, longDuration: Long = 500L) {

        private var isPressing = false
        private var pressTime = 0L

        private lateinit var timer: Timer
        private val handler = Handler()

        init {
            view.setOnTouchListener { _, e ->
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isPressing = true
                        pressTime = Date().time
                        timer = Timer()
                        timer.scheduleAtFixedRate(object : TimerTask() {
                            override fun run() {
                                val duration = Date().time - pressTime
                                if (isPressing) {
                                    if (duration < longDuration) {
                                    } else {
                                        listener?.onLongClick(false)
                                    }
                                } else {
                                    if (duration < longDuration) {
                                        listener?.onClick()
                                    } else {
                                        listener?.onLongClick(true)
                                    }
                                }
                            }
                        }, 10, 10)
                    }
                    MotionEvent.ACTION_UP -> {
                        isPressing = false
                        handler.postDelayed({
                            timer.cancel()
                            timer.purge()
                        }, 10)
                    }
                    else -> Unit
                }
                true
            }
        }

        interface OnClickListener {
            fun onClick()
            fun onLongClick(isFinished: Boolean)
        }

        private var listener: OnClickListener? = null

        fun setOnClickListener(listener: OnClickListener) {
            this.listener = listener
        }
    }
}
