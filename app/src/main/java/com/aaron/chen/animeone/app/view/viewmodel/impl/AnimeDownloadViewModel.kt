package com.aaron.chen.animeone.app.view.viewmodel.impl

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
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
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
import java.io.File

@KoinViewModel
class AnimeDownloadViewModel(val context: Context): ViewModel(), IAnimeDownloadViewModel, KoinComponent {
    private val ketch: Ketch by lazy {
        Ketch.builder().build(context)
    }
    override val loadVideoState: MutableStateFlow<UiState<List<AnimeDownloadBean>>> = MutableStateFlow(UiState.Idle)

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun download(url: String, headers: HashMap<String, String>, episodeBean: AnimeEpisodeBean?) {
        var defaultFileName = url.substringAfterLast("/")
        val fileName = buildString {
            if (episodeBean != null) {
                append("${VideoConst.ANIME_TAG}_${episodeBean.id}_${episodeBean.title}_${episodeBean.episode}.mp4")
                defaultFileName = "${episodeBean.title}_${episodeBean.episode}.mp4"
            } else {
                append(defaultFileName)
            }
        }
        val lowerName = fileName.lowercase()
        val appPath = "/MxAnime"

        Toast.makeText(context, "開始下載", Toast.LENGTH_SHORT).show()

        // ✅ 自動建立通知 Channel
        createDownloadChannelIfNeeded()

        // ✅ 根據副檔名判斷檔案型別
        val isImage = listOf(".jpg", ".jpeg", ".png", ".webp", ".gif").any { lowerName.endsWith(it) }
        val isVideo = listOf(".mp4", ".mov", ".mkv").any { lowerName.endsWith(it) }

       // ✅ 正確 MIME type（避免 image/jpg 錯誤，也支援 GIF）
        val mimeType = when {
            lowerName.endsWith(".png") -> "image/png"
            lowerName.endsWith(".webp") -> "image/webp"
            lowerName.endsWith(".gif") -> "image/gif"
            lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") -> "image/jpeg"
            lowerName.endsWith(".mp4") -> "video/mp4"
            lowerName.endsWith(".mov") -> "video/quicktime"
            lowerName.endsWith(".mkv") -> "video/x-matroska"
            else -> if (isImage) "image/jpeg" else "video/mp4"
        }

        val relativePath = "${Environment.DIRECTORY_DOWNLOADS}$appPath"
        val collectionUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI

        // 1️⃣ 建立 MediaStore 預留項
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(collectionUri, contentValues)

        if (uri == null) {
            Toast.makeText(context, "下載失敗", Toast.LENGTH_SHORT).show()
            return
        }

        // 2️⃣ 暫存資料夾
        val tempDir = File(context.cacheDir, "downloads").apply { if (!exists()) mkdirs() }

        // 3️⃣ 開始下載
        val downloadId = ketch.download(
            url = url,
            fileName = fileName,
            path = tempDir.absolutePath,
            headers = headers
        )

        // 🔔 建立通知
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = fileName.hashCode()

        val builder = NotificationCompat.Builder(context, "download_channel")
            .setContentTitle("下載中...")
            .setContentText(defaultFileName)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setProgress(100, 0, false)

        notificationManager.notify(notificationId, builder.build())

        // 4️⃣ 監聽進度與完成狀態
        viewModelScope.launch {
            ketch.observeDownloadById(downloadId)
                .flowOn(Dispatchers.IO)
                .collect { model ->
                    val progress = model?.progress ?: 0
                    builder.setProgress(100, progress, false)
                    notificationManager.notify(notificationId, builder.build())

                    if (model?.status?.name?.contains(Status.SUCCESS.name, ignoreCase = true) == true) {
                        val downloadedFile = File(tempDir, fileName)
                        if (downloadedFile.exists()) {
                            resolver.openOutputStream(uri)?.use { output ->
                                downloadedFile.inputStream().use { input ->
                                    input.copyTo(output)
                                }
                            }

                            // ✅ 解鎖檔案
                            val updateValues = ContentValues().apply {
                                put(MediaStore.MediaColumns.IS_PENDING, 0)
                            }
                            resolver.update(uri, updateValues, null, null)

                            // ✅ 通知系統掃描
                            MediaScannerConnection.scanFile(
                                context,
                                arrayOf("${Environment.getExternalStorageDirectory()}/$relativePath/$fileName"),
                                arrayOf(mimeType),
                                null
                            )

                            // 📍 新增：點通知可開啟檔案
                            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, mimeType)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            val pendingIntent = PendingIntent.getActivity(
                                context,
                                0,
                                openIntent,
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            )

                            // ✅ 完成通知
                            builder.setContentTitle("下載完成")
                                .setContentText(defaultFileName)
                                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                                .setProgress(0, 0, false)
                                .setContentIntent(pendingIntent) // 📌 ← 點擊可直接開啟
                                .setAutoCancel(true) // 點一下自動關閉通知
                            notificationManager.notify(notificationId, builder.build())
                        }
                    }

                    if (model?.status?.name?.contains(Status.FAILED.name, ignoreCase = true) == true) {
                        builder.setContentTitle("下載失敗")
                            .setContentText(fileName)
                            .setSmallIcon(android.R.drawable.stat_notify_error)
                            .setProgress(0, 0, false)
                        notificationManager.notify(notificationId, builder.build())
                    }
                }
        }
    }

    fun deleteVideo(file: File) {
        if (file.exists()) {
            file.delete()
        }
    }


    override fun loadDownloadedVideos() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("aaron_tt", "loadDownloadedVideos_loading")
            //loadVideoState.value = UiState.Loading
            try {
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadDir, "MxAnime")

                // ✅ 1. 取得所有 mp4 檔案
                val files = targetDir.listFiles()?.filter {
                    it.isFile && it.name.startsWith(VideoConst.ANIME_TAG) && it.name.endsWith(".mp4")
                }.orEmpty()

                Log.d("aaron_tt", "file: $files")

                // ✅ 2. 按照最後修改時間排序（最新的排最前面）
                val sortedFiles = files.sortedByDescending { it.lastModified() }

                // ✅ 3. 建立結果列表
                val result = sortedFiles.map { file ->
                    val frame = extractMiddleFrame(file.path)
                    AnimeDownloadBean(
                        name = file.name.split("_", limit = 4).drop(3).joinToString("_"),
                        path = file.path,
                        preview = frame
                    )
                }

                // ✅ 4. 更新 UI 狀態
                if (result.isEmpty()) {
                    loadVideoState.value = UiState.Empty
                    Log.d("aaron_tt", "loadDownloadedVideos_empty")
                } else {
                    loadVideoState.value = UiState.Success(result)
                    Log.d("aaron_tt", "loadDownloadedVideos_success_${result.map { it.name }}")
                }
            } catch (e: Exception) {
                loadVideoState.value = UiState.Error(e.message)
                Log.d("aaron_tt", "loadDownloadedVideos_error_${e.message}")
            }
        }
    }


    fun getVideoContentUri(context: Context, file: File): Uri? {
        val projection = arrayOf(MediaStore.Video.Media._ID)
        val selection = MediaStore.Video.Media.DATA + "=?"
        val selectionArgs = arrayOf(file.absolutePath)

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                return ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
            }
        }
        return null
    }


    private fun extractMiddleFrame(path: String): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

}