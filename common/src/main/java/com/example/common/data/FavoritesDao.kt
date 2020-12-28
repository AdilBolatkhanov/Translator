package com.example.common.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FavoritesDao {
    @Query("select * from favorites")
    fun getAll(): LiveData<List<Favorites>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(favorites: List<Favorites>)

    @Delete
    suspend fun delete(favorites: Favorites)
}