package com.inspiredandroid.kai.linux

import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val BUFFER_SIZE = 128 * 1024
private const val MAX_RETRIES_PER_URL = 3

/**
 * Streams a rootfs tarball to disk, trying each candidate URL in turn with
 * automatic resume and multi-retry resilience.
 */
class RootfsDownloader(private val httpClient: HttpClient) {

    /**
     * Cancellable through the calling coroutine. A failed URL tries up to [MAX_RETRIES_PER_URL]
     * before falling back to the next candidate mirror.
     */
    suspend fun download(
        urls: List<String>,
        targetFile: File,
        onProgress: (Float) -> Unit,
    ) {
        require(urls.isNotEmpty()) { "No download URL for this rootfs" }
        targetFile.parentFile?.mkdirs()
        var lastError: Exception? = null

        for (url in urls) {
            for (attempt in 1..MAX_RETRIES_PER_URL) {
                try {
                    downloadFrom(url, targetFile, onProgress)
                    return
                } catch (e: CancellationException) {
                    targetFile.delete()
                    throw e
                } catch (e: Exception) {
                    lastError = e
                    if (attempt < MAX_RETRIES_PER_URL) {
                        delay(1000L * attempt)
                    }
                }
            }
            targetFile.delete()
            onProgress(0f)
        }
        throw IOException("All rootfs download mirrors failed: ${lastError?.message}", lastError)
    }

    private suspend fun downloadFrom(
        url: String,
        targetFile: File,
        onProgress: (Float) -> Unit,
    ) {
        val existingBytes = if (targetFile.exists()) targetFile.length() else 0L

        httpClient.prepareGet(url) {
            headers {
                append(HttpHeaders.UserAgent, "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36")
                if (existingBytes > 0L) {
                    append(HttpHeaders.Range, "bytes=$existingBytes-")
                }
            }
        }.execute { response ->
            val isResume = response.status == HttpStatusCode.PartialContent
            if (!response.status.isSuccess() && !isResume) {
                throw IOException("HTTP ${response.status.value} from $url")
            }

            val remainingBytes = response.contentLength() ?: -1L
            val totalBytes = if (isResume && remainingBytes > 0L) existingBytes + remainingBytes else (if (remainingBytes > 0L) remainingBytes else -1L)
            val channel = response.bodyAsChannel()
            val buffer = ByteArray(BUFFER_SIZE)
            var downloadedBytes = if (isResume) existingBytes else 0L

            FileOutputStream(targetFile, isResume).use { output ->
                while (!channel.isClosedForRead) {
                    currentCoroutineContext().ensureActive()
                    val bytesRead = channel.readAvailable(buffer)
                    if (bytesRead <= 0) break
                    output.write(buffer, 0, bytesRead)
                    downloadedBytes += bytesRead
                    if (totalBytes > 0L) {
                        onProgress((downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f))
                    }
                }
                output.flush()
            }
            onProgress(1f)
        }
    }
}
