package com.lince.browser.browser.downloads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CandyDownloadNotifierTest {
    @Test
    fun `progress percent clamps transfer bytes and rejects unknown totals`() {
        assertNull(CandyDownloadNotifier.progressPercent(received = 10L, total = -1L))
        assertNull(CandyDownloadNotifier.progressPercent(received = 10L, total = 0L))
        assertEquals(0, CandyDownloadNotifier.progressPercent(received = -10L, total = 200L))
        assertEquals(50, CandyDownloadNotifier.progressPercent(received = 100L, total = 200L))
        assertEquals(100, CandyDownloadNotifier.progressPercent(received = 300L, total = 200L))
    }

    @Test
    fun `first progress post is always allowed`() {
        assertTrue(DownloadNotificationThrottleRules.shouldPostProgress(null, nowMillis = 5L, percent = 0))
        assertTrue(DownloadNotificationThrottleRules.shouldPostProgress(null, nowMillis = 5L, percent = null))
    }

    @Test
    fun `known total posts only when percent changes after the minimum interval`() {
        val previous = DownloadProgressPost(postedAtMillis = 1_000L, percent = 10)
        assertFalse(DownloadNotificationThrottleRules.shouldPostProgress(previous, 1_499L, percent = 40))
        assertFalse(DownloadNotificationThrottleRules.shouldPostProgress(previous, 5_000L, percent = 10))
        assertTrue(DownloadNotificationThrottleRules.shouldPostProgress(previous, 1_500L, percent = 11))
    }

    @Test
    fun `unknown total posts at most once per second`() {
        val previous = DownloadProgressPost(postedAtMillis = 1_000L, percent = null)
        assertFalse(DownloadNotificationThrottleRules.shouldPostProgress(previous, 1_999L, percent = null))
        assertTrue(DownloadNotificationThrottleRules.shouldPostProgress(previous, 2_000L, percent = null))
    }

    @Test
    fun `clock going backwards does not suppress progress forever`() {
        val previous = DownloadProgressPost(postedAtMillis = 9_000L, percent = 10)
        assertTrue(DownloadNotificationThrottleRules.shouldPostProgress(previous, 1_000L, percent = 10))
    }
}
