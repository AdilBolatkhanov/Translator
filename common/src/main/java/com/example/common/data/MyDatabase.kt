package com.example.common.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ResponseClass::class, Favorites::class, TranslatedResponse::class],
    version = 1,
    exportSchema = false
)
abstract class MyDatabase : RoomDatabase() {
    abstract fun responseClassDao(): ResponseClassDao
    abstract fun favoritesDao(): FavoritesDao
    abstract fun translatedResponseDao(): TranslatedResponseDao

    companion object {
        @Volatile
        private var instance: MyDatabase? = null

        fun getInstance(context: Context): MyDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): MyDatabase =
            Room.databaseBuilder(
                context,
                MyDatabase::class.java,
                "my-database"
            ).build()
    }
}