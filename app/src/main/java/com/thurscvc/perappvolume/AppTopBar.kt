package com.thurscvc.perappvolume

import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.GlassTopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun AppTopBar(
    title: String,
    scrollBehavior: ScrollBehavior,
    background: Color,
    blurred: Boolean,
    backdrop: Backdrop? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
) {
    if (blurred && backdrop != null) {
        // GlassTopAppBar defaults to a 26dp title inset; the normal bar uses
        // 16dp. Shift the glass content by 10dp so titles do not jump.
        if (navigationIcon != null) {
            GlassTopAppBar(
                title = title,
                backdrop = backdrop,
                contentModifier = Modifier.offset(x = (-10).dp),
                largeTitle = title,
                scrollBehavior = scrollBehavior,
                navigationIcon = navigationIcon,
            )
        } else {
            GlassTopAppBar(
                title = title,
                backdrop = backdrop,
                contentModifier = Modifier.offset(x = (-10).dp),
                largeTitle = title,
                scrollBehavior = scrollBehavior,
            )
        }
        return
    }

    if (navigationIcon != null) {
        TopAppBar(
            title = title,
            largeTitle = title,
            color = background,
            titleColor = MiuixTheme.colorScheme.onBackground,
            largeTitleColor = MiuixTheme.colorScheme.onBackground,
            titlePadding = 16.dp,
            scrollBehavior = scrollBehavior,
            navigationIcon = navigationIcon,
        )
    } else {
        TopAppBar(
            title = title,
            largeTitle = title,
            color = background,
            titleColor = MiuixTheme.colorScheme.onBackground,
            largeTitleColor = MiuixTheme.colorScheme.onBackground,
            titlePadding = 16.dp,
            scrollBehavior = scrollBehavior,
        )
    }
}
