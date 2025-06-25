package com.aaron.chen.animeone.app.model.data.responsevo

import android.os.Build
import androidx.annotation.RequiresApi
import com.aaron.chen.animeone.app.model.data.bean.AnimeCommentBean
import com.aaron.chen.animeone.app.model.data.bean.AvatarBean
import com.aaron.chen.animeone.app.model.data.bean.MediaBean
import com.aaron.chen.animeone.app.model.data.bean.UserBean
import com.aaron.chen.animeone.constant.DefaultConst
import com.aaron.chen.animeone.utils.DateTimeUtils
import com.google.gson.annotations.SerializedName


class AnimeCommentRespVo{
    @SerializedName("response")
    val data: List<CommentRespVo>? = null
        get() = field ?: emptyList()
}
class CommentRespVo {
    @SerializedName("id")
    val id: String? = null
        get() = field.orEmpty()

    @SerializedName("createdAt")
    val createdAt: String? = null
        get() = field.orEmpty()

    @SerializedName("raw_message")
    val message: String? = null
        get() = field.orEmpty()

    @SerializedName("likes")
    val likes: Int? = null
        get() = field ?: DefaultConst.INT_COUNT

    @SerializedName("dislikes")
    val dislikes: Int? = null
        get() = field ?: DefaultConst.INT_COUNT

    @SerializedName("media")
    val media: List<MediaVo>? = null
        get() = field ?: emptyList()

    @SerializedName("author")
    val user: UserVo? = null
        get() = field ?: UserVo()


    class MediaVo {
        @SerializedName("mediaType")
        var type: String? = null
            get() = field.orEmpty()

        @SerializedName("url")
        var url: String? = null
            get() = field.orEmpty()
    }

    class UserVo {
        @SerializedName("name")
        var name: String? = null
            get() = field.orEmpty()

        @SerializedName("avatar")
        var avatar: AvatarVo? = null
            get() = field ?: AvatarVo()

        class AvatarVo {
            @SerializedName("permalink")
            var url: String? = null
                get() = field.orEmpty()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun AnimeCommentRespVo.toCommentList(): List<AnimeCommentBean> {
    return data!!.map { comment ->
        AnimeCommentBean(
            id = comment.id!!,
            createdAt = DateTimeUtils.formatDate(comment.createdAt!!),
            message = comment.message!!,
            likes = comment.likes!!,
            dislikes = comment.dislikes!!,
            media = comment.media!!.toList(),
            user = comment.user!!.toBean()
        )
    }
}

fun List<CommentRespVo.MediaVo>.toList(): List<MediaBean> {
    return map { media ->
        MediaBean(
            type = media.type!!,
            url = media.url!!
        )
    }
}

fun CommentRespVo.UserVo.toBean(): UserBean {
    return UserBean(
        name = name!!,
        avatar = avatar!!.toBean()
    )
}

fun CommentRespVo.UserVo.AvatarVo.toBean(): AvatarBean {
    return AvatarBean(
        url = url!!
    )
}