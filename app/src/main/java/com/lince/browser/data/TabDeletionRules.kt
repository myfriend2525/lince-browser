package com.lince.browser.data

import com.lince.browser.browser.BrowserTab

internal object TabDeletionRules {
    fun canDelete(tab: BrowserTab): Boolean = !tab.isPinned

    fun deletableTabIds(tabs: List<BrowserTab>): Set<String> = tabs
        .asSequence()
        .filter(::canDelete)
        .mapTo(linkedSetOf(), BrowserTab::id)
}
