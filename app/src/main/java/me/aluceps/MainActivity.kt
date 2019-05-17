package me.aluceps

import android.annotation.SuppressLint
import android.databinding.DataBindingUtil
import android.databinding.ObservableField
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
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

        CustomClickEvent(binding.shutter).apply {
            setOnClickListener(object : CustomClickEvent.OnClickListener {
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

                override fun onError(v: View, e: CustomClickEvent.TooShortClickTimeException) {
                    runOnUiThread {
                        Toast.makeText(v.context, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            })
            setCleanUp(Runnable {
                binding.root.postDelayed({
                    message.set("")
                }, 1000)
            })
        }
    }

    class CustomClickEvent(val view: View, val longDivision: Long = 500L, val longLowerLimit: Long = 1000L) {

        class TooShortClickTimeException(private val time: Long) : Exception() {
            override val message: String
                get() = "You must be keep click longer than $time ms."
        }

        private var isPressing = false

        private var timer: Timer? = null

        private val handler = Handler()

        private var cleanUpRunnable: Runnable? = null

        init {
            view.setOnTouchListener { _, e ->
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isPressing = true
                        scheduledTimer()
                    }
                    MotionEvent.ACTION_UP -> {
                        isPressing = false
                        terminateTimer()
                    }
                    else -> Unit
                }
                true
            }
        }

        fun setCleanUp(r: Runnable) {
            cleanUpRunnable = r
        }

        private fun scheduledTimer() {
            val pressTime = Date().time
            timer = Timer("CustomClickEventTimer")
            timer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val duration = Date().time - pressTime
                    val isLong = duration < longDivision
                    when (isPressing) {
                        true -> if (isLong) {
                        } else {
                            listener?.onLongClick(false)
                        }
                        else -> if (isLong) {
                            listener?.onClick()
                        } else {
                            if ((duration - longDivision) < longLowerLimit) {
                                listener?.onError(view, TooShortClickTimeException(longLowerLimit))
                            } else {
                                listener?.onLongClick(true)
                            }
                        }
                    }
                }
            }, 10, 10)
        }

        private fun terminateTimer() {
            handler.postDelayed({
                cleanUpRunnable?.run()
                timer?.cancel()
                timer?.purge()
            }, 10)
        }

        interface OnClickListener {
            fun onClick()
            fun onLongClick(isFinished: Boolean)
            fun onError(v: View, e: TooShortClickTimeException)
        }

        private var listener: OnClickListener? = null

        fun setOnClickListener(listener: OnClickListener) {
            this.listener = listener
        }
    }
}
