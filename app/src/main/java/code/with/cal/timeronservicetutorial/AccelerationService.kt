package code.with.cal.timeronservicetutorial

import android.app.Service
import android.app.Service.START_NOT_STICKY
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log

class AccelerationService : Service(), SensorEventListener
{
    override fun onBind(p0: Intent?): IBinder? = null
    private var mSensorManager : SensorManager?= null
    private var mAccelerometer : Sensor?= null
    private var resume = false;

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
        return START_NOT_STICKY
    }

    override fun onDestroy()
    {
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val intent = Intent(ACC_Updated)
            intent.putExtra(ACC_EXTRA, event.values[2].toDouble())
            Log.d("MainActivity_value", intent.getDoubleExtra(ACC_EXTRA, event.values[2].toDouble()).toString() )
            sendBroadcast(intent)

        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }


    companion object
    {
        const val ACC_Updated = "accUpdated"
        const val ACC_EXTRA = "accExtra"
    }
}