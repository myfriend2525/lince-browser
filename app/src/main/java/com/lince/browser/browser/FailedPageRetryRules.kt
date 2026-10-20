package com.lince.browser.browser

import com.lince.browser.browser.integration.BrowserUriPolicy
import com.lince.browser.shared.browser.BrowserEngineCommand
import com.lince.browser.shared.browser.BrowserEngineCommands

internal object FailedPageRetryRules {
    fun commandFor(
        tab: BrowserTab,
    ): BrowserEngineCommand? {
        if (tab.isLoading) return null
        val retryUrl = BrowserUriPolicy.normalizeHttpUrl(tab.url) ?: return null
        if (tab.error == null && tab.httpStatusCode == null) return null
        return BrowserEngineCommands.retryFailedPage(retryUrl)
    }
}
