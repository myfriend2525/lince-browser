package com.lince.browser.browser.systemwebview

import android.webkit.WebView

internal fun currentSystemWebViewIdentity(): String =
    "android.webkit.WebView@${WebView.getCurrentWebViewPackage()?.versionName.orEmpty()}"
