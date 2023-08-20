package com.ninhh.example.servicesample

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Process
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.BundleCompat
import timber.log.Timber
import java.util.Random


class MyForeGroundService : Service() {

    private var mServiceHandler: ServiceHandler? = null
    private var handlerThread: HandlerThread? = null
    private val TAG = "MyForegroundService"
    private val notificationBuilder by lazy {
        newNotificationBuilder("My Service is stared")
    }

    private val notificationManagerCompat: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(this)
    }

    var r: Random? = null

    private var mostRecentStartId = -1

    // Handler that receives messages from the thread
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            // Normally we would do some work here, like download a file.
            // For our example, we just sleep for 5 seconds then display toasts.
            //setup how many messages
            val times: Int = msg.data.getInt("times", 1)

            val pendingIntent: PendingIntent? = BundleCompat.getParcelable(
                msg.data, "pending-intent", PendingIntent::class.java
            )
            Timber.tag(TAG).i("handleMessage with input: times=$times, pendingIntent=$pendingIntent")

            //loop that many times, sleeping for 2 seconds.
            var i: Int = 0
            notificationBuilder.setContentText("My Service updating...")
            while (i < times) {
                updateNotificationProgress(times, i)
                try {
                    Thread.sleep(5000)

                } catch (e: InterruptedException) {
                    Timber.tag(TAG).e(e)
                }

                val someInt = r!!.nextInt(100)
                Timber.tag(TAG).i("$i random $someInt")


                try {
                    pendingIntent?.send(this@MyForeGroundService, Activity.RESULT_OK, Intent("my-action").apply {
                        putExtra("randomInt", someInt)
                    })
                } catch (e: PendingIntent.CanceledException) {
                    Timber.e(e)
                }

                i++
            }
            notificationBuilder.setContentText("My Service done")
            updateNotificationProgress(times, times)

//            // Stop the service using the startId, so that we don't stop
//            // the service in the middle of handling another job
//            stopSelf(msg.arg1) //notification will go away as well.
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateNotificationProgress(max: Int, progress: Int) {
        val noti = notificationBuilder.setProgress(max, progress, false)
            .build()
        notificationManagerCompat.notify(notiID, noti)
    }

    private val notiID = 12345

    override fun onCreate() {
        r = Random()
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        handlerThread =
            HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
                start()
                mServiceHandler = ServiceHandler(looper)
            }

        //promote to foreground and create persistent notification.
        //in Oreo we only have a few seconds to do this or the service is killed.
        val notification: Notification = notificationBuilder.build()
        startForeground(
            notiID,
            notification
        ) //the ID as same as the notification id.  can't be zero.
        Timber.i("onCreate service")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mostRecentStartId = startId
        Timber.i("onStartCommand with startId=$startId")

        notificationManagerCompat
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        val msg: Message = mServiceHandler!!.obtainMessage()
        msg.arg1 = startId //needed for stop.
        if (intent != null) {
            msg.data = intent.extras
            mServiceHandler!!.sendMessage(msg)
        } else {
            Timber.i("The Intent to start is null")
        }

        // If we get killed, after returning from here, restart
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {
        handlerThread?.quitSafely()
        notificationManagerCompat.cancel(notiID)
        Timber.i("service done")
    }

    private fun newNotificationBuilder(message: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext, ForegroundServiceActivity.id1)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true) //persistent notification!
            .setChannelId(ForegroundServiceActivity.id1)
            .setContentTitle("Service") //Title message top row.
            .setContentText(message) //message when looking at the notification, second row
    }
}