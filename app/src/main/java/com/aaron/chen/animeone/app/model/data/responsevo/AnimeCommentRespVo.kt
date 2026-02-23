package com.aaron.chen.animeone.app.model.data.responsevo

import com.aaron.chen.animeone.app.model.data.bean.AnimeCommentBean
import com.aaron.chen.animeone.app.model.data.bean.LocatorBean
import com.aaron.chen.animeone.app.model.data.bean.UserBean
import com.aaron.chen.animeone.constant.DefaultConst
import com.aaron.chen.animeone.utils.DateTimeUtils
import com.google.gson.annotations.SerializedName


class AnimeCommentRespVo{
    @SerializedName("comments")
    var data: List<CommentBlockRespVo>? = null
        get() = field ?: emptyList()

    @SerializedName("info")
    var info: InfoRespVo? = null
        get() = field ?: InfoRespVo()
}

class InfoRespVo {
    @SerializedName("url")
    var url: String? = null
        get() = field.orEmpty()

    @SerializedName("count")
    var count: Int? = null
        get() = field ?: DefaultConst.INT_COUNT

    @SerializedName("count_left")
    var countLeft: Int? = null
        get() = field ?: DefaultConst.INT_COUNT

    @SerializedName("first_time")
    var firstTime: String? = null
        get() = field.orEmpty()

    @SerializedName("last_time")
    var lastTime: String? = null
        get() = field.orEmpty()
}

class CommentBlockRespVo {
    @SerializedName("comment")
    var comment: CommentRespVo? = null
        get() = field ?: CommentRespVo()

    @SerializedName("replies")
    var replies: List<CommentBlockRespVo>? = null
        get() = field ?: emptyList()
}

class CommentRespVo {
    @SerializedName("id")
    var id: String? = null
        get() = field.orEmpty()

    @SerializedName("pid")
    var pid: String? = null
        get() = field.orEmpty()

    @SerializedName("text")
    var text: String? = null
        get() = field.orEmpty()

    @SerializedName("user")
    var user: UserVo? = null
        get() = field ?: UserVo()

    @SerializedName("locator")
    var locator: LocatorVo? = null
        get() = field ?: LocatorVo()

    @SerializedName("score")
    var score: Int? = null
        get() = field ?: DefaultConst.INT_COUNT

    @SerializedName("vote")
    var vote: Int? = null
        get() = field ?: DefaultConst.INT_COUNT

    @SerializedName("time")
    var time: String? = null
        get() = field.orEmpty()

    @SerializedName("imported")
    var imported: Boolean? = null
        get() = field ?: false

    @SerializedName("title")
    var title: String? = null
        get() = field.orEmpty()

    class UserVo {
        @SerializedName("name")
        var name: String? = null
            get() = field.orEmpty()

        @SerializedName("id")
        var id: String? = null
            get() = field.orEmpty()

        @SerializedName("picture")
        var picture: String? = null
            get() = field.orEmpty()

        @SerializedName("admin")
        var admin: Boolean? = null
            get() = field ?: false
    }

    class LocatorVo {
        @SerializedName("site")
        var site: String? = null
            get() = field.orEmpty()

        @SerializedName("url")
        var url: String? = null
            get() = field.orEmpty()
    }
}

fun AnimeCommentRespVo.toCommentList(): List<AnimeCommentBean> {
    return data!!.flatMap { block ->
        block.toCommentBeanList(isReply = false)
    }
}

private fun CommentBlockRespVo.toCommentBeanList(isReply: Boolean): List<AnimeCommentBean> {
    val current = comment
    val currentBean = AnimeCommentBean(
        id = current!!.id!!,
        pid = current.pid!!,
        text = current.text!!,
        user = current.user!!.toUserBean(),
        locator = current.locator!!.toLocatorBean(),
        score = current.score!!,
        vote = current.vote!!,
        time = DateTimeUtils.formatDate(current.time!!),
        imported = current.imported!!,
        title = current.title!!,
        isReply = isReply
    )

    val replyBeans = replies!!.flatMap { reply ->
        reply.toCommentBeanList(isReply = true)
    }

    return listOf(currentBean) + replyBeans
}

fun CommentRespVo.LocatorVo.toLocatorBean(): LocatorBean {
    return LocatorBean(
        site = site!!,
        url = url!!
    )
}

fun CommentRespVo.UserVo.toUserBean(): UserBean {
    return UserBean(
        name = name!!,
        id = id!!,
        picture = picture!!,
        admin = admin!!
    )
}