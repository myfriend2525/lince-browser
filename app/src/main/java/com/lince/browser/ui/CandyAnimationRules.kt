package com.lince.browser.ui

internal object CandyAnimationRules {
    fun startupAnimationEnabled(
        animationsEnabled: Boolean,
        startupAnimationEnabled: Boolean,
    ): Boolean = animationsEnabled && startupAnimationEnabled

    fun favoriteLaunchAnimationEnabled(
        animationsEnabled: Boolean,
        favoriteLaunchAnimationEnabled: Boolean,
    ): Boolean = animationsEnabled && favoriteLaunchAnimationEnabled
}
