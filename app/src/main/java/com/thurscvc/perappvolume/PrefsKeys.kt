package com.thurscvc.perappvolume

import android.content.Context
import android.content.SharedPreferences

object PrefsKeys {
    const val GROUP = "per_app_volume"
    const val ENABLED = "enabled"
    const val RESTORE_VOLUME = "restore_volume"
    const val DEFAULT_PERCENT = 0

    fun volumeKey(packageName: String) = "volume_$packageName"

    fun appEnabledKey(packageName: String) = "app_enabled_$packageName"

    fun rememberKey(packageName: String) = "remember_$packageName"
}

object UiPrefs {
    private const val FILE = "ui"
    const val GLASS = "glass"
    const val TOP_BAR_BLUR = "top_bar_blur"
    const val HIDE_ICON = "hide_icon"

    fun get(context: Context): SharedPreferences =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
}
