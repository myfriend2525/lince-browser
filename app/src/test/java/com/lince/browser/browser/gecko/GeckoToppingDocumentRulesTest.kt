package com.lince.browser.browser.gecko

import com.lince.browser.browser.engine.BrowserEngineContentKind
import com.lince.browser.browser.userscript.UserScript
import com.lince.browser.browser.userscript.UserScriptParseResult
import com.lince.browser.browser.userscript.UserScriptParser
import com.lince.browser.shared.topping.ToppingFrameScope
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeckoToppingDocumentRulesTest {
    @Test
    fun `session policy excludes private and link peek content`() {
        assertTrue(GeckoToppingSessionRules.allows(false, BrowserEngineContentKind.RegularTab))
        assertTrue(GeckoToppingSessionRules.allows(false, BrowserEngineContentKind.ExternalPreview))
        assertFalse(GeckoToppingSessionRules.allows(false, BrowserEngineContentKind.LinkPeek))
        assertFalse(GeckoToppingSessionRules.allows(true, BrowserEngineContentKind.RegularTab))
    }

    @Test
    fun `top scope rejects matching child frame`() {
        assertFalse(
            GeckoToppingDocumentRules.isEligible(
                script = script(ToppingFrameScope.Top),
                pageUrl = "https://example.com/frame",
                topUrl = "https://example.com/page",
                frameId = 1,
            ),
        )
    }

    @Test
    fun `same origin scope accepts default ports and rejects cross origin frames`() {
        val script = script(ToppingFrameScope.SameOrigin)

        assertTrue(
            GeckoToppingDocumentRules.isEligible(
                script = script,
                pageUrl = "https://example.com:443/frame",
                topUrl = "https://example.com/page",
                frameId = 2,
            ),
        )
        assertFalse(
            GeckoToppingDocumentRules.isEligible(
                script = script,
                pageUrl = "https://cdn.example/frame",
                topUrl = "https://example.com/page",
                frameId = 2,
            ),
        )
    }

    @Test
    fun `all matching scope still enforces script url match`() {
        val script = script(ToppingFrameScope.AllMatching)

        assertTrue(
            GeckoToppingDocumentRules.isEligible(
                script = script,
                pageUrl = "https://example.com/frame",
                topUrl = "https://other.example/page",
                frameId = 3,
            ),
        )
        assertFalse(
            GeckoToppingDocumentRules.isEligible(
                script = script,
                pageUrl = "https://blocked.example/frame",
                topUrl = "https://other.example/page",
                frameId = 3,
            ),
        )
    }

    private fun script(scope: ToppingFrameScope): UserScript {
        val source = """
            // ==UserScript==
            // @name Frames
            // @match https://example.com/*
            // @candy-frames ${scope.wireValue}
            // @grant none
            // ==/UserScript==
        """.trimIndent()
        return (UserScriptParser.parse("frames", source) as UserScriptParseResult.Accepted).script
    }
}
