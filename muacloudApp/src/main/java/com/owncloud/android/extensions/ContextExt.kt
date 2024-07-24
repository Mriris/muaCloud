
package com.owncloud.android.extensions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
fun Context.createNotificationChannel(
    id: String,
    name: String,
    description: String,
    importance: Int
) {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val notificationChannel = NotificationChannel(id, name, importance).apply {
        setDescription(description)
    }

    notificationManager.createNotificationChannel(notificationChannel)
}
