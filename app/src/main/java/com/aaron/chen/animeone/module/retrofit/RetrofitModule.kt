package com.aaron.chen.animeone.module.retrofit

import android.content.Context
import com.aaron.chen.animeone.app.model.data.responsevo.AnimeListRespVo
import com.aaron.chen.animeone.app.model.deserializer.AnimeListRespVoDeserializer
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

@Module
object RetrofitModule: KoinComponent {
    const val MAX_REQUESTS_PER_HOST: Int = 10
    const val BASE_URL: String = "https://anime1.me/"

    private lateinit var okHttpClient: OkHttpClient

    @Single
    fun getInstance(applicationContext: Context): IRetrofitApi {
        return initApi(applicationContext)
    }

    private fun initApi(context: Context): IRetrofitApi {
        val TIMEOUT_IN_SECS = 30L

        val okhttpClientBuilder = OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .readTimeout(TIMEOUT_IN_SECS, TimeUnit.SECONDS)
            .connectTimeout(TIMEOUT_IN_SECS, TimeUnit.SECONDS)


        okHttpClient = okhttpClientBuilder.build()
        okHttpClient.dispatcher.maxRequestsPerHost = MAX_REQUESTS_PER_HOST

        val gson = GsonBuilder()
            .registerTypeAdapter(AnimeListRespVo::class.java, AnimeListRespVoDeserializer())
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RetrofitFlowFactory.create())
            .build()

        return retrofit.create(IRetrofitApi::class.java)
    }
}