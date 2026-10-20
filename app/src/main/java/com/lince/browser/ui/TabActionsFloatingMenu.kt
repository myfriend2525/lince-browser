package com.lince.browser.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.lince.browser.R
import com.lince.browser.browser.AddressResolver
import com.lince.browser.browser.BLANK_URL
import com.lince.browser.browser.BrowserProfile
import com.lince.browser.browser.BrowserTab
import com.lince.browser.browser.gecko.GeckoExtensionActionKey
import com.lince.browser.browser.gecko.GeckoExtensionActionState
import com.lince.browser.shared.browser.BrowserFeatureMenuAction
import com.lince.browser.shared.browser.BrowserMenuLayout
import com.lince.browser.shared.ui.TabActionsMenuEffects
import com.lince.browser.shared.ui.TabActionsMenuState
import com.lince.browser.shared.ui.TabActionsProfile
import com.lince.browser.shared.ui.TabActionsFloatingMenu as SharedTabActionsFloatingMenu
import com.lince.browser.ui.theme.browserChromeColor
import com.lince.browser.ui.theme.browserChromeSurfaceTokens

internal typealias TabActionsMenuMotion =
    com.lince.browser.shared.ui.TabActionsMenuMotion

@Composable
internal fun TabActionsFloatingMenu(
    tab: BrowserTab?,
    backdropSource: CandyChromeBackdropSource? = null,
    profiles: List<BrowserProfile>,
    isFavorite: Boolean,
    canToggleDomainMute: Boolean,
    isDomainMuted: Boolean,
    canCloseAllTabs: Boolean,
    hasPinnedTabs: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenCandyTrail: () -> Unit,
    onTogglePinned: () -> Unit,
    onMoveToProfile: (String) -> Unit,
    onShare: () -> Unit,
    onOpenExternal: () -> Unit,
    onPrint: () -> Unit,
    onDomainMutedChange: (Boolean) -> Unit,
    onAddSiteCapsule: () -> Unit,
    onSummarize: () -> Unit,
    onSnooze: () -> Unit,
    onCloseAllTabs: () -> Unit,
    stackContent: @Composable ColumnScope.() -> Unit = {},
    onDismiss: () -> Unit,
    extensionActions: List<GeckoExtensionActionState> = emptyList(),
    onExtensionAction: (GeckoExtensionActionKey) -> Unit = {},
    menuLayout: BrowserMenuLayout = BrowserMenuLayout.Default,
) {
    BackHandler(enabled = tab != null, onBack = onDismiss)
    val configuration = LocalConfiguration.current
    val presented = tab?.let { presentedTab ->
        TabActionsMenuState(
            tabId = presentedTab.id,
            pageSubtitle = if (presentedTab.url == BLANK_URL) {
                stringResource(R.string.new_tab_title)
            } else {
                AddressResolver.displayText(presentedTab.url)
            },
            canToggleFavorite = presentedTab.url != BLANK_URL && !presentedTab.isIncognito,
            isFavorite = isFavorite,
            isPinned = presentedTab.isPinned,
            canUsePageActions = presentedTab.url != BLANK_URL,
            canToggleDomainMute = canToggleDomainMute,
            isDomainMuted = isDomainMuted,
            canOpenCandyTrail = presentedTab.url != BLANK_URL,
            canAddSiteCapsule = presentedTab.url != BLANK_URL &&
                !presentedTab.isIncognito &&
                (presentedTab.url.startsWith("https://") ||
                    presentedTab.url.startsWith("http://")),
            canSummarize = presentedTab.url != BLANK_URL,
            canSnooze = !presentedTab.isIncognito,
            canCloseAllTabs = canCloseAllTabs,
            hasPinnedTabs = hasPinnedTabs,
            profiles = profiles
                .filter { profile -> profile.id != presentedTab.profileId }
                .map { profile -> TabActionsProfile(profile.id, profile.emoji) },
            menuLayout = menuLayout,
        )
    }
    val chromeTokens = browserChromeSurfaceTokens().copy(
        containerColor = browserChromeColor(MaterialTheme.colorScheme.surfaceContainerLow),
        tonalElevation = 0.dp,
        shadowElevation = 6.dp,
    )
    val effects = object : TabActionsMenuEffects {
        @Composable
        override fun menuSurface(
            modifier: Modifier,
            shape: Shape,
            content: @Composable () -> Unit,
        ) {
            CandyChromeSurface(
                backdropSource = backdropSource,
                tokens = chromeTokens,
                modifier = modifier,
                shape = shape,
                content = content,
            )
        }

        @Composable
        override fun containerColor(color: Color, frostedAlpha: Float): Color =
            browserChromeColor(color, frostedAlpha)
    }
    val extensionSnapshot = extensionActions.toList()
    var presentedExtensionActions by remember { mutableStateOf(extensionSnapshot) }
    if (tab != null && presentedExtensionActions != extensionSnapshot) {
        presentedExtensionActions = extensionSnapshot
    }
    SharedTabActionsFloatingMenu(
        state = presented,
        screenSize = DpSize(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp),
        resources = AndroidTabActionsMenuResources,
        effects = effects,
        onToggleFavorite = { onToggleFavorite() },
        onTogglePinned = { onTogglePinned() },
        onMoveToProfile = { _, profileId -> onMoveToProfile(profileId) },
        onAction = { _, action ->
            when (action) {
                BrowserFeatureMenuAction.Share -> onShare()
                BrowserFeatureMenuAction.OpenExternal -> onOpenExternal()
                BrowserFeatureMenuAction.Print -> onPrint()
                BrowserFeatureMenuAction.OpenCandyTrail -> onOpenCandyTrail()
                BrowserFeatureMenuAction.AddSiteCapsule -> onAddSiteCapsule()
                BrowserFeatureMenuAction.Summarize -> onSummarize()
                BrowserFeatureMenuAction.SnoozeTab -> onSnooze()
                else -> Unit
            }
        },
        onDomainMutedChange = { _, muted -> onDomainMutedChange(muted) },
        onCloseAllTabs = { onCloseAllTabs() },
        onDismiss = onDismiss,
        extensionContent = {
            FirefoxExtensionMenuSection(
                actions = presentedExtensionActions,
                onAction = onExtensionAction,
            )
        },
        profileContent = stackContent,
    )
}
