package com.lince.browser.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit

@Composable
expect fun PlatformProfileEmojiText(
    emoji: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
)
