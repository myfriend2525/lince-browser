package com.lince.browser.shared

import com.lince.browser.browser.PageTranslationProvider
import kotlin.test.Test
import kotlin.test.assertEquals

class CandySharedFacadeTranslationTest {
    @Test
    fun selectedProviderCrossesPlatformFacade() {
        assertEquals(
            "https://translate.google.com/translate?sl=auto&tl=de&u=" +
                "https%3A%2F%2Fexample.com%2Farticle",
            CandySharedFacade().pageTranslationUrl(
                provider = PageTranslationProvider.Google,
                sourceUrl = "https://example.com/article",
                targetLanguage = "de",
            ),
        )
    }
}
