package com.thurscvc.perappvolume

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.glass.GlassNavigationBar
import top.yukonga.miuix.kmp.glass.GlassNavigationItem
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val controller = remember {
                ThemeController(ColorSchemeMode.System)
            }

            MiuixTheme(controller = controller) {
                VolumeApp()
            }
        }
    }
}

@Composable
private fun VolumeApp() {
    val context = LocalContext.current
    val uiPrefs = remember { UiPrefs.get(context) }
    val background = MiuixTheme.colorScheme.surface

    var selectedTab by remember { mutableIntStateOf(0) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var addAppOpen by remember { mutableStateOf(false) }
    var glassEnabled by remember {
        mutableStateOf(uiPrefs.getBoolean(UiPrefs.GLASS, true))
    }
    var topBarBlur by remember {
        mutableStateOf(uiPrefs.getBoolean(UiPrefs.TOP_BAR_BLUR, false))
    }

    val menuProgress by animateFloatAsState(
        targetValue = if (addAppOpen) 1f else 0f,
        animationSpec = tween(300),
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = MiuixScrollBehavior(topAppBarState)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshKey++
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(refreshKey) {
        App.refreshScope()
    }

    val backdrop = rememberLayerBackdrop()
    val navigationBottom = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val contentBottom = navigationBottom + 96.dp

    BackHandler(enabled = addAppOpen) {
        addAppOpen = false
    }

    val tabs = listOf("首页", "配置", "设置")
    val icons = listOf(MiuixIcons.Home, MiuixIcons.Tune, MiuixIcons.Settings)
    val title = when (selectedTab) {
        0 -> "音量调节"
        1 -> "音量配置"
        else -> "设置"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    scaleX = 1f - 0.02f * menuProgress
                    scaleY = 1f - 0.02f * menuProgress
                    translationX = -size.width * 0.06f * menuProgress
                    alpha = 1f - 0.28f * menuProgress
                }
                .layerBackdrop(backdrop)
                .background(background),
        ) {
            Scaffold(
                topBar = {
                    AppTopBar(
                        title = title,
                        scrollBehavior = scrollBehavior,
                        background = background,
                        blurred = topBarBlur,
                        backdrop = backdrop,
                    )
                },
                containerColor = Color.Transparent,
            ) { innerPadding ->
                val contentModifier = Modifier.nestedScroll(
                    scrollBehavior.nestedScrollConnection,
                )

                when (selectedTab) {
                    0 -> HomePage(
                        modifier = contentModifier,
                        topPadding = innerPadding.calculateTopPadding(),
                        bottomSpace = contentBottom + 12.dp,
                    )

                    1 -> ConfigPage(
                        modifier = contentModifier,
                        topPadding = innerPadding.calculateTopPadding(),
                        bottomSpace = contentBottom + 12.dp,
                        refreshKey = refreshKey,
                        onAddApp = { addAppOpen = true },
                    )

                    else -> SettingsPage(
                        modifier = contentModifier,
                        topPadding = innerPadding.calculateTopPadding(),
                        bottomSpace = contentBottom + 12.dp,
                        glassEnabled = glassEnabled,
                        onGlassChange = { glassEnabled = it },
                        topBarBlur = topBarBlur,
                        onTopBarBlurChange = { topBarBlur = it },
                    )
                }
            }
        }

        if (glassEnabled) {
            GlassNavigationBar(
                items = tabs.mapIndexed { index, label ->
                    GlassNavigationItem(icons[index], label)
                },
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it },
                backdrop = backdrop,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            )
        } else {
            NavigationBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                showDivider = true,
            ) {
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = icons[index],
                        label = label,
                    )
                }
            }
        }

        if (menuProgress > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.24f * menuProgress)),
            )
        }

        AnimatedVisibility(
            visible = addAppOpen,
            enter = slideInHorizontally(
                animationSpec = tween(320),
                initialOffsetX = { it },
            ) + fadeIn(animationSpec = tween(180)),
            exit = slideOutHorizontally(
                animationSpec = tween(260),
                targetOffsetX = { it },
            ) + fadeOut(animationSpec = tween(140)),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        RoundedCornerShape(
                            topStart = 24.dp,
                            bottomStart = 24.dp,
                        ),
                    )
                    .background(background),
            ) {
                AddAppPage(
                    onBack = { addAppOpen = false },
                    topBarBlur = topBarBlur,
                )
            }
        }
    }
}
