package com.aaron.chen.animeone

import android.app.Application
import android.util.Log
import com.aaron.chen.animeone.di.getKoinModuleList
import com.aaron.chen.animeone.module.retrofit.IRetrofitApi
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Application: Application() {
    private val logTag = this::class.java.simpleName
    override fun onCreate() {
        super.onCreate()

        initKoin()
        preInitRetrofitApi()
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@Application)
            modules(getKoinModuleList())
        }
    }

    private fun preInitRetrofitApi() {
        get<IRetrofitApi>()
        Log.d(logTag, "[preInitRetrofitApi] pre-init retrofit instance")
    }
}