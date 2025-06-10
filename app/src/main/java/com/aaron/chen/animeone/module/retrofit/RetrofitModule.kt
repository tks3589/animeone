package com.aaron.chen.animeone.module.retrofit

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
object RetrofitModule: KoinComponent {
    const val MAX_REQUESTS_PER_HOST: Int = 10

    private lateinit var okHttpClient: OkHttpClient

    @Single(binds = [IEtRetrofitApi::class])
    fun getInstance(applicationContext: Context): IEtRetrofitApi {
        return initApi(applicationContext)
    }

    private fun initApi(context: Context): IEtRetrofitApi {
        val TIMEOUT_IN_SECS = 30L

        val okhttpClientBuilder = OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .readTimeout(TIMEOUT_IN_SECS, TimeUnit.SECONDS)
            .connectTimeout(TIMEOUT_IN_SECS, TimeUnit.SECONDS)


        okHttpClient = okhttpClientBuilder.build()
        okHttpClient.dispatcher().maxRequestsPerHost = MAX_REQUESTS_PER_HOST

        val gson = GsonBuilder().create()

        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(IEtRetrofitApi::class.java)
    }
}