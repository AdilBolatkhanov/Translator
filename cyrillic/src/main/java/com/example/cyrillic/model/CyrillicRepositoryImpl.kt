package com.example.cyrillic.model

import androidx.lifecycle.LiveData
import com.example.common.data.Favorites
import com.example.common.data.FavoritesDao
import com.example.common.data.ResponseClass
import com.example.common.data.ResponseClassDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CyrillicRepositoryImpl(
    private val responseDao: ResponseClassDao,
    private val favoritesDao: FavoritesDao
) : CyrillicRepositoryInt {
    override fun getAll(): LiveData<List<ResponseClass>> = responseDao.getAll()

    override suspend fun insertAll(responseClasses: List<ResponseClass>) {
        withContext(Dispatchers.IO) { responseDao.insertAll(responseClasses) }
    }

    override suspend fun delete(responseClass: ResponseClass) {
        withContext(Dispatchers.IO) { responseDao.delete(responseClass) }
    }

    override suspend fun insertFavorites(favorites: List<Favorites>) {
        withContext(Dispatchers.IO) { favoritesDao.insertAll(favorites) }
    }
}