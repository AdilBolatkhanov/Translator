package com.example.favorites.model

import androidx.lifecycle.LiveData
import com.example.common.data.Favorites
import com.example.common.data.FavoritesDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FavoriteRepository(private val favoritesDao: FavoritesDao) : FavoriteRepositoryInt {
    override fun getAll(): LiveData<List<Favorites>> = favoritesDao.getAll()

    override suspend fun insertAll(favorites: List<Favorites>) {
        withContext(Dispatchers.IO) { favoritesDao.insertAll(favorites) }
    }

    override suspend fun delete(favorites: Favorites) {
        withContext(Dispatchers.IO) { favoritesDao.delete(favorites) }
    }
}