package com.lince.browser.ui

import kotlin.math.abs

internal object TabFocusHapticRules {
    fun crossedEntryCount(previousPage: Int, currentPage: Int): Int =
        abs(currentPage - previousPage)
}
