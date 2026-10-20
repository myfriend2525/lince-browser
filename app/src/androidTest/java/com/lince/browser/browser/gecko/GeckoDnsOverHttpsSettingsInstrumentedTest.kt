package com.lince.browser.browser.gecko

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lince.browser.browser.DnsOverHttpsProvider
import com.lince.browser.browser.DnsOverHttpsRules
import com.lince.browser.browser.DnsOverHttpsSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.geckoview.ContentBlocking
import org.mozilla.geckoview.GeckoRuntimeSettings

@RunWith(AndroidJUnit4::class)
class GeckoDnsOverHttpsSettingsInstrumentedTest {
    @Test
    fun factoryAppliesStrictResolverBeforeRuntimeCreation() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val settings = GeckoRuntimeSettingsFactory.create(
                contentBlocking = ContentBlocking.Settings.Builder().build(),
                dnsOverHttpsSettings = DnsOverHttpsSettings(DnsOverHttpsProvider.Cloudflare),
            )

            assertEquals(
                GeckoRuntimeSettings.TRR_MODE_ONLY,
                settings.getTrustedRecusiveResolverMode(),
            )
            assertEquals(
                "https://cloudflare-dns.com/dns-query",
                settings.trustedRecursiveResolverUri,
            )
            assertFalse(settings.dohAutoselectEnabled)
        }
    }

    @Test
    fun disablingStrictResolverRestoresNativeDnsWithoutLeavingEndpoint() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val settings = GeckoRuntimeSettingsFactory.create(
                contentBlocking = ContentBlocking.Settings.Builder().build(),
                dnsOverHttpsSettings = DnsOverHttpsSettings(DnsOverHttpsProvider.Google),
            )

            settings.applyDnsOverHttpsSettings(DnsOverHttpsRules.Default)

            assertEquals(
                GeckoRuntimeSettings.TRR_MODE_DISABLED,
                settings.getTrustedRecusiveResolverMode(),
            )
            assertEquals("", settings.trustedRecursiveResolverUri)
        }
    }

    @Test
    fun liveRuntimeAppliesStrictAndSystemUpdates() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.runOnMainSync {
            val runtime = GeckoRuntimeOwner.getOrCreate(context) as GeckoViewRuntimeHandle
            runtime.setDnsOverHttpsSettings(
                DnsOverHttpsSettings(DnsOverHttpsProvider.Cloudflare),
            )
            assertEquals(
                GeckoRuntimeSettings.TRR_MODE_ONLY,
                runtime.dnsOverHttpsModeForTesting(),
            )
            assertEquals(
                "https://cloudflare-dns.com/dns-query",
                runtime.dnsOverHttpsEndpointForTesting(),
            )

            runtime.setDnsOverHttpsSettings(DnsOverHttpsRules.Default)

            assertEquals(
                GeckoRuntimeSettings.TRR_MODE_DISABLED,
                runtime.dnsOverHttpsModeForTesting(),
            )
            assertEquals("", runtime.dnsOverHttpsEndpointForTesting())
        }
    }
}
