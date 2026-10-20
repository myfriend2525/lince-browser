package com.lince.browser.capsule

import com.lince.browser.browser.BrowserProfile

data class CapsuleProfileProjection(
    val profileId: String,
    val isolationEnabled: Boolean,
    val storageContextId: String,
)

object CapsuleProfileRules {
    fun resolve(
        capsule: SiteCapsule,
        profiles: List<BrowserProfile>,
        multiProfileSupported: Boolean,
    ): CapsuleProfileProjection? {
        val profile = profiles.firstOrNull { it.id == capsule.profileId } ?: return null
        val isolated = profile.isolationEnabled && multiProfileSupported
        return CapsuleProfileProjection(
            profileId = profile.id,
            isolationEnabled = isolated,
            storageContextId = profile.id,
        )
    }
}
