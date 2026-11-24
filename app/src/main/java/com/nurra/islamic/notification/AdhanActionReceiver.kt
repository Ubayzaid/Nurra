package com.nurra.islamic.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.nurra.islamic.repository.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdhanActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_PRAYER_COMPLETED = "com.nurra.islamic.ACTION_MARK_PRAYER_COMPLETED"
        const val ACTION_SNOOZE_10_MINUTES = "com.nurra.islamic.ACTION_SNOOZE_10_MINUTES"
        private const val TAG = "AdhanActionReceiver"
    }

    @SuppressLint("ScheduleExactAlarm")
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val prayerName = intent.getStringExtra("EXTRA_PRAYER_NAME") ?: "Salat"
        val notifId = intent.getIntExtra("EXTRA_NOTIF_ID", prayerName.hashCode())

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notifId)

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        when (action) {
            ACTION_MARK_PRAYER_COMPLETED -> {
                Log.d(TAG, "Marking prayer completed: $prayerName for date $todayDate")

                CoroutineScope(Dispatchers.IO).launch {
                    val key = booleanPreferencesKey("completed_${todayDate}_${prayerName.lowercase()}")
                    context.dataStore.edit { prefs ->
                        prefs[key] = true
                    }
                }

                Toast.makeText(
                    context,
                    "Alhamdulillah, Salat $prayerName telah ditandai selesai 🤲",
                    Toast.LENGTH_SHORT
                ).show()
            }

            ACTION_SNOOZE_10_MINUTES -> {
                Log.d(TAG, "Snoozing prayer reminder: $prayerName for 10 minutes")

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val snoozeTimeMs = System.currentTimeMillis() + (10 * 60 * 1000L) // Exactly 10 minutes later

                val snoozeIntent = Intent(context, AdhanAlarmReceiver::class.java).apply {
                    putExtra("EXTRA_PRAYER_NAME", prayerName)
                    putExtra("EXTRA_IS_SNOOZE", true)
                }

                val snoozePendingIntent = PendingIntent.getBroadcast(
                    context,
                    prayerName.hashCode() + 500,
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            snoozeTimeMs,
                            snoozePendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            snoozeTimeMs,
                            snoozePendingIntent
                        )
                    }

                    Toast.makeText(
                        context,
                        "Pengingat $prayerName dijadwalkan ulang 10 menit lagi ⏰",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting snooze alarm: ${e.message}")
                }
            }
        }
    }
}
