package com.example.miuixvolume

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.HotReloadedParam
import io.github.libxposed.api.XposedModuleInterface.HotReloadingParam
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

class ModuleMain : XposedModule() {

    @Volatile
    private var enabled = true

    @Volatile
    private var targetPercent = PrefsKeys.DEFAULT_PERCENT

    private var remotePrefs: SharedPreferences? = null
    private var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    private val hookInstalled = AtomicBoolean(false)
    private val inForeground = AtomicBoolean(false)

    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private val leaveForeground = Runnable { inForeground.set(false) }

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        log(Log.INFO, TAG, "loaded in ${param.processName}")
    }

    override fun onPackageReady(param: PackageReadyParam) {
        loadPrefs(param.packageName)
        installHooks()
    }

    override fun onHotReloading(param: HotReloadingParam): Boolean {
        runCatching {
            val listener = prefsListener
            if (listener != null) {
                remotePrefs?.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }
        prefsListener = null
        handler.removeCallbacks(leaveForeground)
        return true
    }

    override fun onHotReloaded(param: HotReloadedParam) {
        param.oldHookHandles.forEach { it.unhook() }
        hookInstalled.set(false)
        inForeground.set(false)

        val packageName = param.processName.substringBefore(":")
        loadPrefs(packageName)
        installHooks()
    }

    private fun installHooks() {
        if (!hookInstalled.compareAndSet(false, true)) return

        val activityClass = Activity::class.java
        val onResume = activityClass.getDeclaredMethod("onResume").apply {
            isAccessible = true
        }
        val onPause = activityClass.getDeclaredMethod("onPause").apply {
            isAccessible = true
        }

        hook(onResume).intercept { chain ->
            val result = chain.proceed()
            val activity = chain.thisObject as? Activity

            if (activity != null && enabled) {
                handler.removeCallbacks(leaveForeground)
                if (inForeground.compareAndSet(false, true)) {
                    applyVolume(activity)
                }
            }

            result
        }

        hook(onPause).intercept { chain ->
            val result = chain.proceed()
            handler.removeCallbacks(leaveForeground)
            handler.postDelayed(leaveForeground, 700L)
            result
        }
    }

    private fun loadPrefs(packageName: String) {
        val percentKey = PrefsKeys.volumeKey(packageName)
        val prefs = runCatching {
            getRemotePreferences(PrefsKeys.GROUP)
        }.getOrNull()

        remotePrefs = prefs
        if (prefs == null) {
            enabled = true
            targetPercent = PrefsKeys.DEFAULT_PERCENT
            return
        }

        enabled = prefs.getBoolean(PrefsKeys.ENABLED, true)
        targetPercent = prefs.getInt(percentKey, PrefsKeys.DEFAULT_PERCENT)
            .coerceIn(0, 100)

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                PrefsKeys.ENABLED -> {
                    enabled = prefs.getBoolean(PrefsKeys.ENABLED, true)
                }

                percentKey -> {
                    targetPercent = prefs.getInt(percentKey, PrefsKeys.DEFAULT_PERCENT)
                        .coerceIn(0, 100)
                }
            }
        }

        prefsListener = listener
        runCatching {
            prefs.registerOnSharedPreferenceChangeListener(listener)
        }
    }

    private fun applyVolume(activity: Activity) {
        try {
            val audio = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            if (max <= 0) return

            val target = (max * targetPercent / 100f).roundToInt().coerceIn(0, max)
            val current = audio.getStreamVolume(AudioManager.STREAM_MUSIC)

            if (current != target) {
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
                log(
                    Log.INFO,
                    TAG,
                    "${activity.packageName}: $current -> $target ($targetPercent%)",
                )
            }
        } catch (t: Throwable) {
            log(Log.ERROR, TAG, "failed to change volume", t)
        }
    }

    companion object {
        private const val TAG = "MiuixVolume"
    }
}
