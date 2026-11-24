package com.nurra.islamic.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nurra.islamic.MainActivity
import com.nurra.islamic.R

class AdhanAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("EXTRA_PRAYER_NAME") ?: "Salat"
        val isPrePrayer = intent.getBooleanExtra("EXTRA_IS_PRE_PRAYER", false)
        val isSnooze = intent.getBooleanExtra("EXTRA_IS_SNOOZE", false)
        val minutesBefore = intent.getIntExtra("EXTRA_PRE_MINUTES", 10)

        val notifId = prayerName.hashCode() + if (isPrePrayer) 100 else 0

        Log.d("AdhanAlarmReceiver", "Adhan Alarm Fired for: $prayerName (isPre = $isPrePrayer, isSnooze = $isSnooze)")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "nurra_adhan_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Pengingat Adzan & Salat Nurra",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat waktu adzan dan salat 5 waktu"
                enableVibration(true)
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingMainIntent = PendingIntent.getActivity(
            context,
            notifId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: "Sudah Salat"
        val markCompletedIntent = Intent(context, AdhanActionReceiver::class.java).apply {
            action = AdhanActionReceiver.ACTION_MARK_PRAYER_COMPLETED
            putExtra("EXTRA_PRAYER_NAME", prayerName)
            putExtra("EXTRA_NOTIF_ID", notifId)
        }
        val markCompletedPendingIntent = PendingIntent.getBroadcast(
            context,
            notifId + 1000,
            markCompletedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: "Ingatkan 10 Menit Lagi"
        val snoozeIntent = Intent(context, AdhanActionReceiver::class.java).apply {
            action = AdhanActionReceiver.ACTION_SNOOZE_10_MINUTES
            putExtra("EXTRA_PRAYER_NAME", prayerName)
            putExtra("EXTRA_NOTIF_ID", notifId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notifId + 2000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when {
            isSnooze -> "⏰ Pengingat Lanjutan Salat $prayerName"
            isPrePrayer -> "⌛ $minutesBefore Menit Sebelum Salat $prayerName"
            else -> "🌙 Waktu Salat $prayerName"
        }

        val body = when {
            isSnooze -> "Pengingat 10 menit telah berlalu. Yuk, tunaikan salat $prayerName sekarang."
            isPrePrayer -> "Persiapkan diri untuk menunaikan salat $prayerName dalam $minutesBefore menit lagi."
            else -> "Waktu $prayerName telah tiba. Yuk, tunaikan salat dan jangan lupa berdoa."
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_crescent)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingMainIntent)
            .addAction(0, "Sudah Salat", markCompletedPendingIntent)
            .addAction(0, "Ingatkan 10 Menit Lagi", snoozePendingIntent)
            .build()

        notificationManager.notify(notifId, notification)
    }
}
