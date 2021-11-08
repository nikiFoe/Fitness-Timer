package code.with.cal.timeronservicetutorial

import android.app.*
import android.app.Service.START_NOT_STICKY
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import java.util.*

class AccelerationService : Service(), SensorEventListener
{
    override fun onBind(p0: Intent?): IBinder? = null
    private var mSensorManager : SensorManager?= null
    private var mAccelerometer : Sensor?= null
    private var resume = false;
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "i.apps.notificationss"
    private val description = "notification Start"
    private var time = 0.0
    //private var intent_acc = Intent(INNER_TIMER_RUNNING)
    private var intent = Intent(ACC_Updated)
    private lateinit var testIntent: Intent


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {

        Log.d("MainActivity", "Started Acc1" )
        // Create the sensor manager
        //val acceleration = intent.getDoubleExtra(AccelerationService.ACC_EXTRA, 0.0)
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Specify the sensor you want to listen to
        mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            mSensorManager!!.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        Log.d("MainActivity", "Started Acc2" )
        intent.putExtra(TIMER_RUNNING, intent.getBooleanExtra(TIMER_RUNNING, false))
        Log.d("Timer_03", intent.getBooleanExtra(TIMER_RUNNING, false).toString())
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return START_NOT_STICKY
    }

    override fun onDestroy()
    {
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            Log.d("Timer_01", intent.getBooleanExtra(TIMER_RUNNING, false).toString())
            if (event.values[2].toDouble() > 14 && !intent.getBooleanExtra(INNER_TIMER_RUNNING, false) && !intent.getBooleanExtra(
                    TIMER_RUNNING, false)) {
                notificationCall()
                intent.putExtra(INNER_TIMER_RUNNING, true)
                val timer = Timer()
                timer.scheduleAtFixedRate(TimeTask(time, timer), 0, 1000)
                Log.d(
                    "InnerTimer_02",
                    intent.getBooleanExtra(INNER_TIMER_RUNNING, false).toString()
                )
                //innertimerStarted = true
                intent.putExtra(ACC_EXTRA, event.values[2].toDouble())
                intent.putExtra(TIMER_START, true)
                intent.putExtra(TIMER_RUNNING, false)
                sendBroadcast(intent)
            }
            /*val intent = Intent(ACC_Updated)
            val intent_acc = Intent(INNER_TIMER_RUNNING)
            intent.putExtra(ACC_EXTRA, event.values[2].toDouble())
            Log.d("MainActivity_value",intent_acc.getBooleanExtra(INNER_TIMER_RUNNING, false).toString() )
            if (event.values[2].toDouble() > 14 && (innerTimerRunning == false)){
                //innerTimerRunning = intent.getBooleanExtra(INNER_TIMER_RUNNING, false)
                timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
                intent.putExtra(TIMER_START, true)
                //intent.putExtra(TIMER_RUNNING, true)
                sendBroadcast(intent)
                notificationCall()
            }*/



        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private inner class TimeTask(private var time: Double, private val timer: Timer) : TimerTask() {
        override fun run() {
            time++
            Log.d("innerTimerClass", intent.getBooleanExtra(INNER_TIMER_RUNNING, false).toString())
            if (time > 4){
                Log.d("AccelerationTime", "Cancel" )
                intent.putExtra(INNER_TIMER_RUNNING, false)
                timer.cancel()

            }
        }
    }

    private fun notificationCall(){
        Log.d("Acceleration Not", "NotificationCall")
        val pattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        val intent = Intent(this, LauncherActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                channelId,
                description,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(500)
            notificationManager.createNotificationChannel(notificationChannel)
            builder = Notification.Builder(this, channelId).setContentTitle(
                "Fitness Timer Notification"
            ).setContentText("Timer starts now.") .setSmallIcon(R.drawable.ic_brightness)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        this.resources, R.drawable
                            .ic_launcher_background
                    )
                ).setContentIntent(pendingIntent)
        }
        notificationManager.notify(12457, builder.build())
    }
    companion object
    {
        const val ACC_Updated = "accUpdated"
        const val ACC_EXTRA = "accExtra"
        const val TIMER_START = "timerStart"
        const val TIMER_RUNNING = "timerisRunning"
        const val MEASUREWAIT = "measureWait"
        const val INNER_TIMER_RUNNING = "innerTimerRunning"
        const val TEST_VAR = "testVar"
    }
}