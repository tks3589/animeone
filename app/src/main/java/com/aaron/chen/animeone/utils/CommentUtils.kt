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
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

object CommentUtils {

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

    fun parseMessage(html: String): List<MessagePart> {
        val parts = mutableListOf<MessagePart>()
        val document = Jsoup.parseBodyFragment(html)
        val body = document.body()

        body.childNodes().forEach { node ->
            parseNode(node, parts)
        }

        return parts
    }

    private fun parseNode(
        node: Node,
        parts: MutableList<MessagePart>
    ) {
        when (node) {
            is TextNode -> {
                val text = node.text().trim()
                if (text.isNotEmpty()) {
                    parts.add(MessagePart.Text(text))
                }
            }
            is Element -> {
                when (node.tagName()) {
                    "p" -> {
                        node.childNodes().forEach {
                            parseNode(it, parts)
                        }
                    }
                    "img" -> {
                        val url = node.attr("src")
                        if (url.isNotEmpty()) {
                            parts.add(MessagePart.Media(url))
                        }
                    }
                    "br" -> {
                        parts.add(MessagePart.BlankLine)
                    }
                    "span" -> {
                        if (node.hasClass("spoiler")) {
                            val text = node.text().trim()
                            if (text.isNotEmpty()) {
                                parts.add(MessagePart.Spoiler(text))
                            }
                        } else {
                            // 普通 span 當作 inline text
                            node.childNodes().forEach {
                                parseNode(it, parts)
                            }
                        }
                    }
                    else -> {
                        // 其他 inline element（例如 b, i, a 等）
                        node.childNodes().forEach {
                            parseNode(it, parts)
                        }
                    }
                }
            }
        }
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
    data class Spoiler(val text: String) : MessagePart()
    data class Media(val url: String) : MessagePart()
    object BlankLine: MessagePart()
}