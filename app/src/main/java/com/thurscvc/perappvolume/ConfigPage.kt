package com.thurscvc.perappvolume

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.AddCircle
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.roundToInt

private data class ScopedApp(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap?,
)

@Composable
internal fun ConfigPage(
    modifier: Modifier,
    topPadding: Dp,
    bottomSpace: Dp,
    onAddApp: () -> Unit,
) {
    val context = LocalContext.current
    val scope = App.scope
    val density = LocalDensity.current
    val iconSizePx = with(density) { 40.dp.roundToPx() }

    val apps = remember(scope, iconSizePx) {
        val packageManager = context.packageManager
        scope.map { packageName ->
            val applicationInfo = runCatching {
                packageManager.getApplicationInfo(packageName, 0)
            }.getOrNull()

            val label = applicationInfo?.let {
                runCatching {
                    packageManager.getApplicationLabel(it).toString()
                }.getOrNull()
            } ?: packageName

            val icon = runCatching {
                packageManager.getApplicationIcon(packageName).toImageBitmap(iconSizePx)
            }.getOrNull()

            ScopedApp(packageName, label, icon)
        }.sortedBy { it.label }
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onAddApp() },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = MiuixIcons.AddCircle,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "添加应用",
                            fontSize = 17.sp,
                            fontWeight = FontWeight(550),
                        )
                        Text(
                            text = "从已安装应用中选择",
                            fontSize = 12.sp,
                            fontWeight = FontWeight(550),
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                    Icon(
                        imageVector = MiuixIcons.ChevronForward,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }

        if (apps.isEmpty()) {
            item {
                Text(
                    text = "暂无应用",
                    fontSize = 14.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }

        items(apps, key = { it.packageName }) { app ->
            AppVolumeCard(app)
        }
    }
}

@Composable
private fun AppVolumeCard(app: ScopedApp) {
    val prefs = App.prefs
    val key = PrefsKeys.volumeKey(app.packageName)
    val enabledKey = PrefsKeys.appEnabledKey(app.packageName)

    var percent by remember(app.packageName, prefs) {
        mutableIntStateOf(
            prefs?.getInt(key, PrefsKeys.DEFAULT_PERCENT)
                ?.coerceIn(0, 100)
                ?: PrefsKeys.DEFAULT_PERCENT,
        )
    }
    var appEnabled by remember(app.packageName, prefs) {
        mutableStateOf(prefs?.getBoolean(enabledKey, true) ?: true)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(
            start = 16.dp,
            top = 20.dp,
            end = 16.dp,
            bottom = 22.dp,
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (app.icon != null) {
                    Image(
                        bitmap = app.icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MiuixTheme.colorScheme.surfaceContainerHigh),
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.label,
                        fontSize = 17.sp,
                        fontWeight = FontWeight(550),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = app.packageName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight(550),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    text = "$percent%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = appEnabled,
                    onCheckedChange = { value ->
                        appEnabled = value
                        prefs?.edit()
                            ?.putBoolean(enabledKey, value)
                            ?.apply()
                    },
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Slider(
                value = percent.toFloat(),
                onValueChange = {
                    val value = it.roundToInt().coerceIn(0, 100)
                    percent = value

                    // Persist during the drag as well. If the slider's
                    // onValueChangeFinished callback is not delivered, the
                    // hook would otherwise keep using the old value.
                    val editor = prefs?.edit()
                    if (editor != null) {
                        editor.putInt(key, value)
                        editor.apply()
                    }
                },
                onValueChangeFinished = {
                    val editor = prefs?.edit()
                    if (editor != null) {
                        editor.putInt(key, percent)
                        editor.commit()
                    }
                },
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}


