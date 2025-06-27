package com.aaron.chen.animeone.app.model.repository.api

import android.net.Uri
import app.cash.turbine.test
import com.aaron.chen.animeone.app.model.data.bean.AnimeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeCommentBean
import com.aaron.chen.animeone.app.model.data.bean.AvatarBean
import com.aaron.chen.animeone.app.model.data.bean.UserBean
import com.aaron.chen.animeone.app.model.data.responsevo.AnimeCommentRespVo
import com.aaron.chen.animeone.app.model.data.responsevo.AnimeListRespVo
import com.aaron.chen.animeone.app.model.data.responsevo.CommentRespVo
import com.aaron.chen.animeone.extension.mockkRelaxed
import com.aaron.chen.animeone.module.retrofit.IRetrofitApi
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.koin.KoinExtension
import io.kotest.koin.KoinLifecycleMode
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.koin.dsl.module

class AnimeApiModelLocalTest: FeatureSpec() {
    private val mockApi: IRetrofitApi = mockkRelaxed()

    private val koinTestModule = module {
        single { mockApi }
    }

    override fun extensions(): List<Extension> = listOf(
        KoinExtension(module = koinTestModule, mode = KoinLifecycleMode.Root)
    )

    override suspend fun beforeEach(testCase: TestCase) {
        mockkStatic(Uri::class)
        every {Uri.parse(any())} returns mockkRelaxed()
    }

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        clearAllMocks()
    }

    init {
        feature("Get anime list") {
            scenario("Get list success") {
                val apiModel = AnimeoneApiModel()
                val respVo = AnimeListRespVo().apply {
                    animes = listOf(
                        AnimeListRespVo.AnimeRespVo().apply {
                            id = "1"
                            title = "title1"
                            status = "status1"
                            year = "year1"
                            season = "season1"
                            fansub = "fansub1"
                        },
                        AnimeListRespVo.AnimeRespVo().apply {
                            id = "2"
                            title = "title2"
                            status = "status2"
                            year = "year2"
                            season = "season2"
                            fansub = "fansub2"
                        },
                        AnimeListRespVo.AnimeRespVo().apply {
                            id = "3"
                            title = "title3"
                            status = "status3"
                            year = "year3"
                            season = "season3"
                            fansub = "fansub3"
                        }
                    )
                }
                val expected = listOf(
                    AnimeBean(id = "1", title = "title1", status = "status1", year = "year1", season = "season1", fansub = "fansub1"),
                    AnimeBean(id = "2", title = "title2", status = "status2", year = "year2", season = "season2", fansub = "fansub2"),
                    AnimeBean(id = "3", title = "title3", status = "status3", year = "year3", season = "season3", fansub = "fansub3")
                )
                every { mockApi.getAnimeList(any(), any()) } returns flowOf(respVo)
                apiModel.getAnimeList()
                    .test {
                        awaitItem() shouldBe expected
                        awaitComplete()
                    }
            }
            scenario("Get list fail") {
                val error = Throwable("fail!")
                every { mockApi.getAnimeList(any(), any()) } returns flow { throw error }
                val apiModel = AnimeoneApiModel()
                apiModel.getAnimeList()
                    .test {
                        awaitError().message shouldBe error.message
                    }
            }
        }
        feature("Get comment list") {
            scenario("Get comments success") {
                val apiModel = AnimeoneApiModel()
                val respVo = AnimeCommentRespVo().apply {
                    data = listOf(
                        CommentRespVo().apply {
                            id = "1"
                            createdAt = "2023-10-01T12:00:00"
                            message = "message1"
                            likes = 1
                            dislikes = 1
                            media = emptyList()
                            user = CommentRespVo.UserVo().apply {
                                name = "user1"
                                avatar = CommentRespVo.UserVo.AvatarVo().apply { url = "url1" }
                            }
                        },
                       CommentRespVo().apply {
                            id = "2"
                            createdAt = "2023-10-01T13:00:00"
                            message = "message2"
                            likes = 1
                            dislikes = 1
                            media = emptyList()
                            user =  CommentRespVo.UserVo().apply {
                                name = "user2"
                                avatar = CommentRespVo.UserVo.AvatarVo().apply { url = "url2" }
                            }
                        },
                        CommentRespVo().apply {
                            id = "3"
                            createdAt = "2023-10-01T14:00:00"
                            message = "message3"
                            likes = 1
                            dislikes = 1
                            media = emptyList()
                            user = CommentRespVo.UserVo().apply {
                                name = "user3"
                                avatar = CommentRespVo.UserVo.AvatarVo().apply { url = "url3" }
                            }
                        }
                    )
                }
                val expected = listOf(
                    AnimeCommentBean(id = "1", createdAt = "2023/10/01 12:00", message = "message1", likes = 1, dislikes = 1, media = emptyList(), user = UserBean(name = "user1", avatar = AvatarBean(url = "url1"))),
                    AnimeCommentBean(id = "2", createdAt = "2023/10/01 13:00", message = "message2", likes = 1, dislikes = 1, media = emptyList(), user = UserBean(name = "user2", avatar = AvatarBean(url = "url2"))),
                    AnimeCommentBean(id = "3", createdAt = "2023/10/01 14:00", message = "message3", likes = 1, dislikes = 1, media = emptyList(), user = UserBean(name = "user3", avatar = AvatarBean(url = "url3"))),
                )
                every { mockApi.requestComments(any(), any()) } returns flowOf(respVo)
                apiModel.requestComments("animeId")
                    .test {
                        awaitItem() shouldBe expected
                        awaitComplete()
                    }
            }
            scenario("Get comments fail") {
                val error = Throwable("fail!")
                every { mockApi.requestComments(any(), any()) } returns flow { throw error }
                val apiModel = AnimeoneApiModel()
                apiModel.requestComments("animeId")
                    .test {
                        awaitError().message shouldBe error.message
                    }
            }
        }

    }
}