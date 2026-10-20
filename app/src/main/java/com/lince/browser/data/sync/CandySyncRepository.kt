package com.lince.browser.data.sync

import com.lince.browser.sync.AndroidArgon2RecoveryKeyDeriver
import com.lince.browser.sync.CandySyncRepository as SharedCandySyncRepository
import com.lince.browser.sync.SyncCacheStore
import com.lince.browser.sync.SyncConnectionSettings
import com.lince.browser.sync.SyncCrypto
import com.lince.browser.sync.SyncDeviceIconCatalog
import com.lince.browser.sync.SyncDispatch
import com.lince.browser.sync.SyncEnrollmentOutcome
import com.lince.browser.sync.SyncPendingMutation
import com.lince.browser.sync.SyncRecoveryKeyDeriver
import com.lince.browser.sync.SyncRepositoryState
import com.lince.browser.sync.SyncSettingsStore
import com.lince.browser.sync.SyncTransport
import com.lince.browser.sync.SyncVaultStore
import com.lince.browser.sync.SyncWriteOutcome
import java.security.SecureRandom
import java.time.Clock
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Android compatibility facade. Protocol orchestration lives once in the shared KMP repository;
 * this class only preserves the existing CompletableFuture API for BrowserController.
 */
class CandySyncRepository(
    settingsStore: SyncSettingsStore,
    vaultStore: SyncVaultStore,
    cacheStore: SyncCacheStore,
    iconCatalog: SyncDeviceIconCatalog,
    transportFactory: (String) -> SyncTransport = ::SyncHttpClient,
    crypto: SyncCrypto? = null,
    recoveryKeyDeriver: SyncRecoveryKeyDeriver = AndroidArgon2RecoveryKeyDeriver(),
    clock: Clock = Clock.systemUTC(),
    random: SecureRandom = SecureRandom(),
    private val executor: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "candy-sync").apply { isDaemon = true }
    },
    private val realtimeScheduler: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "candy-sync-realtime").apply { isDaemon = true }
        },
) : AutoCloseable {
    private val repository = SharedCandySyncRepository(
        settingsStore = settingsStore,
        vaultStore = vaultStore,
        cacheStore = cacheStore,
        iconCatalog = iconCatalog,
        transportFactory = transportFactory,
        crypto = crypto ?: SyncCrypto(random = random, clock = clock),
        recoveryKeyDeriver = recoveryKeyDeriver,
        dispatch = object : SyncDispatch {
            override fun dispatch(block: () -> Unit) {
                executor.execute(block)
            }

            override fun dispatchAfter(
                seconds: Long,
                block: () -> Unit,
            ) {
                realtimeScheduler.schedule(
                    { if (!executor.isShutdown) executor.execute(block) },
                    seconds,
                    TimeUnit.SECONDS,
                )
            }
        },
    )

    fun currentState(): SyncRepositoryState = repository.currentState()

    fun observe(listener: (SyncRepositoryState) -> Unit): AutoCloseable =
        repository.observe(listener)

    fun configure(settings: SyncConnectionSettings): Boolean = repository.configure(settings)

    fun enroll(
        serverPassword: CharArray,
        passphrase: CharArray,
    ): CompletableFuture<SyncEnrollmentOutcome> = CompletableFuture<SyncEnrollmentOutcome>().also { future ->
        repository.enroll(serverPassword, passphrase) { outcome -> future.complete(outcome) }
    }

    fun refresh(): CompletableFuture<Boolean> = CompletableFuture<Boolean>().also { future ->
        repository.refresh { refreshed -> future.complete(refreshed) }
    }

    fun mutate(mutation: SyncPendingMutation): CompletableFuture<SyncWriteOutcome> =
        CompletableFuture<SyncWriteOutcome>().also { future ->
            repository.mutate(mutation) { outcome -> future.complete(outcome) }
        }

    fun startRealtime() = repository.startRealtime()

    fun stopRealtime() = repository.stopRealtime()

    override fun close() {
        repository.close()
        realtimeScheduler.shutdownNow()
        executor.shutdownNow()
    }
}
