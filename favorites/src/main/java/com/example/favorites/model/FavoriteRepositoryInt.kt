package com.example.favorites.model

import com.example.common.data.Favorites
import com.example.common.data.ResponseClass
import mvvm.UseCaseResult

interface FavoriteRepositoryInt {
    suspend fun getAll(): UseCaseResult<List<Favorites>>
    suspend fun insertAll(favorites: List<Favorites>)
    suspend fun delete(favorites: Favorites)
}