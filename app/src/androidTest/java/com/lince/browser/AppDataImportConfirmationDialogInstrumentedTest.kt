package com.lince.browser

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lince.browser.data.AppDataArchiveCompatibility
import com.lince.browser.data.AppDataArchiveEntry
import com.lince.browser.data.AppDataArchiveInspection
import com.lince.browser.data.AppDataArchiveManifest
import com.lince.browser.data.StagedAppDataArchive
import com.lince.browser.ui.AppDataImportConfirmationDialog
import com.lince.browser.ui.AppDataImportPreview
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDataImportConfirmationDialogInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun versionMismatchShowsWarningAndExplicitOverride() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val confirmCalls = AtomicInteger()
        composeRule.setContent {
            MaterialTheme {
                AppDataImportConfirmationDialog(
                    pending = pending(AppDataArchiveCompatibility.BrowserEngineMismatch),
                    onDismiss = {},
                    onConfirm = confirmCalls::incrementAndGet,
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.data_archive_import_version_warning))
            .assertExists()
        composeRule.onNodeWithText(context.getString(R.string.data_archive_import_mismatch_action))
            .performClick()

        assertEquals(1, confirmCalls.get())
    }

    @Test
    fun matchingVersionUsesNormalImportAction() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            MaterialTheme {
                AppDataImportConfirmationDialog(
                    pending = pending(AppDataArchiveCompatibility.Same),
                    onDismiss = {},
                    onConfirm = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.data_archive_import_action))
            .assertExists()
        composeRule.onNodeWithText(context.getString(R.string.data_archive_import_version_warning))
            .assertDoesNotExist()
    }

    private fun pending(compatibility: AppDataArchiveCompatibility) = AppDataImportPreview(
        staged = StagedAppDataArchive(
            fileName = "00000000-0000-0000-0000-000000000000.zip",
            inspection = AppDataArchiveInspection(
                manifest = AppDataArchiveManifest(
                    packageName = "com.lince.browser",
                    appVersionName = "0.1",
                    appVersionCode = 1L,
                    webViewVersion = "1",
                    sdkInt = 35,
                    exportedAtEpochMillis = 1L,
                ),
                entries = listOf(
                    AppDataArchiveEntry(
                        relativePath = "shared_prefs/browser_session.xml",
                        isDirectory = false,
                        size = 1L,
                        crc = 1L,
                    ),
                ),
            ),
        ),
        compatibility = compatibility,
    )
}
