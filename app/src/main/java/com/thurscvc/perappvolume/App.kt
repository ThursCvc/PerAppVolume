package com.thurscvc.perappvolume

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

data class FrameworkInfo(
    val name: String,
    val version: String,
    val versionCode: Long,
    val api: Int,
)

class App : Application(), XposedServiceHelper.OnServiceListener {

    override fun onCreate() {
        super.onCreate()

        val hidden = UiPrefs.get(this).getBoolean(UiPrefs.HIDE_ICON, false)
        setLauncherVisible(this, !hidden)

        XposedServiceHelper.registerListener(this)
    }

    override fun onServiceBind(bound: XposedService) {
        val info = runCatching {
            FrameworkInfo(
                name = bound.frameworkName,
                version = bound.frameworkVersion,
                versionCode = bound.frameworkVersionCode,
                api = bound.apiVersion,
            )
        }.getOrNull()

        val remote = runCatching {
            bound.getRemotePreferences(PrefsKeys.GROUP)
        }.getOrNull()

        Handler(Looper.getMainLooper()).post {
            prefs = remote
            framework = info
            service = bound
            refreshScope()
        }
    }

    override fun onServiceDied(bound: XposedService) {
        Handler(Looper.getMainLooper()).post {
            prefs = null
            framework = null
            service = null
            scope = emptyList()
        }
    }

    companion object {
        var prefs by mutableStateOf<SharedPreferences?>(null)
            private set

        var framework by mutableStateOf<FrameworkInfo?>(null)
            private set

        var scope by mutableStateOf<List<String>>(emptyList())
            private set

        private var service by mutableStateOf<XposedService?>(null)

        fun refreshScope() {
            val current = service
            scope = if (current == null) {
                emptyList()
            } else {
                runCatching { current.scope }
                    .getOrDefault(emptyList())
                    .filter { it.isNotBlank() && it != "system" }
                    .distinct()
            }
        }

        fun setLauncherVisible(context: Context, visible: Boolean) {
            val component = ComponentName(context, "${context.packageName}.Launcher")
            val state = if (visible) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }

            runCatching {
                context.packageManager.setComponentEnabledSetting(
                    component,
                    state,
                    PackageManager.DONT_KILL_APP,
                )
            }
        }
    }
}
