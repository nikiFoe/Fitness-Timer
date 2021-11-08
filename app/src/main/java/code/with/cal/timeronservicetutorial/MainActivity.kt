package code.with.cal.timeronservicetutorial

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.RemoteViews
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.RecyclerView
import code.with.cal.timeronservicetutorial.databinding.ActivityMainBinding
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.round

class MainActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMainBinding
    private var measurmentStarted = false
    private lateinit var serviceIntent: Intent
    private lateinit var serviceIntentAcc: Intent
    private lateinit var serviceIntentVibration: Intent
    private lateinit var serviceIntentTest: Intent
    private var time = 0.0
    private var acceleration = 0.0
    private var current_accel = 0.0
    private var timerStart = false
    private var timerRunning = false
    private var mSensorManager : SensorManager ?= null
    private var mAccelerometer : Sensor ?= null
    private lateinit var v : Vibrator
    private var TAG = "MainActivity"
    private var wantedTime = 25.0


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startStopButton.setOnClickListener { startStopMeasurment() }
        binding.resetButton.setOnClickListener { resetTimer() }

        serviceIntent = Intent(applicationContext, TimerService::class.java)
        registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))

        serviceIntentAcc = Intent(applicationContext, AccelerationService::class.java)
        registerReceiver(updateAccel, IntentFilter(AccelerationService.ACC_Updated))
        //intent.putExtra(AccelerationService.TIMER_RUNNING, false)

        serviceIntentVibration = Intent(applicationContext, VibrateService::class.java)
        serviceIntentTest = Intent(applicationContext, AccelerationService::class.java)


        Log.d(TAG, "onCreat")

        v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        binding.volumseekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                Log.d(TAG, "ProgressbarChange " + p1)
                binding.volume.text = p1.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                Log.d(TAG, "Progressbar_StartTouch")
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                Log.d(TAG, "Progressbar_StopTouch")
                wantedTime = (binding.volume.text.toString()).toDouble()
            }
        })
    }



    private fun resetTimer()
    {
        stopTimer()
        //startMeasurment()
        time = 0.0
        binding.timeTV.text = getTimeStringFromDouble(time)
    }

    private fun startStopMeasurment()
    {
        if(timerRunning) {
            stopTimer()
            Log.d(TAG, "TimerStop")
        }else {
            startMeasurment()

        }
    }

    private fun startMeasurment()
    {
        serviceIntentAcc.putExtra(AccelerationService.ACC_Updated, acceleration)
        startService(serviceIntentAcc)
        binding.startStopButton.text = "Stop"
        binding.startStopButton.icon = getDrawable(R.drawable.ic_baseline_pause_24)
        measurmentStarted = true
    }


    private fun startTimer()
    {
        serviceIntent.putExtra(TimerService.TIMER_UPDATED, time)
        serviceIntent.putExtra(TimerService.INTERVAL, wantedTime)
        startService(serviceIntent)
        Log.d(TAG, "TimerStart")
    }



    private fun stopTimer()
    {
        stopService(serviceIntent)
        //timerRunning = false
        //serviceIntentAcc.putExtra(AccelerationService.TIMER_RUNNING, timerRunning)
        binding.startStopButton.text = "Start"
        binding.startStopButton.icon = getDrawable(R.drawable.ic_baseline_play_arrow_24)
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {
            time = intent.getDoubleExtra(TimerService.TIME_EXTRA, 0.0)
            binding.timeTV.text = getTimeStringFromDouble(time)
            if(time>wantedTime)
            {
                resetTimer()
                timerRunning = false
                serviceIntentAcc.putExtra(AccelerationService.TIMER_RUNNING, timerRunning)
                var newIntent = Intent(applicationContext, AccelerationService::class.java)
                newIntent.putExtra(AccelerationService.TIMER_RUNNING, false)
                startService(newIntent)
                binding.timeTV.text = getTimeStringFromDouble(time)
                Log.d(TAG, "TimerRunOut")
            }
        }
    }

    private val updateAccel:BroadcastReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {
            acceleration = serviceIntentAcc.getDoubleExtra(AccelerationService.ACC_EXTRA, 0.0)
            timerStart = serviceIntentAcc.getBooleanExtra(AccelerationService.TIMER_START, false)
            Log.d("timerStart", timerStart.toString())
            //timerRunning = intent.getBooleanExtra(AccelerationService.TIMER_RUNNING,true)
            current_accel = round2Decimal(acceleration)
            binding.accel.text = current_accel.toString()
            timerRunning = true
            serviceIntentAcc.putExtra(AccelerationService.TIMER_RUNNING, timerRunning)
            timerStart = false
            serviceIntentAcc.putExtra(AccelerationService.TIMER_START, timerStart)
            var newIntent = Intent(applicationContext, AccelerationService::class.java)
            newIntent.putExtra(AccelerationService.TIMER_RUNNING, timerRunning)
            startService(newIntent)
            //sendBroadcast(newIntent)
            startTimer()
            Log.d("TimmerRunning", serviceIntentAcc.getBooleanExtra(AccelerationService.TIMER_RUNNING, false).toString())

        }
    }



    private fun round2Decimal(accel: Double): Double
    {
        val accel_rounded = round((accel*100))/100
        return accel_rounded
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