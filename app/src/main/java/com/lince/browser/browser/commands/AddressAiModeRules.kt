package com.lince.browser.browser.commands

import com.lince.browser.browser.AddressResolver
import com.lince.browser.browser.SearchEngine
import com.lince.browser.browser.SearchMode

object AddressAiModeRules {
    fun isToggleVisible(
        input: String,
        searchEngine: SearchEngine,
        settingEnabled: Boolean,
        allowGeckoInternalPages: Boolean = false,
    ): Boolean = settingEnabled &&
        searchEngine.supportsAiSearch &&
        !CommandMatcher.isExplicitCommandQuery(input) &&
        AddressResolver.isSearchQuery(input, allowGeckoInternalPages)

    fun searchMode(toggleVisible: Boolean, toggleSelected: Boolean): SearchMode =
        if (toggleVisible && toggleSelected) SearchMode.Ai else SearchMode.Web
}
