package com.lince.browser.browser

import com.lince.browser.browser.integration.BrowserUriPolicy
import com.lince.browser.shared.browser.BrowserEngineEventType

internal object BrowserInitialNavigationRules {
    fun shouldIgnoreInitialBlank(
        currentUrl: String?,
        pendingUrl: String?,
        eventUrl: String?,
        eventType: BrowserEngineEventType,
    ): Boolean = eventUrl == BLANK_URL &&
        BrowserUriPolicy.normalizeHttpUrl(currentUrl) != null &&
        BrowserUriPolicy.normalizeHttpUrl(pendingUrl) != null &&
        (eventType == BrowserEngineEventType.NavigationStarted ||
            eventType == BrowserEngineEventType.NavigationCommitted ||
            eventType == BrowserEngineEventType.StateChanged)
}
