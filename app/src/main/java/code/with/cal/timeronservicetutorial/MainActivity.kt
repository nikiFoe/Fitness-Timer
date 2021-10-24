package code.with.cal.timeronservicetutorial

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import code.with.cal.timeronservicetutorial.databinding.ActivityMainBinding
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMainBinding
    private var timerStarted = false
    private lateinit var serviceIntent: Intent
    private lateinit var serviceIntentAcc: Intent
    private var time = 0.0
    private var acceleration = 0.0
    private var mSensorManager : SensorManager ?= null
    private var mAccelerometer : Sensor ?= null


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startStopButton.setOnClickListener { startStopTimer() }
        binding.resetButton.setOnClickListener { resetTimer() }

        serviceIntent = Intent(applicationContext, TimerService::class.java)
        registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))

        serviceIntentAcc = Intent(applicationContext, AccelerationService::class.java)
        registerReceiver(updateAccel, IntentFilter(AccelerationService.ACC_Updated))

        Log.d("MainActivity", "onCreat")

    }

    private fun resetTimer()
    {
        stopTimer()
        time = 0.0
        binding.timeTV.text = getTimeStringFromDouble(time)
    }

    private fun startStopTimer()
    {
        if(timerStarted)
            stopTimer()
        else
            startTimer()
    }

    private fun startTimer()
    {
        serviceIntentAcc.putExtra(AccelerationService.ACC_Updated, acceleration)
        startService(serviceIntentAcc)
        binding.startStopButton.text = "Stop"
        binding.startStopButton.icon = getDrawable(R.drawable.ic_baseline_pause_24)
        timerStarted = true
        Log.d("MainActivity", "Start")
    }

    private fun startMeasuring()
    {
        serviceIntentAcc.putExtra(AccelerationService.ACC_Updated, acceleration)
        startService(serviceIntentAcc)

    }

    private fun stopTimer()
    {
        stopService(serviceIntent)
        binding.startStopButton.text = "Start"
        binding.startStopButton.icon = getDrawable(R.drawable.ic_baseline_play_arrow_24)
        timerStarted = false
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {
            time = intent.getDoubleExtra(TimerService.TIME_EXTRA, 0.0)
            binding.timeTV.text = getTimeStringFromDouble(time)
        }
    }

    private val updateAccel:BroadcastReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {

            acceleration = intent.getDoubleExtra(AccelerationService.ACC_EXTRA, 0.2)
            binding.accel.text = acceleration.toString()
            Log.d("MainActivity_Receive", (acceleration.toString()))
            /*val v = (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500,
                    VibrationEffect.DEFAULT_AMPLITUDE))
            }
            else {
                v.vibrate(500)
            }*/
        }
    }

    private fun getTimeStringFromDouble(time: Double): String
    {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60

        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hour: Int, min: Int, sec: Int): String = String.format(
        "%02d:%02d:%02d",
        hour,
        min,
        sec
    )
}