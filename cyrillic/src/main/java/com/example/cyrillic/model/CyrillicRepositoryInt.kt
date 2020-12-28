package com.example.cyrillic.model

import androidx.lifecycle.LiveData
import com.example.common.data.Favorites
import com.example.common.data.ResponseClass

interface CyrillicRepositoryInt {
    fun getAll(): LiveData<List<ResponseClass>>
    suspend fun insertAll(responseClasses: List<ResponseClass>)
    suspend fun delete(responseClass: ResponseClass)
    suspend fun insertFavorites(favorites: List<Favorites>)
}