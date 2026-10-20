package com.lince.browser.browser.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lince.browser.FavoritesActivity
import com.lince.browser.data.FavoriteEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoritesActivityContractInstrumentedTest {
    @Test
    fun launchAndNavigationResultRoundTrip() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val launchIntent = FavoritesActivityContract.launchIntent(context)
        val favorite = FavoriteEntry(
            url = "https://favorite.example/guide",
            title = "Favorite",
            addedAt = 10,
        )

        assertEquals(FavoritesActivity::class.java.name, launchIntent.component?.className)
        assertEquals(
            favorite.url,
            FavoritesActivityContract.navigationUrlFrom(
                FavoritesActivityContract.resultIntent(favorite),
            ),
        )
        assertNull(FavoritesActivityContract.navigationUrlFrom(null))
    }
}
