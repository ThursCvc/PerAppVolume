package com.thurscvc.perappvolume

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Module-side mirror of the last volume adjusted inside a target app. The
 * target app stores the authoritative value in its own SharedPreferences;
 * this store only feeds the configuration page, so the slider follows the
 * remembered value.
 */
object RememberStore {
    const val ACTION = "com.thurscvc.perappvolume.REMEMBER_VOLUME"
    private const val FILE = "remembered_volumes"

    fun get(context: Context, packageName: String): Int =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getInt(packageName, -1)

    fun put(context: Context, packageName: String, percent: Int) {
        if (percent !in 0..100) return
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putInt(packageName, percent)
            .apply()
    }

    fun remove(context: Context, packageName: String) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .remove(packageName)
            .apply()
    }
}

class RememberReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != RememberStore.ACTION) return
        val packageName = intent.getStringExtra("package") ?: return
        val percent = intent.getIntExtra("percent", -1)
        RememberStore.put(context, packageName, percent)
    }
}
