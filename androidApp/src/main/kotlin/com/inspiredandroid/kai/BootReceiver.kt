package com.inspiredandroid.kai

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.inspiredandroid.kai.data.AppSettings
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val appSettings: AppSettings by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            try {
                if (appSettings.isSchedulingEnabled() && appSettings.isDaemonEnabled()) {
                    val serviceIntent = Intent(context, DaemonService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                }
            } catch (_: Exception) {
                // Ignore background start restrictions if triggered before user unlock
            }
        }
    }
}
