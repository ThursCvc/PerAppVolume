package com.thurscvc.perappvolume

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun HomePage(
    modifier: Modifier,
    topPadding: Dp,
    bottomSpace: Dp,
) {
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
            ActivationCard(App.framework)
        }
        item {
            InfoCard()
        }
    }
}

@Composable
private fun ActivationCard(info: FrameworkInfo?) {
    val active = info != null
    val dark = MiuixTheme.colorScheme.background.luminance() < 0.5f

    val container = when {
        active && dark -> Color(0xFF17351F)
        active -> Color(0xFFDDFAE4)
        dark -> Color(0xFF310808)
        else -> Color(0xFFF8E2E2)
    }
    val textColor = MiuixTheme.colorScheme.onSurface
    val iconColor = if (active) Color(0xFF36D267) else Color(0xFFF72727)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(
            color = container,
            contentColor = textColor,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(136.dp),
        ) {
            Image(
                painter = painterResource(
                    if (active) R.drawable.ic_activated else R.drawable.ic_deactivated,
                ),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 8.dp)
                    .size(76.dp),
                colorFilter = ColorFilter.tint(iconColor),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Text(
                    text = if (active) "已激活" else "未激活",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(2.dp))

                if (info != null) {
                    Text(
                        text = "${info.version} (${info.versionCode})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                    )
                    Text(
                        text = "API ${info.api}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                    )
                } else {
                    Text(
                        text = "LSPosed 未连接",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard() {
    val context = LocalContext.current
    val version = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "-"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            InfoText("设备型号", Build.MODEL.ifBlank { "-" })
            InfoText(
                "系统版本",
                "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            )
            InfoText("系统版本号", Build.VERSION.INCREMENTAL.ifBlank { "-" })
            InfoText("架构", Build.SUPPORTED_ABIS.firstOrNull() ?: "-")
            InfoText("模块版本", version)
            InfoText("作用域应用", App.scope.size.toString())
            InfoText("包名", context.packageName, bottomPadding = 0.dp)
        }
    }
}

@Composable
private fun InfoText(
    title: String,
    content: String,
    bottomPadding: Dp = 24.dp,
) {
    Text(
        text = title,
        fontSize = 17.sp,
        fontWeight = FontWeight.Medium,
        color = MiuixTheme.colorScheme.onSurface,
    )
    Text(
        text = content,
        fontSize = 14.sp,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        modifier = Modifier.padding(top = 2.dp, bottom = bottomPadding),
    )
}
