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
        val isPrePrayer = intent.getBooleanVariable("EXTRA_IS_PRE_PRAYER", false)
        val minutesBefore = intent.getIntExtra("EXTRA_PRE_MINUTES", 10)

        Log.d("AdhanAlarmReceiver", "Adhan Alarm Fired for: $prayerName (isPre = $isPrePrayer)")

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
        val pendingIntent = PendingIntent.getActivity(
            context,
            prayerName.hashCode(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isPrePrayer) {
            "Pengingat $minutesBefore Menit Sebelum Salat $prayerName"
        } else {
            "Waktu Salat $prayerName Telah Tiba 🕌"
        }

        val body = if (isPrePrayer) {
            "Persiapkan diri untuk menunaikan salat $prayerName dalam $minutesBefore menit lagi."
        } else {
            "Saatnya menunaikan ibadah salat $prayerName. Mari utamakan salat tepat waktu."
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(prayerName.hashCode() + if (isPrePrayer) 100 else 0, notification)
    }

    private fun Intent.getBooleanVariable(key: String, defaultValue: Boolean): Boolean {
        return getBooleanExtra(key, defaultValue)
    }
}
