package com.example.translatorkotlin.module

import com.example.common.InjectionModule
import com.example.common.data.MyDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.module

object MainModule : InjectionModule {
    override fun create(): Module = module {
        single { MyDatabase.getInstance(androidApplication()) }
    }
}
