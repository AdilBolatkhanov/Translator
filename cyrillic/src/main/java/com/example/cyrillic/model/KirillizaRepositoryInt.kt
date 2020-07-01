package com.example.cyrillic.model

import com.example.common.data.Favorites
import com.example.common.data.ResponseClass
import mvvm.UseCaseResult

interface KirillizaRepositoryInt {
    suspend fun getAll(): UseCaseResult<List<ResponseClass>>
    suspend fun insertAll(responseClasses: List<ResponseClass>)
    suspend fun delete(responseClass: ResponseClass)
    suspend fun getAllFavorites(): UseCaseResult<List<Favorites>>
    suspend fun insertFavorites(favorites: List<Favorites>)
}