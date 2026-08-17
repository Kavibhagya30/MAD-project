package com.piieradication.agent.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.piieradication.agent.R
import com.piieradication.agent.domain.model.DeletionRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local, on-device notifications for deletion-request status changes.
 * A production build would additionally push these via Firebase Cloud
 * Messaging so status changes computed on a server reach the device
 * even when the app process isn't running — that needs a live Firebase
 * project (`google-services.json`) and backend, neither of which exist
 * in this offline build, so this is the honest, working local
 * equivalent (see README "Swapping in Firebase").
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Deletion request updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies you when a data broker confirms deletion of your info."
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun notifyCompleted(request: DeletionRequest) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Deletion confirmed")
            .setContentText("${request.brokerName} confirmed removal of ${request.recordDisplayName}'s data.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(request.id.toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "deletion_request_updates"
    }
}
