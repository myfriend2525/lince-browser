package com.lince.browser.data

import com.lince.browser.browser.BrowserTab

internal object TabAutoSortingRules {
    fun orderedTabs(
        tabs: List<BrowserTab>,
        selectedTabId: String,
    ): List<BrowserTab> = tabs.sortedWith(
        compareByDescending<BrowserTab>(BrowserTab::isPinned)
            .thenBy(BrowserTab::lastAccessedAt)
            .thenBy { tab -> tab.id == selectedTabId }
            .thenBy(BrowserTab::id),
    )
}
