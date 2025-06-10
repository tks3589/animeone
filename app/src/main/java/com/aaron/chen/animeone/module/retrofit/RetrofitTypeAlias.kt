package com.aaron.chen.animeone.module.retrofit

import kotlinx.coroutines.flow.Flow
import retrofit2.Response

typealias RetrofitFlow<T> = Flow<Response<T>>