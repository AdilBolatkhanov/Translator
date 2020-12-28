package com.example.favorites.model

import androidx.lifecycle.LiveData
import com.example.common.data.Favorites

interface FavoriteRepositoryInt {
    fun getAll(): LiveData<List<Favorites>>
    suspend fun insertAll(favorites: List<Favorites>)
    suspend fun delete(favorites: Favorites)
}