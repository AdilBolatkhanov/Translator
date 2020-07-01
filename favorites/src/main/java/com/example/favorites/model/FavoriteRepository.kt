package com.example.favorites.model

import com.example.common.data.Favorites
import com.example.common.data.FavoritesDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mvvm.UseCaseResult

class FavoriteRepository(private val favoritesDao: FavoritesDao):FavoriteRepositoryInt {
    override suspend fun getAll(): UseCaseResult<List<Favorites>> {
        return try {
            val result = withContext(Dispatchers.IO) { favoritesDao.getAll()}
            UseCaseResult.Success(result)
        } catch (ex: Exception) {
            UseCaseResult.Error(ex)
        }
    }

    override suspend fun insertAll(favorites: List<Favorites>) {
        withContext(Dispatchers.IO) { favoritesDao.insertAll(favorites)}
    }

    override suspend fun delete(favorites: Favorites) {
        withContext(Dispatchers.IO) { favoritesDao.delete(favorites)}
    }
}