package com.lince.browser

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.lince.browser.data.BrowsingHistoryRepository
import com.lince.browser.data.BrowserSessionStore
import com.lince.browser.data.CandyTrailRepository
import com.lince.browser.data.RecallRepository
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

internal object BrowsingHistoryLifecycle {
    private var installed = false
    private val cleanupExecutor = Executors.newSingleThreadExecutor { task ->
        Thread(task, "history-exit-cleanup")
    }
    private val foregroundGeneration = AtomicLong()

    @Synchronized
    fun install(application: Application) {
        if (installed) return
        installed = true
        if (!BrowserSessionStore(application).loadRecallEnabled()) {
            RecallRepository.get(application).clearAsync()
        }
        BrowsingHistoryRepository.get(application).beginSession()
        application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                private var startedActivityCount = 0

                override fun onActivityStarted(activity: Activity) {
                    if (startedActivityCount == 0) {
                        foregroundGeneration.incrementAndGet()
                        BrowsingHistoryRepository.get(application).markForegroundSessionActive()
                    }
                    startedActivityCount++
                }

                override fun onActivityStopped(activity: Activity) {
                    startedActivityCount = (startedActivityCount - 1).coerceAtLeast(0)
                    if (startedActivityCount == 0 && !activity.isChangingConfigurations) {
                        val stoppedGeneration = foregroundGeneration.get()
                        val stoppedAtMillis = System.currentTimeMillis()
                        cleanupExecutor.execute {
                            val isCurrent = { foregroundGeneration.get() == stoppedGeneration }
                            if (
                                isCurrent() &&
                                BrowsingHistoryRepository.get(application).clearOnExit(
                                    isSessionCurrent = isCurrent,
                                    untilExclusiveMillis = stoppedAtMillis,
                                )
                            ) {
                                CandyTrailRepository.get(application)
                                    .processPendingRedactions(acknowledge = false)
                            }
                        }
                    }
                }

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
                override fun onActivityResumed(activity: Activity) = Unit
                override fun onActivityPaused(activity: Activity) = Unit
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
                override fun onActivityDestroyed(activity: Activity) = Unit
            },
        )
    }
}
