package com.masterminds.pulsecast.core

import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * There is no public Android API for a third-party app to draw touch
 * indicators over OTHER apps' screens system-wide — that's a platform
 * limitation, not something worth trying to work around with a hacky
 * accessibility-service trick. What legitimate screen recorders actually
 * do is toggle the OS-level Developer Option "Show taps"
 * (Settings.System.SHOW_TOUCHES), which needs WRITE_SECURE_SETTINGS — a
 * signature-level permission that can't be granted through a normal
 * runtime permission dialog, only via:
 *
 *     adb shell pm grant <applicationId> android.permission.WRITE_SECURE_SETTINGS
 *
 * Without that grant, enable() throws SecurityException, and the caller
 * should fall back to sending the user to Developer Options to flip it on
 * by hand — see MainActivity's usage of this class.
 */
object TouchVisualization {

    fun isEnabled(context: Context): Boolean =
        try {
            Settings.System.getInt(context.contentResolver, "show_touches", 0) == 1
        } catch (e: Exception) {
            false
        }

    /** @return true if it actually succeeded, false if the permission isn't granted. */
    fun trySetEnabled(context: Context, enabled: Boolean): Boolean {
        return try {
            Settings.System.putInt(
                context.contentResolver,
                "show_touches",
                if (enabled) 1 else 0
            )
            true
        } catch (e: SecurityException) {
            false
        }
    }

    fun openDeveloperOptions(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
