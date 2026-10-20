package com.lince.browser.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lince.browser.R
import com.lince.browser.browser.BrowserProfile
import com.lince.browser.browser.BrowserTab
import com.lince.browser.browser.isSynced
import com.lince.browser.shared.ui.PlatformProfileEmoji

@Composable
internal fun MoveTabToProfileDialog(
    tab: BrowserTab?,
    profiles: List<BrowserProfile>,
    onMove: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val resolvedTab = tab ?: return
    val targets = profiles.filter { profile ->
        profile.id != resolvedTab.profileId &&
            (!resolvedTab.isIncognito || !profile.isSynced)
    }
    LaunchedEffect(resolvedTab.id, targets) {
        if (targets.isEmpty()) onDismiss()
    }
    if (targets.isEmpty()) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.action_move_tab_to_profile)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                targets.forEach { profile ->
                    TextButton(
                        onClick = { onMove(profile.id) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PlatformProfileEmoji(emoji = profile.emoji, fontSize = 20.sp)
                            profile.syncedDisplayName?.let { name ->
                                Spacer(Modifier.width(8.dp))
                                Text(name)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
