package com.aaron.chen.animeone.app.model.data.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimeCommentBean (
    val id: String,
    val createdAt: String,
    val parent: String,
    val message: String,
    val likes: Int,
    val dislikes: Int,
    val media: List<MediaBean>,
    val user: UserBean,
    val hasNext: Boolean,
    val next: String
) : Parcelable

@Parcelize
data class MediaBean(
    val type: String,
    val url: String
) : Parcelable

@Parcelize
data class UserBean(
    val name: String,
    val avatar: AvatarBean
) : Parcelable

@Parcelize
data class AvatarBean(
    val url: String
) : Parcelable