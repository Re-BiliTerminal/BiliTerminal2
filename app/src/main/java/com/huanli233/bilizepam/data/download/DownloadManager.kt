package com.huanli233.bilizepam.data.download

import android.content.Context
import android.net.Uri
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.huanli233.bilizepam.worker.download.DownloadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao
) {

    fun observeAll() = downloadDao.observeAll()

    suspend fun enqueue(request: DownloadRequest): Result<Long> {
        val existing = downloadDao.getByKey(request.key)
        if (existing != null && (existing.status == DownloadStatus.ENQUEUED || existing.status == DownloadStatus.RUNNING)) {
            return Result.success(existing.id)
        }

        val now = System.currentTimeMillis()
        val downloadId = if (existing == null) {
            downloadDao.insert(
                DownloadEntity(
                    key = request.key,
                    url = request.url,
                    headersJson = request.headersJson,
                    coverUrl = request.coverUrl,
                    fileName = request.fileName,
                    mimeType = request.mimeType,
                    status = DownloadStatus.ENQUEUED,
                    createdAt = now,
                    updatedAt = now
                )
            )
        } else {
            val updated = existing.copy(
                url = request.url,
                headersJson = request.headersJson,
                coverUrl = request.coverUrl,
                fileName = request.fileName,
                mimeType = request.mimeType,
                contentUri = null,
                status = DownloadStatus.ENQUEUED,
                progress = 0,
                downloadedBytes = 0,
                totalBytes = 0,
                errorMessage = null,
                workId = null,
                updatedAt = now,
                finishedAt = null
            )
            downloadDao.update(updated)
            updated.id
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val work = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(
                workDataOf(
                    DownloadWorker.KEY_DOWNLOAD_ID to downloadId
                )
            )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS
            )
            .addTag(DownloadWorker.TAG_DOWNLOAD)
            .addTag("key:${request.key}")
            .build()

        val workId = work.id.toString()
        downloadDao.update(downloadDao.getById(downloadId)!!.copy(workId = workId, updatedAt = System.currentTimeMillis()))

        WorkManager.getInstance(context).enqueueUniqueWork(
            request.key,
            ExistingWorkPolicy.KEEP,
            work
        )

        return Result.success(downloadId)
    }

    suspend fun cancel(downloadId: Long) {
        val entity = downloadDao.getById(downloadId) ?: return
        entity.workId?.let { id ->
            kotlin.runCatching { WorkManager.getInstance(context).cancelWorkById(UUID.fromString(id)) }
        }
        downloadDao.update(entity.copy(status = DownloadStatus.CANCELED, updatedAt = System.currentTimeMillis(), finishedAt = System.currentTimeMillis()))
    }

    suspend fun retry(downloadId: Long): Result<Long> {
        val entity = downloadDao.getById(downloadId) ?: return Result.failure(IllegalArgumentException("download not found"))
        return enqueue(
            DownloadRequest(
                key = entity.key,
                url = entity.url,
                headersJson = entity.headersJson,
                coverUrl = entity.coverUrl,
                fileName = entity.fileName,
                mimeType = entity.mimeType
            )
        )
    }

    suspend fun delete(downloadId: Long, deleteFile: Boolean) {
        val entity = downloadDao.getById(downloadId) ?: return

        entity.workId?.let { id ->
            kotlin.runCatching { WorkManager.getInstance(context).cancelWorkById(UUID.fromString(id)) }
        }

        if (deleteFile) {
            entity.contentUri?.let { raw ->
                kotlin.runCatching {
                    context.contentResolver.delete(Uri.parse(raw), null, null)
                }
            }
        }

        downloadDao.deleteById(downloadId)
    }
}
