package com.lince.browser.browser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserChromeImeRulesTest {
    @Test
    fun `releasing chrome ownership with the keyboard still visible keeps it from the page`() {
        val closing = BrowserChromeImeRules.startsClosingAfterRelease(
            browserChromeOwnsIme = false,
            isImeVisible = true,
        )

        assertTrue(closing)
        assertTrue(
            BrowserChromeImeRules.masksPageIme(
                browserChromeOwnsIme = false,
                isClosing = closing,
            ),
        )
    }

    @Test
    fun `releasing chrome ownership after the keyboard hid does not wait`() {
        assertFalse(
            BrowserChromeImeRules.startsClosingAfterRelease(
                browserChromeOwnsIme = false,
                isImeVisible = false,
            ),
        )
        assertFalse(
            BrowserChromeImeRules.startsClosingAfterRelease(
                browserChromeOwnsIme = true,
                isImeVisible = true,
            ),
        )
    }

    @Test
    fun `hidden keyboard ends the chrome close`() {
        assertTrue(BrowserChromeImeRules.finishesClosing(isClosing = true, isImeVisible = false))
        assertFalse(BrowserChromeImeRules.finishesClosing(isClosing = true, isImeVisible = true))
        assertFalse(BrowserChromeImeRules.finishesClosing(isClosing = false, isImeVisible = false))
    }

    @Test
    fun `page keyboard is unmasked once chrome neither owns nor closes it`() {
        assertFalse(
            BrowserChromeImeRules.masksPageIme(browserChromeOwnsIme = false, isClosing = false),
        )
        assertTrue(
            BrowserChromeImeRules.masksPageIme(browserChromeOwnsIme = true, isClosing = false),
        )
    }
}
