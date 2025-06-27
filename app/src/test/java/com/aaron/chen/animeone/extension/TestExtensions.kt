package com.aaron.chen.animeone.extension

import io.mockk.mockk

inline fun <reified T : Any> mockkRelaxed(): T = mockk(relaxed = true)