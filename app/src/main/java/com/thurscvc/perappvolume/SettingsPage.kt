package com.thurscvc.perappvolume

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun SettingsPage(
    modifier: Modifier,
    topPadding: Dp,
    bottomSpace: Dp,
    glassEnabled: Boolean,
    onGlassChange: (Boolean) -> Unit,
    topBarBlur: Boolean,
    onTopBarBlurChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val uiPrefs = remember { UiPrefs.get(context) }
    val prefs = App.prefs
    var section by remember { mutableIntStateOf(0) }

    var hideIcon by remember {
        mutableStateOf(uiPrefs.getBoolean(UiPrefs.HIDE_ICON, false))
    }
    var restoreVolume by remember(prefs) {
        mutableStateOf(prefs?.getBoolean(PrefsKeys.RESTORE_VOLUME, false) ?: false)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 12.dp,
            top = topPadding + 12.dp,
            end = 12.dp,
            bottom = bottomSpace,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            TabRow(
                tabs = listOf("模块设置", "主题设置"),
                selectedTabIndex = section,
                onTabSelected = { section = it },
                height = 48.dp,
            )
        }

        if (section == 0) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    SwitchPreference(
                        title = "退出应用后恢复原音量",
                        checked = restoreVolume,
                        onCheckedChange = { value ->
                            restoreVolume = value
                            prefs?.edit()
                                ?.putBoolean(PrefsKeys.RESTORE_VOLUME, value)
                                ?.apply()
                        },
                    )
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "使用步骤：在 LSPosed 中授权应用，再到音量配置里调节。",
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        } else {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    SwitchPreference(
                        title = "玻璃底栏",
                        checked = glassEnabled,
                        onCheckedChange = { value ->
                            onGlassChange(value)
                            uiPrefs.edit()
                                .putBoolean(UiPrefs.GLASS, value)
                                .apply()
                        },
                    )
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    SwitchPreference(
                        title = "顶栏模糊",
                        checked = topBarBlur,
                        onCheckedChange = { value ->
                            onTopBarBlurChange(value)
                            uiPrefs.edit()
                                .putBoolean(UiPrefs.TOP_BAR_BLUR, value)
                                .apply()
                        },
                    )
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    SwitchPreference(
                        title = "隐藏桌面图标",
                        checked = hideIcon,
                        onCheckedChange = { value ->
                            hideIcon = value
                            uiPrefs.edit()
                                .putBoolean(UiPrefs.HIDE_ICON, value)
                                .apply()
                            App.setLauncherVisible(context, !value)
                        },
                    )
                }
            }
        }
    }
}
