package com.lince.browser.browser.systemwebview

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PrivateDownloadRedirectRulesTest {
    private val start = PrivateDownloadHop(
        url = "https://files.example.com/get/report",
        cookies = "session=private",
        referrer = "https://files.example.com/page",
    )

    @Test
    fun `same host redirect keeps private cookies and resolves relative location`() {
        val next = PrivateDownloadRedirectRules.next(start, "/download/report.pdf", "files.example.com")

        assertEquals("https://files.example.com/download/report.pdf", next?.url)
        assertEquals("session=private", next?.cookies)
    }

    @Test
    fun `cross host redirect drops private cookies`() {
        val next = PrivateDownloadRedirectRules.next(
            start,
            "https://cdn.other.net/report.pdf",
            "files.example.com",
        )

        assertEquals("https://cdn.other.net/report.pdf", next?.url)
        assertNull(next?.cookies)
    }

    @Test
    fun `cookies stay dropped after leaving and returning to the original host`() {
        val away = PrivateDownloadRedirectRules.next(start, "https://cdn.other.net/x", "files.example.com")!!
        val back = PrivateDownloadRedirectRules.next(away, "https://files.example.com/y", "files.example.com")

        assertNull(back?.cookies)
    }

    @Test
    fun `non http and missing locations are rejected`() {
        assertNull(PrivateDownloadRedirectRules.next(start, "file:///sdcard/secret", "files.example.com"))
        assertNull(PrivateDownloadRedirectRules.next(start, "javascript:alert(1)", "files.example.com"))
        assertNull(PrivateDownloadRedirectRules.next(start, null, "files.example.com"))
        assertNull(PrivateDownloadRedirectRules.next(start, "   ", "files.example.com"))
    }

    @Test
    fun `cross origin redirect reduces referrer to its origin`() {
        val next = PrivateDownloadRedirectRules.next(start, "https://cdn.other.net/r", "files.example.com")

        assertEquals("https://files.example.com", next?.referrer)
    }
}
