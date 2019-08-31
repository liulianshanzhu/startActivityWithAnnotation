package com.durian.annotationdemo

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.durian.annotation.Creator
import com.durian.annotation.Extra

@Creator
class MyService : Service() {

    @Extra(false)
    var isFalse = false

    @Extra(true)
    var isTrue = true

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

}
