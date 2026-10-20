package com.lince.browser.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal fun rememberQrCodeScanner(): QrCodeScanner = remember {
    object : QrCodeScanner {
        override fun startScan(
            onSuccess: (String) -> Unit,
            onCanceled: () -> Unit,
            onFailure: () -> Unit,
        ) = onFailure()
    }
}
