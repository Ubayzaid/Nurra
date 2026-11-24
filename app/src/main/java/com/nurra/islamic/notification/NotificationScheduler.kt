package com.nurra.islamic.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.nurra.islamic.model.DailyPrayerSchedule
import com.nurra.islamic.repository.AppSettings
import java.util.Calendar
import java.util.TimeZone

class NotificationScheduler(private val context: Context) {

    private val TAG = "NurraNotifScheduler"
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun schedulePrayerAlarms(
        schedule: DailyPrayerSchedule,
        settings: AppSettings
    ) {
        if (!settings.isAdhanNotificationEnabled) {
            cancelAllAlarms()
            return
        }

        val tz = TimeZone.getTimeZone(schedule.location.countryName) // or WIB/WITA/WIT
        val now = Calendar.getInstance()

        schedule.prayerList.forEach { item ->
            val prayerName = item.name
            val isEnabledForPrayer = when (prayerName.lowercase()) {
                "subuh" -> settings.isSubuhNotifEnabled
                "dzuhur", "zuhur" -> settings.isDzuhurNotifEnabled
                "ashar", "asar" -> settings.isAsharNotifEnabled
                "maghrib", "magrib" -> settings.isMaghribNotifEnabled
                "isya" -> settings.isIsyaNotifEnabled
                else -> false
            }

            val requestCode = prayerName.hashCode()

            if (!isEnabledForPrayer) {
                cancelAlarm(requestCode)
                return@forEach
            }

            // Parse time string e.g. "04:35 WIB"
            val timeParts = item.time.replace(" WIB", "").replace(" WITA", "").replace(" WIT", "").trim().split(":")
            if (timeParts.size == 2) {
                val hour = timeParts[0].toIntOrNull() ?: return@forEach
                val minute = timeParts[1].toIntOrNull() ?: return@forEach

                val prayerCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // If prayer time today has passed, schedule for tomorrow at the same time
                if (prayerCal.timeInMillis <= now.timeInMillis) {
                    prayerCal.add(Calendar.DAY_OF_YEAR, 1)
                }

                val intent = Intent(context, AdhanAlarmReceiver::class.java).apply {
                    putExtra("EXTRA_PRAYER_NAME", prayerName)
                    putExtra("EXTRA_IS_PRE_PRAYER", false)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            prayerCal.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            prayerCal.timeInMillis,
                            pendingIntent
                        )
                    }
                    Log.d(TAG, "Scheduled Adhan Alarm for $prayerName at ${prayerCal.time}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error scheduling alarm for $prayerName: ${e.message}")
                }

                // Pre-Prayer Reminder if enabled
                if (settings.isPrePrayerReminderEnabled) {
                    val preCal = (prayerCal.clone() as Calendar).apply {
                        add(Calendar.MINUTE, -settings.prePrayerMinutes)
                    }
                    if (preCal.timeInMillis > now.timeInMillis) {
                        val preIntent = Intent(context, AdhanAlarmReceiver::class.java).apply {
                            putExtra("EXTRA_PRAYER_NAME", prayerName)
                            putExtra("EXTRA_IS_PRE_PRAYER", true)
                            putExtra("EXTRA_PRE_MINUTES", settings.prePrayerMinutes)
                        }
                        val prePendingIntent = PendingIntent.getBroadcast(
                            context,
                            requestCode + 100,
                            preIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        try {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                preCal.timeInMillis,
                                prePendingIntent
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error scheduling pre-prayer alarm for $prayerName: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    fun cancelAlarm(requestCode: Int) {
        val intent = Intent(context, AdhanAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        val prePendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode + 100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(prePendingIntent)
    }

    fun cancelAllAlarms() {
        listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya").forEach { name ->
            cancelAlarm(name.hashCode())
        }
    }
}
