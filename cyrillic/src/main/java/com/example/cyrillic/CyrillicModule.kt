package com.example.cyrillic

import com.example.common.data.FavoritesDao
import com.example.common.data.MyDatabase
import com.example.common.data.ResponseClassDao
import com.example.common.mvp.InjectionModule
import com.example.cyrillic.model.KirillizaRepository
import com.example.cyrillic.viewmodel.KirillizaViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

object CyrillicModule: InjectionModule {
    override fun create(): Module = module {
        fun provideResponseDao(database: MyDatabase): ResponseClassDao{
            return database.responseClassDao()
        }
        fun provideFavoriteDao(database: MyDatabase): FavoritesDao{
            return database.favoritesDao()
        }

        single { provideResponseDao(get()) }
        single { provideFavoriteDao(get()) }
        single{ KirillizaRepository(get(), get()) }

        viewModel { KirillizaViewModel(get()) }
    }
}