package com.lince.browser.shared

import com.lince.browser.browser.PageTranslationProvider
import com.lince.browser.browser.PageTranslationRules
import com.lince.browser.shared.browser.AddressResolution
import com.lince.browser.shared.browser.BrowserChromeGestureRules
import com.lince.browser.shared.browser.BrowserUrlRules
import com.lince.browser.shared.topping.ToppingInjectionPlan
import com.lince.browser.shared.topping.ToppingParseResult
import com.lince.browser.shared.topping.ToppingRules

class CandySharedFacade {
    fun resolveAddress(input: String): AddressResolution = BrowserUrlRules.resolve(input)

    fun defaultPageTranslationUrl(
        sourceUrl: String?,
        targetLanguage: String,
    ): String? = pageTranslationUrl(
        provider = PageTranslationProvider.fromStableId(null),
        sourceUrl = sourceUrl,
        targetLanguage = targetLanguage,
    )

    fun pageTranslationUrl(
        provider: PageTranslationProvider,
        sourceUrl: String?,
        targetLanguage: String,
    ): String? = PageTranslationRules.buildTranslationUrl(
        provider = provider,
        sourceUrl = sourceUrl,
        targetLanguage = targetLanguage,
    )

    fun addressBarOverviewProgress(dragY: Double): Double =
        BrowserChromeGestureRules.overviewProgress(dragY)

    fun shouldOpenTabOverview(
        dragX: Double,
        dragY: Double,
        touchSlop: Double,
        isAddressEditing: Boolean,
    ): Boolean = BrowserChromeGestureRules.shouldOpenOverview(
        dragX = dragX,
        dragY = dragY,
        touchSlop = touchSlop,
        isAddressEditing = isAddressEditing,
    )

    fun bundledDemoTopping(): ToppingInjectionPlan {
        val source = """
            // ==UserScript==
            // @name Candy iOS bridge marker
            // @match https://*/*
            // @run-at document-start
            // ==/UserScript==
            document.documentElement.dataset.candyToppingRuntime = "wkuserscript";
        """.trimIndent()
        val script = (ToppingRules.parse("candy-ios-marker", source) as ToppingParseResult.Accepted).script
        return ToppingRules.injectionPlan(script)
    }
}
