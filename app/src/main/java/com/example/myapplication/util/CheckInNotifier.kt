package com.example.myapplication.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapplication.R
import com.example.myapplication.model.BoardingPass

/** Owns the boarding-pass notification channel and posts the "check-in complete" notification. */
class CheckInNotifier(private val context: Context) {

    init { ensureNotificationChannel() }

    fun postCheckInComplete(boardingPass: BoardingPass) {
        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply { setPackage(null); flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val contentPI = launchIntent?.let {
            PendingIntent.getActivity(context, boardingPass.id.hashCode(), it, pendingFlags)
        }

        val depTime = boardingPass.departureTime.takeLast(5)
        val bigText = """
            ✈ ${boardingPass.origin} → ${boardingPass.destination}
            Flight ${boardingPass.flightNumber} · ${boardingPass.airlineName}
            Seat ${boardingPass.seat} · Gate ${boardingPass.gate} · Boarding ${boardingPass.boardingGroup}
            Departure $depTime · Terminal ${boardingPass.terminal}
        """.trimIndent()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(Color.parseColor("#004D2F"))
            .setColorized(true)
            .setContentTitle("Boarding pass ready")
            .setContentText("${boardingPass.flightNumber} · ${boardingPass.origin}→${boardingPass.destination} · Seat ${boardingPass.seat}")
            .setSubText(boardingPass.airlineName)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("Boarding pass ready · ${boardingPass.flightNumber}")
                    .bigText(bigText)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(contentPI)
            .build()

        runCatching {
            val notifManager = NotificationManagerCompat.from(context)
            if (notifManager.areNotificationsEnabled()) {
                notifManager.notify(boardingPass.id.hashCode(), notification)
            }
        }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(CHANNEL_ID, "Boarding pass updates", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Confirms when your boarding pass is ready and reminds you about boarding"
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
                lightColor = Color.parseColor("#004D2F")
            }
            manager.createNotificationChannel(channel)
        }
    }

    private companion object {
        const val CHANNEL_ID = "checkin_updates"
    }
}
