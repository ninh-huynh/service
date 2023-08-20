package com.ninhh.example.servicesample

import android.app.Application
import timber.log.Timber

class ServiceSampleApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}