package com.lince.browser.browser.systemwebview

import android.content.ContentUris
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.lince.browser.browser.downloads.CandyDownloadNotifier
import com.lince.browser.browser.gecko.GeckoDownloadCancellation
import com.lince.browser.browser.gecko.GeckoDownloadFailure
import com.lince.browser.browser.gecko.GeckoDownloadStreamEntry
import com.lince.browser.browser.gecko.GeckoDownloadStreamSink
import com.lince.browser.browser.gecko.GeckoDownloadTransferListener
import com.lince.browser.browser.gecko.GeckoDownloadTransferStart
import com.lince.browser.browser.gecko.MediaStoreDownloadStreamSink
import com.lince.browser.data.BrowserDownloadRequest
import com.lince.browser.data.BrowserDownloadRequestFactory
import com.lince.browser.data.DownloadRuntimeRegistry
import com.lince.browser.data.SafeDownloadValues
import java.io.IOException
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/** A validated redirect hop. Cookies are kept only while the hop stays on the original host. */
internal data class PrivateDownloadHop(
    val url: String,
    val cookies: String?,
    val referrer: String?,
)

internal object PrivateDownloadRedirectRules {
    const val MAX_REDIRECTS = 5

    fun next(current: PrivateDownloadHop, location: String?, originalHost: String): PrivateDownloadHop? {
        val target = location?.trim()?.takeIf(String::isNotEmpty) ?: return null
        val resolved = runCatching { URI(current.url).resolve(target).toString() }.getOrNull()
            ?: return null
        if (!SafeDownloadValues.isHttpUrl(resolved)) return null
        val host = runCatching { URI(resolved).host }.getOrNull() ?: return null
        val sameHost = host.equals(originalHost, ignoreCase = true)
        return PrivateDownloadHop(
            url = resolved,
            // Private-session cookies never follow a redirect to another host.
            cookies = current.cookies.takeIf { sameHost },
            referrer = current.referrer?.let { SafeDownloadValues.referrer(it, resolved) },
        )
    }
}

/**
 * Streams System WebView private-tab downloads into scoped storage instead of Android
 * `DownloadManager`, which would persist the URL and private-session cookies in its database.
 * Cookies live only in memory for the duration of the transfer.
 */
