package com.thurscvc.perappvolume

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.libxposed.service.XposedService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.theme.MiuixTheme

private data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap?,
)

@Composable
internal fun AddAppPage(
    onBack: () -> Unit,
    topBarBlur: Boolean,
) {
    val context = LocalContext.current
    val background = MiuixTheme.colorScheme.surface
    val navigationBottom = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = MiuixScrollBehavior(topAppBarState)
    val backdrop = rememberLayerBackdrop()

    var query by remember { mutableStateOf("") }
    var pending by remember { mutableStateOf<Set<String>>(emptySet()) }
    var installedApps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val scope = App.scope

    LaunchedEffect(Unit) {
        installedApps = withContext(Dispatchers.IO) {
            loadInstalledApps(context)
        }
        loading = false
    }

    val filteredApps = remember(query, installedApps, scope, pending) {
        val filtered = if (query.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
            }
        }

        filtered.sortedWith(
            compareByDescending<InstalledApp> {
                scope.contains(it.packageName) || it.packageName in pending
            }.thenBy { it.label.lowercase() },
        )
    }

    fun requestScope(app: InstalledApp) {
        if (App.scope.contains(app.packageName) || app.packageName in pending) return

        pending = pending + app.packageName
        App.requestScope(
            app.packageName,
            object : XposedService.OnScopeEventListener {
                override fun onScopeRequestApproved(packages: List<String>) {
                    mainHandler.post {
                        pending = pending - app.packageName
                        App.refreshScope()
                    }
                }

                override fun onScopeRequestFailed(message: String) {
                    mainHandler.post {
                        pending = pending - app.packageName
                        Toast.makeText(
                            context,
                            message.ifBlank { "作用域请求失败" },
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .layerBackdrop(backdrop),
    ) {
    Scaffold(
        containerColor = background,
        topBar = {
            AppTopBar(
                title = "添加应用",
                scrollBehavior = scrollBehavior,
                background = background,
                blurred = topBarBlur,
                backdrop = backdrop,
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                start = 12.dp,
                top = innerPadding.calculateTopPadding() + 12.dp,
                end = 12.dp,
                bottom = navigationBottom + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SearchField(
                    query = query,
                    onQueryChange = { query = it },
                )
            }

            if (loading) {
                item {
                    Text(
                        text = "正在加载应用…",
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                    )
                }
            } else if (filteredApps.isEmpty()) {
                item {
                    Text(
                        text = "没有找到应用",
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                    )
                }
            }

            items(filteredApps, key = { it.packageName }) { app ->
                val checked = scope.contains(app.packageName) ||
                    app.packageName in pending

                InstalledAppCard(
                    app = app,
                    checked = checked,
                    onToggle = {
                        when {
                            scope.contains(app.packageName) -> {
                                App.removeScope(app.packageName)
                            }

                            app.packageName in pending -> {
                                pending = pending - app.packageName
                            }

                            else -> requestScope(app)
                        }
                    },
                )
            }
        }
    }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = MiuixIcons.Search,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = MiuixTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = "搜索应用",
                                fontSize = 16.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
    }
}

@Composable
private fun InstalledAppCard(
    app: InstalledApp,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onToggle),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (app.icon != null) {
                Image(
                    bitmap = app.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(11.dp)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(11.dp)),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    fontSize = 17.sp,
                    fontWeight = FontWeight(550),
                    maxLines = 1,
                )
                Text(
                    text = app.packageName,
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                )
            }

            Checkbox(
                state = if (checked) ToggleableState.On else ToggleableState.Off,
                onClick = onToggle,
            )
        }
    }
}

private fun loadInstalledApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    val iconSize = (44 * context.resources.displayMetrics.density).toInt()
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

    return pm.queryIntentActivities(intent, 0)
        .asSequence()
        .mapNotNull { it.activityInfo?.applicationInfo }
        .filter { it.packageName != context.packageName }
        .distinctBy { it.packageName }
        .map { info ->
            val label = runCatching {
                pm.getApplicationLabel(info).toString()
            }.getOrDefault(info.packageName)
            val icon = runCatching {
                pm.getApplicationIcon(info).toImageBitmap(iconSize)
            }.getOrNull()

            InstalledApp(info.packageName, label, icon)
        }
        .sortedBy { it.label.lowercase() }
        .toList()
}
