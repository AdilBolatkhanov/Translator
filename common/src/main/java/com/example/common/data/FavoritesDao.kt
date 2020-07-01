package com.example.common.data

import androidx.room.*
import com.example.common.data.Favorites

@Dao
interface FavoritesDao {
    @Query("select * from favorites")
    fun getAll(): List<Favorites>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(favorites: List<Favorites>)

    @Delete
    fun delete(favorites: Favorites)
}