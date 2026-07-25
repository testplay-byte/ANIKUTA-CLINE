package app.confused.anikuta.core.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Android notifications for download progress, completion, and errors.
 *
 * Per ADR-014 (notification channels) + `RULES/ai-agent-rules.md` §9. One
 * channel: `CHANNEL_DOWNLOADS`. A single summary notification shows the active
 * download count + the top task's progress; per-task completion/error posts a
 * one-shot notification.
 *
 * **Throttling.** The queue calls [updateProgress] on every byte tick; this
 * class coalesces to once per [PROGRESS_THROTTLE_MS] so we don't overrun the
 * system notification rate limit.
 *
 * The notification's tap intent opens the app's launch activity (the Downloads
 * screen is reached from More → Downloads). A direct deep-link is a future
 * enhancement once the nav state-machine supports deep links.
 */
class DownloadNotificationManager(
    private val context: Context,
) {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        ensureChannel()
    }

    /** Show/update the active-download summary notification. */
    fun updateProgress(active: List<DownloadTask>) {
        if (active.isEmpty()) {
            cancel(SUMMARY_ID)
            return
        }
        val now = System.currentTimeMillis()
        if (now - lastProgressAt < PROGRESS_THROTTLE_MS) return
        lastProgressAt = now

        val primary = active.first { it.status == DownloadStatus.DOWNLOADING } ?: active.first()
        val title = if (active.size == 1) {
            "${primary.request.anime.title} — EP ${primary.request.episode.episodeNumber.toInt()}"
        } else {
            "Downloading ${active.size} episodes"
        }
        val progressText = if (primary.totalBytes > 0) {
            "${primary.progress}% • ${formatBytes(primary.downloadedBytes)} / ${formatBytes(primary.totalBytes)}"
        } else {
            "${primary.progress}% • ${formatBytes(primary.downloadedBytes)}"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText(progressText)
            .setProgress(100, primary.progress.coerceAtLeast(0), primary.progress <= 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(openAppIntent())

        try {
            notificationManager.notify(SUMMARY_ID, builder.build())
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS not granted — fail silently (the in-app UI still works).
            DownloadLogger.w("Cannot post download notification (permission denied)", e)
        }
    }

    /** Post a one-shot completion notification. */
    fun notifyCompleted(task: DownloadTask) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Download complete")
            .setContentText("${task.request.anime.title} — EP ${task.request.episode.episodeNumber.toInt()}")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppIntent())
        try {
            notificationManager.notify(task.id.toInt() + COMPLETION_OFFSET, builder.build())
        } catch (e: SecurityException) {
            DownloadLogger.w("Cannot post completion notification", e)
        }
    }

    /** Post a one-shot error notification. */
    fun notifyError(task: DownloadTask) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Download failed")
            .setContentText("${task.request.anime.title} — ${task.errorMessage ?: "Unknown error"}")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openAppIntent())
        try {
            notificationManager.notify(task.id.toInt() + ERROR_OFFSET, builder.build())
        } catch (e: SecurityException) {
            DownloadLogger.w("Cannot post error notification", e)
        }
    }

    /** Cancel the active summary (e.g. when the queue empties). */
    fun cancelActive() = cancel(SUMMARY_ID)

    private fun cancel(id: Int) {
        try { notificationManager.cancel(id) } catch (_: Exception) { }
    }

    private fun openAppIntent(): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent()
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Download progress and completion notifications"
                setShowBadge(false)
            }
            try {
                context.getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(channel)
            } catch (e: Exception) {
                DownloadLogger.w("Failed to create download notification channel", e)
            }
        }
    }

    private fun formatBytes(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "%.1f GB".format(bytes / (1024.0 * 1024 * 1024))
    }

    companion object {
        private const val CHANNEL_ID = "anikuta_downloads"
        private const val SUMMARY_ID = 9001
        private const val COMPLETION_OFFSET = 10_000
        private const val ERROR_OFFSET = 20_000
        private const val PROGRESS_THROTTLE_MS = 800L

        @Volatile private var lastProgressAt = 0L
    }
}