internal class SystemWebViewPrivateDownloadTransfer(
    context: Context,
    private val sink: GeckoDownloadStreamSink = MediaStoreDownloadStreamSink(context),
    private val notifier: CandyDownloadNotifier = CandyDownloadNotifier(context),
    private val client: OkHttpClient = defaultClient,
) : AutoCloseable {
    private class Operation(val id: Int) {
        val cancelled = AtomicBoolean(false)
        val terminal = AtomicBoolean(false)
        @Volatile var call: Call? = null
        @Volatile var entry: GeckoDownloadStreamEntry? = null
    }

    private val operations = ConcurrentHashMap<Int, Operation>()
    private val closed = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val io = Executors.newCachedThreadPool { runnable ->
        Thread(runnable, "candy-private-download").apply { isDaemon = true }
    }

    fun start(
        request: BrowserDownloadRequest,
        listener: GeckoDownloadTransferListener,
    ): GeckoDownloadCancellation? {
        val safeRequest = BrowserDownloadRequestFactory.create(
            url = request.url,
            contentDisposition = "attachment; filename=\"${request.fileName}\"",
            mimeType = request.mimeType,
            userAgent = request.userAgent,
            cookies = request.cookies,
            referrer = request.referrer,
        )
        if (closed.get() || safeRequest == null) {
            dispatch { listener.onFailed(GeckoDownloadFailure.InvalidRequest) }
            return null
        }
        val operation = Operation(DownloadRuntimeRegistry.nextTransferId())
        operations[operation.id] = operation
        io.execute { run(operation, safeRequest, listener) }
        return GeckoDownloadCancellation { cancel(operation, listener) }
    }

    fun cancelAll() {
        operations.values.toList().forEach { operation -> cancel(operation, null) }
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        cancelAll()
        io.shutdownNow()
    }

    private fun run(
        operation: Operation,
        request: BrowserDownloadRequest,
        listener: GeckoDownloadTransferListener,
    ) {
        val response = runCatching { execute(operation, request) }.getOrElse {
            finish(operation, listener, if (operation.cancelled.get()) {
                GeckoDownloadFailure.Cancelled
            } else {
                GeckoDownloadFailure.Network
            })
            return
        }
        response.use { body -> transfer(operation, request, body, listener) }
    }

    private fun execute(operation: Operation, request: BrowserDownloadRequest): Response {
        val originalHost = URI(request.url).host
        var hop = PrivateDownloadHop(request.url, request.cookies, request.referrer)
        repeat(PrivateDownloadRedirectRules.MAX_REDIRECTS + 1) {
            if (operation.cancelled.get()) throw IOException("cancelled")
            val builder = Request.Builder().url(hop.url).get()
            request.userAgent?.let { builder.header("User-Agent", it) }
            hop.cookies?.let { builder.header("Cookie", it) }
            hop.referrer?.let { builder.header("Referer", it) }
            val call = client.newCall(builder.build())
            operation.call = call
            val response = call.execute()
            if (!response.isRedirect) return response
            val next = PrivateDownloadRedirectRules.next(hop, response.header("Location"), originalHost)
            response.close()
            hop = next ?: throw IOException("Rejected redirect")
        }
        throw IOException("Too many redirects")
    }

    private fun transfer(
        operation: Operation,
        request: BrowserDownloadRequest,
        response: Response,
        listener: GeckoDownloadTransferListener,
    ) {
        val body = response.body
        if (!response.isSuccessful || body == null) {
            finish(operation, listener, GeckoDownloadFailure.Http)
            return
        }
        val finalUrl = response.request.url.toString()
        val mimeType = SafeDownloadValues.mimeType(
            response.header("Content-Type") ?: request.mimeType,
            finalUrl,
        )
        val fileName = SafeDownloadValues.fileName(
            finalUrl,
            response.header("Content-Disposition")
                ?: "attachment; filename=\"${request.fileName}\"",
            mimeType,
        )
        val effectiveMimeType = if (fileName.endsWith(".apk", ignoreCase = true)) {
            BrowserDownloadRequestFactory.ANDROID_PACKAGE_MIME_TYPE
        } else {
            mimeType
        }
        val total = body.contentLength().takeIf { it >= 0L } ?: -1L
        val entry = runCatching { sink.open(fileName, effectiveMimeType) }.getOrElse {
            finish(operation, listener, GeckoDownloadFailure.Storage)
            return
        }
        val start = GeckoDownloadTransferStart(
            id = operation.id,
            fileName = fileName,
            mimeType = effectiveMimeType,
            sourceUrl = request.url,
            referrer = request.referrer,
            startedAtMillis = System.currentTimeMillis(),
            totalBytes = total,
        )
        synchronized(operation) {
            if (operation.terminal.get()) {
                runCatching(entry::abort)
                return
            }
            operation.entry = entry
            DownloadRuntimeRegistry.started(
                id = operation.id,
                name = fileName,
                source = request.url,
                mime = effectiveMimeType,
                total = total,
                startedAt = start.startedAtMillis,
                mediaStoreId = runCatching { ContentUris.parseId(entry.uri) }.getOrNull(),
                cancel = { cancel(operation, listener) },
            )
            notifier.started(operation.id)
        }
        dispatch { listener.onStarted(start) }
        var received = 0L
        val copied = runCatching {
            body.byteStream().use { input ->
                entry.output.use { output ->
                    val buffer = ByteArray(COPY_BUFFER_BYTES)
                    while (true) {
                        if (operation.cancelled.get()) throw IOException("cancelled")
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        received += count
                        DownloadRuntimeRegistry.progress(
                            id = operation.id,
                            bytes = received,
                            total = total,
                            updatedAt = System.currentTimeMillis(),
                        )
                        notifier.progress(operation.id)
                    }
                }
            }
        }
        if (copied.isFailure || operation.cancelled.get()) {
            finish(operation, listener, if (operation.cancelled.get()) {
                GeckoDownloadFailure.Cancelled
            } else {
                GeckoDownloadFailure.Network
            })
            return
        }
        synchronized(operation) {
            if (!operation.terminal.compareAndSet(false, true)) return
            operations.remove(operation.id, operation)
            val committed = runCatching(entry::commit).isSuccess
            if (!committed) {
                runCatching(entry::abort)
                DownloadRuntimeRegistry.failed(operation.id, cancelled = false, updatedAt = System.currentTimeMillis())
                notifier.cancel(operation.id)
                dispatch { listener.onFailed(GeckoDownloadFailure.Storage) }
                return
            }
            DownloadRuntimeRegistry.completed(operation.id)
            notifier.complete(operation.id, fileName, effectiveMimeType, entry.uri)
        }
        dispatch { listener.onComplete(received) }
    }

    private fun cancel(operation: Operation, listener: GeckoDownloadTransferListener?) {
        operation.cancelled.set(true)
        runCatching { operation.call?.cancel() }
        finish(operation, listener, GeckoDownloadFailure.Cancelled)
    }

    private fun finish(
        operation: Operation,
        listener: GeckoDownloadTransferListener?,
        failure: GeckoDownloadFailure,
    ) {
        synchronized(operation) {
            if (!operation.terminal.compareAndSet(false, true)) return
            operations.remove(operation.id, operation)
            runCatching { operation.entry?.abort() }
            DownloadRuntimeRegistry.failed(
                id = operation.id,
                cancelled = failure == GeckoDownloadFailure.Cancelled,
                updatedAt = System.currentTimeMillis(),
            )
            notifier.cancel(operation.id)
        }
        listener?.let { target -> dispatch { target.onFailed(failure) } }
    }

    private fun dispatch(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) action() else mainHandler.post(action)
    }

    private companion object {
        const val COPY_BUFFER_BYTES = 32 * 1024

        // No cookie jar or cache: private-session state must never be written by the client.
        val defaultClient: OkHttpClient = OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }
}
