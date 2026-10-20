package com.lince.browser

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.lince.browser.data.BrowserAppearanceMode

internal fun AppCompatActivity.applyAppearanceNightMode(appearanceMode: BrowserAppearanceMode) {
    val nightMode = when (appearanceMode) {
        BrowserAppearanceMode.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        BrowserAppearanceMode.Light -> AppCompatDelegate.MODE_NIGHT_NO
        BrowserAppearanceMode.Dark,
        BrowserAppearanceMode.Amoled,
        -> AppCompatDelegate.MODE_NIGHT_YES
    }
    if (delegate.localNightMode != nightMode) delegate.localNightMode = nightMode
}
