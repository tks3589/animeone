package com.aaron.chen.animeone.di

import com.aaron.chen.animeone.KSPKoinModule
import com.aaron.chen.animeone.database.DaoModule
import com.aaron.chen.animeone.module.retrofit.RetrofitModule
import org.koin.ksp.generated.module

fun getKoinModuleList(): List<org.koin.core.module.Module> = listOf(
    KSPKoinModule().module,
    RetrofitModule.module,
    DaoModule().module
)