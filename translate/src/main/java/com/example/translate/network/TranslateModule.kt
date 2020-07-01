package com.example.translate.network

import com.example.common.InjectionModule
import com.example.common.data.FavoritesDao
import com.example.common.data.MyDatabase
import com.example.common.data.TranslatedResponseDao
import com.example.translate.network.api.TranslateApiService
import com.example.translate.network.ui.TranslatePresenter
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

object TranslateModule : InjectionModule {
    override fun create(): Module = module {
        fun provideTranslateDao(database: MyDatabase):TranslatedResponseDao{
            return database.translatedResponseDao()
        }

        viewModel { TranslatePresenter(get(), get(),get()) }
        single { TranslateApiService.create() }
        single { provideTranslateDao(get()) }
    }
}