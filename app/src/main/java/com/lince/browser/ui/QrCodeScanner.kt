package com.lince.browser.ui

internal interface QrCodeScanner {
    fun startScan(
        onSuccess: (String) -> Unit,
        onCanceled: () -> Unit,
        onFailure: () -> Unit,
    )
}
