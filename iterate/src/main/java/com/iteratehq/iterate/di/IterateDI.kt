package com.iteratehq.iterate.di

import android.content.Context
import com.iteratehq.iterate.data.IterateRepository
import com.iteratehq.iterate.data.IterateRepositoryImpl
import com.iteratehq.iterate.data.local.IterateInMemoryCache
import com.iteratehq.iterate.data.local.IterateInMemoryCacheImpl
import com.iteratehq.iterate.data.local.IterateSharedPrefs
import com.iteratehq.iterate.data.local.IterateSharedPrefsImpl

internal class IterateDI(context: Context) {
    private val iterateInMemoryCache: IterateInMemoryCache = IterateInMemoryCacheImpl()
    private val iterateSharedPrefs: IterateSharedPrefs = IterateSharedPrefsImpl(context)

    @JvmSynthetic
    internal val iterateRepository: IterateRepository = IterateRepositoryImpl(
        iterateInMemoryCache,
        iterateSharedPrefs
    )
}
