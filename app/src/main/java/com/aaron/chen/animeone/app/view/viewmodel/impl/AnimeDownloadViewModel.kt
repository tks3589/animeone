package com.aaron.chen.animeone.app.view.viewmodel.impl

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaron.chen.animeone.app.model.data.bean.AnimeDownloadBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeDownloadViewModel
import com.aaron.chen.animeone.constant.VideoConst
import com.ketch.Ketch
import com.ketch.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

@KoinViewModel
class AnimeDownloadViewModel: ViewModel(), IAnimeDownloadViewModel, KoinComponent {
    private val context: Context by inject()
    private val ketch: Ketch by lazy {
        Ketch.builder().build(context)
    }
    override val loadVideoState: MutableStateFlow<UiState<List<AnimeDownloadBean>>> = MutableStateFlow(UiState.Idle)

    override fun download(
        url: String,
        headers: HashMap<String, String>,
        episodeBean: AnimeEpisodeBean?
    ) {
        val resolver = context.contentResolver
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var defaultFileName = url.substringAfterLast("/")
        val fileName = buildString {
            if (episodeBean != null) {
                append("${VideoConst.ANIME_TAG}_${episodeBean.id}_${episodeBean.title}_${episodeBean.episode}.mp4")
                defaultFileName = "${episodeBean.title}_${episodeBean.episode}.mp4"
            } else append(defaultFileName)
        }

        val lowerName = fileName.lowercase()
        val appSubDir = "MxAnime"
        val mimeType = when {
            lowerName.endsWith(".png") -> "image/png"
            lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") -> "image/jpeg"
            lowerName.endsWith(".webp") -> "image/webp"
            lowerName.endsWith(".gif") -> "image/gif"
            lowerName.endsWith(".mov") -> "video/quicktime"
            lowerName.endsWith(".mkv") -> "video/x-matroska"
            else -> "video/mp4"
        }

        Toast.makeText(context, "開始下載", Toast.LENGTH_SHORT).show()
        createDownloadChannelIfNeeded()

        // 建立通知
        val notificationId = fileName.hashCode()
        val builder = NotificationCompat.Builder(context, "download_channel")
            .setContentTitle("下載中...")
            .setContentText(defaultFileName)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setProgress(100, 0, false)

        notificationManager.notify(notificationId, builder.build())

        // 建立暫存資料夾
        val tempDir = File(context.cacheDir, "downloads").apply { if (!exists()) mkdirs() }

        val downloadId = ketch.download(
            url = url,
            fileName = fileName,
            path = tempDir.absolutePath,
            headers = headers
        )

        viewModelScope.launch {
            ketch.observeDownloadById(downloadId)
                .flowOn(Dispatchers.IO)
                .collect { model ->
                    val progress = model?.progress ?: 0
                    builder.setProgress(100, progress, false)
                    notificationManager.notify(notificationId, builder.build())

                    when (model?.status) {
                        Status.SUCCESS -> {
                            val downloadedFile = File(tempDir, fileName)
                            if (!downloadedFile.exists()) return@collect

                            // ✅ 根據版本寫入目標位置
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val relativePath =
                                    "${Environment.DIRECTORY_DOWNLOADS}/$appSubDir"
                                val collectionUri =
                                    MediaStore.Downloads.EXTERNAL_CONTENT_URI
                                val contentValues = ContentValues().apply {
                                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                                }

                                val uri = resolver.insert(collectionUri, contentValues)
                                uri?.let {
                                    resolver.openOutputStream(it)?.use { output ->
                                        downloadedFile.inputStream().use { input ->
                                            input.copyTo(output)
                                        }
                                    }

                                    // 解鎖檔案
                                    resolver.update(
                                        it,
                                        ContentValues().apply {
                                            put(MediaStore.MediaColumns.IS_PENDING, 0)
                                        },
                                        null,
                                        null
                                    )

                                    // 點擊通知可開啟影片
                                    val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(it, mimeType)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    val pendingIntent = PendingIntent.getActivity(
                                        context,
                                        0,
                                        openIntent,
                                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                    )

                                    builder.setContentTitle("下載完成")
                                        .setContentText(defaultFileName)
                                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                                        .setProgress(0, 0, false)
                                        .setContentIntent(pendingIntent)
                                        .setAutoCancel(true)
                                    notificationManager.notify(notificationId, builder.build())
                                }
                            } else {
                                // ✅ Android 8–9：用實體檔案寫入
                                val downloadsDir =
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                val legacyDir = File(downloadsDir, appSubDir).apply { if (!exists()) mkdirs() }
                                val outFile = File(legacyDir, fileName)

                                // 有重複檔案就刪掉
                                if (outFile.exists()) outFile.delete()

                                downloadedFile.copyTo(outFile, overwrite = true)

                                // 通知媒體掃描
                                MediaScannerConnection.scanFile(
                                    context,
                                    arrayOf(outFile.absolutePath),
                                    arrayOf(mimeType),
                                    null
                                )

                                val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.fromFile(outFile), mimeType)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                val pendingIntent = PendingIntent.getActivity(
                                    context,
                                    0,
                                    openIntent,
                                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                )

                                builder.setContentTitle("下載完成")
                                    .setContentText(defaultFileName)
                                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                                    .setProgress(0, 0, false)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true)
                                notificationManager.notify(notificationId, builder.build())
                            }
                        }

                        Status.FAILED -> {
                            builder.setContentTitle("下載失敗")
                                .setContentText(fileName)
                                .setSmallIcon(android.R.drawable.stat_notify_error)
                                .setProgress(0, 0, false)
                            notificationManager.notify(notificationId, builder.build())
                        }

                        else -> Unit
                    }
                }
        }
    }

    fun deleteVideo(context: Context, path: String) {
        try {
            val uri = path.toUri()
            val resolver = context.contentResolver
            resolver.delete(uri, null, null)
        } catch (e: SecurityException) {
            Log.e("aaron_tt", "deleteVideo SecurityException: ${e.message}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val uri = path.toUri()
                val resolver = context.contentResolver
                val intentSender = MediaStore.createDeleteRequest(resolver, listOf(uri)).intentSender
                (context as? Activity)?.startIntentSenderForResult(
                    intentSender,
                    2001,
                    null,
                    0,
                    0,
                    0
                )
            } else {
                val file = File(path)
                if (file.exists()) {
                    val deleted = file.delete()
                    if (deleted) {
                        // 通知媒體庫更新
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(file.absolutePath),
                            null,
                            null
                        )
                    }
                } else {
                    Log.e("aaron_tt", "deleteVideo file not exists : $path")
                }
            }
        } catch (e: Exception) {
            Log.e("aaron_tt", "deleteVideo Exception: ${e.message}")
        }
    }

    override fun loadDownloadedVideos() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // ✅ 1. 查詢 MediaStore 中「Download/MxAnime」資料夾下的 mp4
                val projection = arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DATE_MODIFIED
                )

                val (selection, selectionArgs) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // ✅ Android 10+ 用 RELATIVE_PATH
                    Pair(
                        "${MediaStore.Video.Media.RELATIVE_PATH} LIKE ? AND ${MediaStore.Video.Media.DISPLAY_NAME} LIKE ?",
                        arrayOf("%Download/MxAnime%", "${VideoConst.ANIME_TAG}%.mp4")
                    )
                } else {
                    // ✅ Android 9 以下用 DATA（完整檔案路徑）
                    Pair(
                        "${MediaStore.Video.Media.DATA} LIKE ? AND ${MediaStore.Video.Media.DISPLAY_NAME} LIKE ?",
                        arrayOf("%/Download/MxAnime/%", "${VideoConst.ANIME_TAG}%.mp4")
                    )
                }

                val sortOrder = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"

                val result = mutableListOf<AnimeDownloadBean>()

                context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idCol)
                        val displayName = cursor.getString(nameCol)

                        // ✅ 建立 content:// URI（不再用 File 路徑）
                        val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

                        // ✅ 擷取影片中間縮圖（支援 contentUri）
                        val frame = extractMiddleFrame(contentUri.toString())

                        result.add(
                            AnimeDownloadBean(
                                name = displayName.split("_", limit = 4).drop(3).joinToString("_"),
                                path = contentUri.toString(), // 改用 content://
                                preview = frame
                            )
                        )
                    }
                }

                // ✅ 2. 更新 UI 狀態
                if (result.isEmpty()) {
                    loadVideoState.value = UiState.Empty
                } else {
                    loadVideoState.value = UiState.Success(result)
                }

            } catch (e: Exception) {
                loadVideoState.value = UiState.Error(e.message)
            }
        }
    }

    private fun extractMiddleFrame(path: String): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()

            // ✅ 根據 path 判斷是 contentUri 還是檔案路徑
            if (path.startsWith("content://")) {
                retriever.setDataSource(context, path.toUri())
            } else {
                retriever.setDataSource(path)
            }

            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
            if (duration <= 0L) {
                retriever.release()
                return null
            }

            val middleUs = (duration / 2) * 1000
            val frame = retriever.getFrameAtTime(middleUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()
            frame
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * ✅ 自動建立通知 Channel（只會建立一次）
     */
    private fun createDownloadChannelIfNeeded() {
        val channelId = "download_channel"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId,
                "下載進度",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "顯示下載進度與狀態"
            }
            manager.createNotificationChannel(channel)
        }
    }

}