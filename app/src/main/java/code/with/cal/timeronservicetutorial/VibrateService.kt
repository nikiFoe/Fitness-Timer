package code.with.cal.timeronservicetutorial

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import java.util.*

class VibrateService : Service()
{
    override fun onBind(p0: Intent?): IBinder? = null
    private lateinit var v : Vibrator

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        v.vibrate(
            VibrationEffect.createOneShot(500,
            VibrationEffect.DEFAULT_AMPLITUDE))
        return START_NOT_STICKY
    }

    override fun onDestroy()
    {
        super.onDestroy()
    }

    }