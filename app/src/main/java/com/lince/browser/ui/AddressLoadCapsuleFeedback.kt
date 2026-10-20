package com.lince.browser.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lince.browser.data.BrowserAddressLoadStyle
import com.lince.browser.shared.ui.AddressLoadCapsuleFeedback as SharedAddressLoadCapsuleFeedback

internal typealias AddressLoadFeedbackMode =
    com.lince.browser.shared.ui.AddressLoadFeedbackMode
internal typealias AddressLoadFeedbackState =
    com.lince.browser.shared.ui.AddressLoadFeedbackState
internal typealias AddressLoadSegment =
    com.lince.browser.shared.ui.AddressLoadSegment
internal typealias AddressLoadCapsuleRules =
    com.lince.browser.shared.ui.AddressLoadCapsuleRules

@Composable
internal fun AddressLoadCapsuleFeedback(
    tabId: String,
    isLoading: Boolean,
    progressPercent: Int,
    morphProgress: Float,
    morphTargetSizePx: Float,
    sourceCornerRadiusPx: Float? = null,
    style: BrowserAddressLoadStyle = BrowserAddressLoadStyle.Rainbow,
    modifier: Modifier = Modifier,
) {
    SharedAddressLoadCapsuleFeedback(
        tabId = tabId,
        isLoading = isLoading,
        progressPercent = progressPercent,
        morphProgress = morphProgress,
        morphTargetSizePx = morphTargetSizePx,
        sourceCornerRadiusPx = sourceCornerRadiusPx,
        style = style,
        modifier = modifier,
    )
}
