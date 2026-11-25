package com.thurscvc.perappvolume

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.os.Process
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
        log(
            Log.INFO,
            TAG,
            "loaded in ${param.processName} uid=${Process.myUid()} user=${Process.myUserHandle()}",
        )
    }

    override fun onPackageReady(param: PackageReadyParam) {
        log(
            Log.INFO,
            TAG,
            "ready ${param.packageName} uid=${Process.myUid()} user=${Process.myUserHandle()}",
        )

        // Install hooks first: remote preferences may be unavailable in a
        // secondary user before the module UI has ever been opened there.
        runCatching { installHooks() }
            .onFailure {
                hookInstalled.set(false)
                log(Log.ERROR, TAG, "install hooks failed", it)
            }

        loadPrefs(param.packageName)
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
        runCatching { installHooks() }
            .onFailure {
                hookInstalled.set(false)
                log(Log.ERROR, TAG, "install hooks after reload failed", it)
            }
        loadPrefs(packageName)
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

        log(Log.INFO, TAG, "hooks installed in ${Process.myUserHandle()}")
    }

    private fun loadPrefs(packageName: String) {
        val percentKey = PrefsKeys.volumeKey(packageName)

        runCatching {
            val prefs = getRemotePreferences(PrefsKeys.GROUP)
            remotePrefs = prefs
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
            prefs.registerOnSharedPreferenceChangeListener(listener)

            log(Log.INFO, TAG, "prefs $packageName = $targetPercent% enabled=$enabled")
        }.onFailure {
            remotePrefs = null
            enabled = true
            targetPercent = PrefsKeys.DEFAULT_PERCENT
            log(Log.ERROR, TAG, "remote prefs unavailable for $packageName", it)
        }
    }

    private fun applyVolume(activity: Activity) {
        try {
            // RemotePreferences listeners are not always delivered between
            // secondary-user processes. Re-read the current value on every
            // resume so slider changes take effect without restarting the app.
            val key = PrefsKeys.volumeKey(activity.packageName)
            remotePrefs?.let { prefs ->
                targetPercent = prefs.getInt(key, PrefsKeys.DEFAULT_PERCENT).coerceIn(0, 100)
            }

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
        private const val TAG = "PerAppVolume"
    }
}
