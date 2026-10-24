package com.lince.browser.browser

/**
 * Keeps the keyboard opened by Candy's chrome (address editor, find in page) away from page
 * layout. Releasing chrome ownership usually happens a few frames before Android reports the
 * hidden IME, so the still-visible chrome keyboard must not be handed to the page as its own.
 */
internal object BrowserChromeImeRules {
    const val CLOSE_TIMEOUT_MILLIS = 1_000L

    fun startsClosingAfterRelease(
        browserChromeOwnsIme: Boolean,
        isImeVisible: Boolean,
    ): Boolean = !browserChromeOwnsIme && isImeVisible

    fun finishesClosing(
        isClosing: Boolean,
        isImeVisible: Boolean,
    ): Boolean = isClosing && !isImeVisible

    fun masksPageIme(
        browserChromeOwnsIme: Boolean,
        isClosing: Boolean,
    ): Boolean = browserChromeOwnsIme || isClosing
}
