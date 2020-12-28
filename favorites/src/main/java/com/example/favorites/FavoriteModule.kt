package com.example.favorites

import com.example.common.mvp.InjectionModule
import com.example.favorites.model.FavoriteRepository
import com.example.favorites.model.FavoriteRepositoryInt
import com.example.favorites.viewmodel.FavoriteViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

object FavoriteModule: InjectionModule {
    override fun create(): Module = module {
        single<FavoriteRepositoryInt> { FavoriteRepository(get())}
        viewModel { FavoriteViewModel(get()) }
    }
}