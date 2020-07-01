package com.example.translatorkotlin

import android.app.Application
import com.example.common.data.MyDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    lateinit var db: MyDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        db = MyDatabase.getInstance(this)
        instance = this

        startKoin {
            androidContext(this@App)
            modules(KoinModules.modules)
        }
    }

    companion object {
        lateinit var instance: App
    }
}
