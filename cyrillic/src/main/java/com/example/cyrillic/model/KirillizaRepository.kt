package com.example.cyrillic.model

import com.example.common.data.Favorites
import com.example.common.data.FavoritesDao
import com.example.common.data.ResponseClass
import com.example.common.data.ResponseClassDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mvvm.UseCaseResult


class KirillizaRepository(private val responseDao: ResponseClassDao, private val favoritesDao: FavoritesDao):KirillizaRepositoryInt  {
    override suspend fun getAll(): UseCaseResult<List<ResponseClass>> {
        return try {
            val result = withContext(Dispatchers.IO) { responseDao.getAll()}
            UseCaseResult.Success(result)
        } catch (ex: Exception) {
            UseCaseResult.Error(ex)
        }
    }

    override suspend fun insertAll(responseClasses: List<ResponseClass>) {
        withContext(Dispatchers.IO) { responseDao.insertAll(responseClasses)}
    }

    override suspend fun delete(responseClass: ResponseClass) {
        withContext(Dispatchers.IO) { responseDao.delete(responseClass)}
    }

    override suspend fun getAllFavorites(): UseCaseResult<List<Favorites>> {
        return try {
            val result = withContext(Dispatchers.IO) { favoritesDao.getAll()}
            UseCaseResult.Success(result)
        } catch (ex: Exception) {
            UseCaseResult.Error(ex)
        }
    }

    override suspend fun insertFavorites(favorites: List<Favorites>) {
        withContext(Dispatchers.IO) { favoritesDao.insertAll(favorites)}
    }
}