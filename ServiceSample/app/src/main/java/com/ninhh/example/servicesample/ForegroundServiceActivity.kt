package com.ninhh.example.servicesample

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ninhh.example.servicesample.databinding.ActivityForegroundServiceBinding
import timber.log.Timber


class ForegroundServiceActivity : AppCompatActivity() {

    companion object {
        var id1 = "test_channel_01"
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val REQUIRED_PERMISSIONS = arrayOf<String>(Manifest.permission.POST_NOTIFICATIONS)


    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityForegroundServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // for notifications permission now required in api 33
        //this allows us to check with multiple permissions, but in this case (currently) only need 1.
        val rpl = registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted ->
            var granted = true
            for ((key, value) in isGranted) {
                Timber.i("$key is $value")
                if (!value) granted = false
            }


            if (granted) Timber.i("Permissions granted for api 33+")
        }

        binding.button.setOnClickListener {
            val resultIntent = Intent(this, MyReceiver::class.java)
            val resultPendingIntent = PendingIntent.getBroadcast(
                this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            val serviceIntent = Intent(this, MyForeGroundService::class.java).apply {
                putExtra("times", if (count % 2 == 0) 5 else 10)
                putExtra("pending-intent", resultPendingIntent)
            }

            ContextCompat.startForegroundService(this, serviceIntent)

            count++
        }

        binding.buttonStopServiceMainProc.setOnClickListener {
            stopService(Intent(this, MyForeGroundService::class.java))
        }


        //needed for the persistent notification created in service. Applied for API >= 26 (Android O)
        createChannelCompat()

        //for the new api 33+ notifications permissions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!allPermissionsGranted()) {
                rpl.launch(REQUIRED_PERMISSIONS)
            }
        }
    }

    private fun createChannelCompat() {
        val notificationManagerCompat = NotificationManagerCompat.from(this)

        //important level: default is is high on the phone.  high is urgent on the phone.  low is medium, so none is low?
        val channelCompat=  NotificationChannelCompat.Builder(id1, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(getString(R.string.channel_name))
            .setDescription(getString(R.string.channel_description))

            // Sets the notification light color for notifications posted to this channel, if the device supports this feature.
            .setLightsEnabled(true)
            .setShowBadge(true)
            .build()


        notificationManagerCompat.createNotificationChannel(channelCompat)
    }

    //ask for permissions when we start.
    private fun allPermissionsGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (permission in REQUIRED_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }
}