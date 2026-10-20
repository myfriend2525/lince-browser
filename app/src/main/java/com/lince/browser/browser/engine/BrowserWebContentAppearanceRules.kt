package com.lince.browser.browser.engine

import com.lince.browser.data.AppearanceSettings

internal object BrowserWebContentAppearanceRules {
    /** Resolve System before forwarding to engines with a separate native night-mode cache. */
    fun colorScheme(
        settings: AppearanceSettings,
        systemDark: Boolean,
    ): BrowserWebContentColorScheme = if (settings.usesDarkColors(systemDark)) {
        BrowserWebContentColorScheme.Dark
    } else {
        BrowserWebContentColorScheme.Light
    }
}
