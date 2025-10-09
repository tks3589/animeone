package com.aaron.chen.animeone.utils

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import coil.decode.GifDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.aaron.chen.animeone.app.model.data.bean.AnimeVideoBean
import com.aaron.chen.animeone.constant.VideoConst
import com.aaron.chen.animeone.module.retrofit.RetrofitModule

object MediaUtils {

    fun getImageRequest(context: Context, url: String): ImageRequest {
        return ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .apply {
                if (url.endsWith(".gif")) {
                    decoderFactory(GifDecoder.Factory())
                }
            }.build()
    }

    fun splitMessageWithMedia(message: String): List<MessagePart> {
        val imageUrlRegex = "(https?://[^\\s]+\\.(jpg|jpeg|png|gif))".toRegex(RegexOption.IGNORE_CASE)
        val parts = mutableListOf<MessagePart>()
        var lastIndex = 0
        imageUrlRegex.findAll(message).forEachIndexed { index, match ->
            val start = match.range.first
            if (start > lastIndex) {
                val text = message.substring(lastIndex, start)
                parts.add(MessagePart.Text(text))
            }
            parts.add(MessagePart.ImagePlaceholder(index))
            lastIndex = match.range.last + 1
        }
        if (lastIndex < message.length) {
            parts.add(MessagePart.Text(message.substring(lastIndex)))
        }
        return parts
    }

    @OptIn(UnstableApi::class)
    fun getMediaSource(video: AnimeVideoBean): MediaSource {
        val mediaItem = MediaItem.fromUri(getVideoSrc(video))
        val dataSourceFactory = DefaultHttpDataSource.Factory().setDefaultRequestProperties(getVideoHeaders(video.cookie))
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
    }

    fun getVideoHeaders(cookie: String): HashMap<String, String> {
        return hashMapOf(
            "Cookie" to cookie,
            "Referer" to RetrofitModule.BASE_URL,
            "User-Agent" to VideoConst.USER_AGENTS_LIST.random()
        )
    }

    fun getVideoSrc(video: AnimeVideoBean): String {
        val videoSrc = video.src
        val fixedUrl = if (videoSrc.startsWith("//")) "https:$videoSrc" else videoSrc
        return fixedUrl
    }
}

sealed class MessagePart {
    data class Text(val text: String) : MessagePart()
    data class ImagePlaceholder(val mediaIndex: Int) : MessagePart()
}