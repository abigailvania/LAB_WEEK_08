package com.example.lab_week_08

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SecondNotificationService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}