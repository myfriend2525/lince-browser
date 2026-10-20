package com.lince.browser.data

import com.lince.browser.browser.BLANK_URL
import com.lince.browser.browser.BrowserTab

internal object TabDuplicateRules {
    fun tabIdsToClose(tabs: List<BrowserTab>, selectedTabId: String): List<String> {
        val duplicateGroups = tabs.asSequence()
            .filter { it.url != BLANK_URL }
            .mapNotNull { tab ->
                CanonicalWebUrl.key(tab.url)?.let { url ->
                    Triple(tab.profileId, tab.isIncognito, url) to tab
                }
            }
            .groupBy(keySelector = { it.first }, valueTransform = { it.second })
            .values
            .filter { it.size > 1 }

        val closeIds = duplicateGroups.flatMap { group ->
            val protectedTabs = group.filter { it.isPinned || it.id == selectedTabId }
            val keepId = if (protectedTabs.isEmpty()) {
                group.maxByOrNull(BrowserTab::lastAccessedAt)?.id
            } else {
                null
            }
            group.filter { tab ->
                !tab.isPinned && tab.id != selectedTabId && tab.id != keepId
            }.map(BrowserTab::id)
        }.toSet()
        return tabs.map(BrowserTab::id).filter(closeIds::contains)
    }
}
