package com.lince.browser.browser.gecko

import com.lince.browser.browser.BLANK_URL
import com.lince.browser.browser.BrowserTab
import com.lince.browser.browser.CandyTrail
import com.lince.browser.browser.CandyTrailNode
import com.lince.browser.browser.integration.BrowserUriPolicy

internal object GeckoBootstrapRecoveryRules {
    fun hasPoisonedAddress(url: String): Boolean =
        CandyPrivacyHostContract.isBootstrapDocumentUrl(url)

    fun resetPoisonedTab(tab: BrowserTab): BrowserTab =
        if (hasPoisonedAddress(tab.url)) {
            tab.copy(url = BLANK_URL, title = "")
        } else {
            tab
        }

    fun recoverUrl(trail: CandyTrail): String? {
        trail.nodes.firstOrNull { node -> node.id == trail.currentNodeId }
            ?.url
            ?.let(BrowserUriPolicy::normalizeHttpUrl)
            ?.let { return it }
        return trail.nodes
            .sortedWith(compareByDescending<CandyTrailNode> {
                it.visitedAt
            }.thenBy { it.id })
            .firstNotNullOfOrNull { node -> BrowserUriPolicy.normalizeHttpUrl(node.url) }
    }
}
