package com.aaron.chen.animeone.app.model.data.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimeCommentBean (
    val id: String,
    val pid: String,
    val text: String,
    val user: UserBean,
    val locator: LocatorBean,
    val score: Int,
    val vote: Int,
    val time: String,
    val imported: Boolean,
    val title: String,
    val isReply: Boolean
) : Parcelable

@Parcelize
data class UserBean(
    val name: String,
    val id: String,
    val picture: String,
    val admin: Boolean
) : Parcelable

@Parcelize
data class LocatorBean(
    val site: String,
    val url: String
) : Parcelable