package com.lince.browser.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import com.lince.browser.browser.BrowserController

@Composable
internal fun BrowserImeOwnershipEffect(
    controller: BrowserController,
    ownsIme: Boolean,
) {
    SideEffect {
        controller.setBrowserChromeOwnsIme(ownsIme)
    }
    DisposableEffect(controller) {
        onDispose { controller.setBrowserChromeOwnsIme(false) }
    }
}
