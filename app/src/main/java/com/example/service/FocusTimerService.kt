package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.example.ui.notification.TimerNotificationManager

class FocusTimerService : Service() {

    private lateinit var notificationManager: TimerNotificationManager

    companion object {
        const val ACTION_START = "com.example.service.action.START"
        const val ACTION_UPDATE = "com.example.service.action.UPDATE"
        const val ACTION_STOP = "com.example.service.action.STOP"

        const val EXTRA_TIME_FORMATTED = "extra_time_formatted"
        const val EXTRA_SPECIES_NAME = "extra_species_name"
        const val EXTRA_SUBJECT_TAG = "extra_subject_tag"
        const val EXTRA_IS_COUNTDOWN = "extra_is_countdown"

        fun start(
            context: Context,
            timeFormatted: String,
            speciesName: String,
            subjectTag: String,
            isCountdown: Boolean
        ) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TIME_FORMATTED, timeFormatted)
                putExtra(EXTRA_SPECIES_NAME, speciesName)
                putExtra(EXTRA_SUBJECT_TAG, subjectTag)
                putExtra(EXTRA_IS_COUNTDOWN, isCountdown)
            }
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun update(
            context: Context,
            timeFormatted: String,
            speciesName: String,
            subjectTag: String,
            isCountdown: Boolean
        ) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_TIME_FORMATTED, timeFormatted)
                putExtra(EXTRA_SPECIES_NAME, speciesName)
                putExtra(EXTRA_SUBJECT_TAG, subjectTag)
                putExtra(EXTRA_IS_COUNTDOWN, isCountdown)
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = TimerNotificationManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        val timeFormatted = intent.getStringExtra(EXTRA_TIME_FORMATTED) ?: "00:00"
        val speciesName = intent.getStringExtra(EXTRA_SPECIES_NAME) ?: "Tree"
        val subjectTag = intent.getStringExtra(EXTRA_SUBJECT_TAG) ?: "Focus"
        val isCountdown = intent.getBooleanExtra(EXTRA_IS_COUNTDOWN, true)

        when (action) {
            ACTION_START -> {
                val notification = notificationManager.buildNotification(
                    timeFormatted = timeFormatted,
                    speciesName = speciesName,
                    subjectTag = subjectTag,
                    isCountdown = isCountdown
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val foregroundType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    } else {
                        0
                    }
                    startForeground(TimerNotificationManager.NOTIFICATION_ID, notification, foregroundType)
                } else {
                    startForeground(TimerNotificationManager.NOTIFICATION_ID, notification)
                }
            }
            ACTION_UPDATE -> {
                notificationManager.showOrUpdateTimerNotification(
                    timeFormatted = timeFormatted,
                    speciesName = speciesName,
                    subjectTag = subjectTag,
                    isCountdown = isCountdown
                )
            }
            ACTION_STOP -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                notificationManager.cancelNotification()
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        notificationManager.cancelNotification()
    }
}
