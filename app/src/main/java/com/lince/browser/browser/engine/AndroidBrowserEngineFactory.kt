package com.lince.browser.browser.engine

import android.content.Context
import android.content.res.Configuration
import android.view.View
import com.lince.browser.browser.AndroidBrowserEngineCapabilities
import com.lince.browser.browser.AndroidBrowserEngineKind
import com.lince.browser.browser.DnsOverHttpsSettings
import com.lince.browser.browser.HttpsOnlyMode
import com.lince.browser.browser.WebRtcProtectionMode
import com.lince.browser.browser.gecko.AndroidBrowserEngineSessionPort
import com.lince.browser.browser.gecko.BrowserEngineEventSink
import com.lince.browser.browser.gecko.BrowserEnginePreparedSession
import com.lince.browser.browser.gecko.GeckoBrowsingData
import com.lince.browser.browser.gecko.GeckoCandyTrailHistoryEventSink
import com.lince.browser.browser.gecko.GeckoExtensionActionKey
import com.lince.browser.browser.gecko.GeckoExtensionChromeHost
import com.lince.browser.browser.gecko.GeckoExtensionSessionIdentity
import com.lince.browser.browser.gecko.GeckoPrivacyEventSink
import com.lince.browser.browser.gecko.GeckoPrivacyPolicy
import com.lince.browser.browser.gecko.GeckoToppingHostState
import com.lince.browser.browser.gecko.GeckoToppingInteractionDelegate
import com.lince.browser.browser.userscript.UserScript
import com.lince.browser.browser.userscript.UserScriptMenuCommand

/** Process-wide engine adapter. Implementations must initialize only their own runtime. */
internal interface AndroidBrowserEngineFactory {
    val kind: AndroidBrowserEngineKind

    val capabilities: AndroidBrowserEngineCapabilities

    val runtimeVersionName: String?

    fun setExtensionChromeHost(host: GeckoExtensionChromeHost?) = Unit

    fun clickExtensionAction(key: GeckoExtensionActionKey): Boolean = false

    fun dismissExtensionPopup() = Unit

    fun notifySelectedExtensionTabChanged() = Unit

    fun extensionSessionIdentity(tabId: String): GeckoExtensionSessionIdentity? = null

    fun prepareSession(tabId: String, session: BrowserEnginePreparedSession): Boolean = false

    fun createExtensionPopupView(
        context: Context,
        session: BrowserEnginePreparedSession,
    ): View? = null

    fun releaseExtensionPopupView(view: View) = Unit

    fun reconcileToppings(scripts: List<UserScript>)

    fun setToppingHostStateListener(listener: (GeckoToppingHostState) -> Unit)

    fun setToppingInteractionDelegate(delegate: GeckoToppingInteractionDelegate)

    fun invokeToppingMenuCommand(command: UserScriptMenuCommand)

    fun clearToppingValues(scriptId: String)

    fun clearBrowsingData(data: GeckoBrowsingData, onComplete: (Boolean) -> Unit = {})

    fun clearAllData(onComplete: (Boolean) -> Unit = {})

    fun requestProfileDataDeletion(profileId: String): Boolean

    fun setBlockThirdPartyCookies(blocked: Boolean)

    fun setWebRtcProtectionMode(mode: WebRtcProtectionMode)

    fun setDnsOverHttpsSettings(settings: DnsOverHttpsSettings) = Unit

    fun setHttpsOnlyMode(mode: HttpsOnlyMode) = Unit

    fun setWebContentFontSizeFactor(factor: Float)

    fun setWebContentColorScheme(colorScheme: BrowserWebContentColorScheme) = Unit

    fun onConfigurationChanged(configuration: Configuration) = Unit

    fun setForceDarkWebsites(enabled: Boolean) = Unit

    fun clearPrivateData() = Unit

    fun shutdown() = Unit

    fun create(
        tabId: String,
        profileId: String,
        isolationEnabled: Boolean = false,
        isPrivate: Boolean,
        contentKind: BrowserEngineContentKind,
        privacyPolicy: GeckoPrivacyPolicy = GeckoPrivacyPolicy.Disabled,
        privacyEventSink: GeckoPrivacyEventSink = GeckoPrivacyEventSink { },
        trailHistoryEventSink: GeckoCandyTrailHistoryEventSink =
            GeckoCandyTrailHistoryEventSink { _, _ -> },
        eventSink: BrowserEngineEventSink,
    ): AndroidBrowserEngineSessionPort
}

internal enum class BrowserEngineContentKind {
    RegularTab,
    LinkPeek,
    ExternalPreview,
}

internal enum class BrowserWebContentColorScheme {
    System,
    Light,
    Dark,
}
