package com.lince.browser.sync

import com.lince.browser.browser.DEFAULT_PROFILE_EMOJI
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncDeviceIconCatalogTest {
    @Test
    fun `shared catalog exposes Android profile icons exactly once`() {
        val root = File(System.getProperty("user.dir").orEmpty())
        val catalogFile = listOf(
            File(root, "sync/protocol/device-icons-v1.json"),
            File(root, "../sync/protocol/device-icons-v1.json"),
        ).first(File::isFile)
        val catalog = SyncDeviceIconCatalog.decode(catalogFile.readText())
        assertEquals(26, catalog.icons.size)
        assertEquals(24, catalog.profileIcons.size)
        assertTrue(catalog.profileIcons.all { it.id.startsWith("lynx-") })
        assertTrue(catalog.profileIcons.none { it.id == "phone" || it.id == "computer" })
        assertTrue(catalog.icons.any { it.id == "lynx-01" && it.emoji == DEFAULT_PROFILE_EMOJI })
        assertTrue(catalog.contains("phone"))
        assertTrue(catalog.contains("computer"))
        assertTrue(catalog.icons.all { it.emoji.isNotBlank() && it.label.isNotBlank() })
    }
}
