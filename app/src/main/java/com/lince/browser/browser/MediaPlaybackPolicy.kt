package com.lince.browser.browser

internal object MediaPlaybackPolicy {
    fun requiresUserGesture(
        tabId: String,
        selectedTabId: String,
        isActivityResumed: Boolean,
    ): Boolean = tabId != selectedTabId || !isActivityResumed
}
