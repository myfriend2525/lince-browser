package com.lince.browser.ui

internal object SettingsBackRules {
    fun parentDestination(destination: SettingsDestination): SettingsDestination = when (destination) {
        SettingsDestination.ToppingCatalog -> SettingsDestination.Userscripts
        SettingsDestination.AddressBarLongPressActions,
        SettingsDestination.AddressBarActions,
        SettingsDestination.MenuActions,
        SettingsDestination.LinkPeekActions -> SettingsDestination.TabsAndGestures
        else -> SettingsDestination.Home
    }
}
