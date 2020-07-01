package com.example.translatorkotlin

import com.example.translate.network.TranslateModule
import com.example.translatorkotlin.module.MainModule
import com.example.cyrillic.CyrillicModule
import com.example.favorites.FavoriteModule
import org.koin.core.module.Module

object KoinModules {
    val modules: List<Module> = listOf(
        MainModule.create(),
        TranslateModule.create(),
        CyrillicModule.create(),
        FavoriteModule.create()
    )
}
