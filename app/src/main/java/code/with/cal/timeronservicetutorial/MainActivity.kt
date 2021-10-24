package code.with.cal.timeronservicetutorial

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import code.with.cal.timeronservicetutorial.databinding.ActivityMainBinding
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), SensorEventListener
{
    private lateinit var binding: ActivityMainBinding
    private var timerStarted = false
    private lateinit var serviceIntent: Intent
    private var time = 0.0
    private lateinit var mSensorManager: SensorManager
    private var mSensors: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startStopButton.setOnClickListener { startStopTimer() }
        binding.resetButton.setOnClickListener { resetTimer() }

        serviceIntent = Intent(applicationContext, TimerService::class.java)
        registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        Define the sensor
        mSensors = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override    fun onSensorChanged(p0: SensorEvent?) {
//        Sensor change value
        val millibarsOfPressure = p0!!.values[0]
        if (p0.sensor.type == Sensor.TYPE_ACCELEROMETER)
            Toast.makeText(this, "" + millibarsOfPressure + " lx", Toast.LENGTH_SHORT).show()
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
        serviceIntent.putExtra(TimerService.TIME_EXTRA, time)
        startService(serviceIntent)
        binding.startStopButton.text = "Stop"
        binding.startStopButton.icon = getDrawable(R.drawable.ic_baseline_pause_24)
        timerStarted = true
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