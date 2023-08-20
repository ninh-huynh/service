package com.ninhh.example.servicesample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: "Unknown action"
        val randomInt = intent.getIntExtra("randomInt", -1)
        Timber.i("RandomInt received from another process: $randomInt with action $action")
    }
}