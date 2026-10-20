package com.lince.browser.shared.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.lince.browser.shared.resources.Res
import com.lince.browser.shared.resources.profile_lynx_01
import com.lince.browser.shared.resources.profile_lynx_02
import com.lince.browser.shared.resources.profile_lynx_03
import com.lince.browser.shared.resources.profile_lynx_04
import com.lince.browser.shared.resources.profile_lynx_05
import com.lince.browser.shared.resources.profile_lynx_06
import com.lince.browser.shared.resources.profile_lynx_07
import com.lince.browser.shared.resources.profile_lynx_08
import com.lince.browser.shared.resources.profile_lynx_09
import com.lince.browser.shared.resources.profile_lynx_10
import com.lince.browser.shared.resources.profile_lynx_11
import com.lince.browser.shared.resources.profile_lynx_12
import com.lince.browser.shared.resources.profile_lynx_13
import com.lince.browser.shared.resources.profile_lynx_14
import com.lince.browser.shared.resources.profile_lynx_15
import com.lince.browser.shared.resources.profile_lynx_16
import com.lince.browser.shared.resources.profile_lynx_17
import com.lince.browser.shared.resources.profile_lynx_18
import com.lince.browser.shared.resources.profile_lynx_19
import com.lince.browser.shared.resources.profile_lynx_20
import com.lince.browser.shared.resources.profile_lynx_21
import com.lince.browser.shared.resources.profile_lynx_22
import com.lince.browser.shared.resources.profile_lynx_23
import com.lince.browser.shared.resources.profile_lynx_24
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Lince profile avatars. Each avatar keeps a unique emoji identity (stored in profiles and the
 * shared icon catalog) so text-only surfaces such as widgets, shortcut labels and Candy Sync peers
 * still show a meaningful icon.
 */
internal object ProfileAvatarRules {
    private val avatars: Map<String, DrawableResource> = mapOf(
        "😄" to Res.drawable.profile_lynx_01,
        "😎" to Res.drawable.profile_lynx_02,
        "🥺" to Res.drawable.profile_lynx_03,
        "😉" to Res.drawable.profile_lynx_04,
        "😠" to Res.drawable.profile_lynx_05,
        "😴" to Res.drawable.profile_lynx_06,
        "🧢" to Res.drawable.profile_lynx_07,
        "🐈‍⬛" to Res.drawable.profile_lynx_08,
        "🔥" to Res.drawable.profile_lynx_09,
        "🥰" to Res.drawable.profile_lynx_10,
        "🎧" to Res.drawable.profile_lynx_11,
        "💭" to Res.drawable.profile_lynx_12,
        "🤓" to Res.drawable.profile_lynx_13,
        "😆" to Res.drawable.profile_lynx_14,
        "🦹" to Res.drawable.profile_lynx_15,
        "🕶️" to Res.drawable.profile_lynx_16,
        "🥷" to Res.drawable.profile_lynx_17,
        "👑" to Res.drawable.profile_lynx_18,
        "🌸" to Res.drawable.profile_lynx_19,
        "☀️" to Res.drawable.profile_lynx_20,
        "🧑‍🚀" to Res.drawable.profile_lynx_21,
        "🌌" to Res.drawable.profile_lynx_22,
        "🌿" to Res.drawable.profile_lynx_23,
        "❄️" to Res.drawable.profile_lynx_24,
    )

    fun avatarFor(emoji: String): DrawableResource? = avatars[emoji.trim()]
}

/** True when [emoji] is drawn as a full-bleed lynx avatar instead of emoji text. */
fun hasProfileAvatar(emoji: String): Boolean = ProfileAvatarRules.avatarFor(emoji) != null

/**
 * Renders a profile icon: the lynx avatar when one exists for [emoji], otherwise the emoji.
 * [avatarSize] lets circular slots draw the avatar edge to edge; it is ignored for emoji text.
 */
@Composable
fun PlatformProfileEmoji(
    emoji: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    avatarSize: Dp = Dp.Unspecified,
) {
    val avatar = ProfileAvatarRules.avatarFor(emoji)
    if (avatar == null) {
        PlatformProfileEmojiText(emoji = emoji, fontSize = fontSize, modifier = modifier)
        return
    }
    val size = when {
        avatarSize != Dp.Unspecified -> avatarSize
        fontSize.isSpecified -> with(LocalDensity.current) { fontSize.toDp() * AVATAR_SCALE }
        else -> DEFAULT_AVATAR_SIZE
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(avatar),
            // Keeps the emoji announcement that text-rendered profile icons had.
            contentDescription = emoji,
            contentScale = ContentScale.Fit,
            modifier = if (avatarSize != Dp.Unspecified) {
                Modifier.requiredSize(size)
            } else {
                Modifier.size(size)
            },
        )
    }
}

private const val AVATAR_SCALE = 1.35f
private val DEFAULT_AVATAR_SIZE = 24.dp
