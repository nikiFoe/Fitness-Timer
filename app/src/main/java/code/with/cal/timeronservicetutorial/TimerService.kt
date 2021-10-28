package code.with.cal.timeronservicetutorial

import android.app.*
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import java.util.*


class TimerService : Service()
{
    override fun onBind(p0: Intent?): IBinder? = null

    private val timer = Timer()
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "i.apps.notifications"
    private val description = "Test notification"

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        val time = intent.getDoubleExtra(TIME_EXTRA, 0.0)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.d("MainActivity", "Started Time")
        timer.scheduleAtFixedRate(TimeTask(time), 0, 1000)
        return START_NOT_STICKY
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("TimerService", "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy()
    {
        Log.d("TimerService", "StopTimer")
        super.onDestroy()
        timer.cancel()
        notificationManager.cancel(12345)
    }


    private inner class TimeTask(private var time: Double) : TimerTask() {
        override fun run() {
            val intent = Intent(TIMER_UPDATED)
            time++
            intent.putExtra(TIME_EXTRA, time)
            sendBroadcast(intent)
            if ((time % 5).equals(0.0)) {
                test(true, time)
                Log.d("TimerService", (time % 5).equals(0.0).toString())
            } else {
                Log.d("TimerService", (time % 5).equals(0.0).toString())
                test(false, time)
            }
        }
    }

    fun test(count: Boolean, runningTime: Double)
    {
        if (count) {
            val pattern = longArrayOf(400, 600, 500)
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
                notificationChannel.vibrationPattern = pattern
                notificationManager.createNotificationChannel(notificationChannel)
                builder = Notification.Builder(this, channelId).setContentTitle(
                    "Fitness Timer Notification"
                ).setContentText("Timer runs for " + runningTime.toString() + "s") .setSmallIcon(R.drawable.ic_brightness)
                    .setLargeIcon(
                        BitmapFactory.decodeResource(
                            this.resources, R.drawable
                                .ic_launcher_background
                        )
                    ).setContentIntent(pendingIntent)
            }
            notificationManager.notify(12345, builder.build())
        }else{
            notificationManager.cancel(12345)
        }

    }

    companion object
    {
        const val TIMER_UPDATED = "timerUpdated"
        const val TIME_EXTRA = "timeExtra"
    }

}