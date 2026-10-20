package com.lince.browser.browser

import android.os.Process
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserMediaControllerAuthRulesTest {
    @Test
    fun `own package system and media notification controllers are accepted`() {
        assertTrue(
            BrowserMediaControllerAuthRules.accepts(
                ownPackageName = "com.lince.browser",
                controllerPackageName = "com.lince.browser",
                controllerUid = 12_345,
                isTrustedSystemController = false,
                isMediaNotificationController = false,
            ),
        )
        assertTrue(
            BrowserMediaControllerAuthRules.accepts(
                ownPackageName = "com.lince.browser",
                controllerPackageName = "android",
                controllerUid = Process.SYSTEM_UID,
                isTrustedSystemController = false,
                isMediaNotificationController = false,
            ),
        )
        assertTrue(
            BrowserMediaControllerAuthRules.accepts(
                ownPackageName = "com.lince.browser",
                controllerPackageName = "notification-controller",
                controllerUid = 12_345,
                isTrustedSystemController = false,
                isMediaNotificationController = true,
            ),
        )
    }

    @Test
    fun `untrusted third party controller is rejected`() {
        assertFalse(
            BrowserMediaControllerAuthRules.accepts(
                ownPackageName = "com.lince.browser",
                controllerPackageName = "com.example.remote",
                controllerUid = 12_345,
                isTrustedSystemController = false,
                isMediaNotificationController = false,
            ),
        )
    }
}
