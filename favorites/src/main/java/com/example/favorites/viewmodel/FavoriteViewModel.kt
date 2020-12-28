package com.example.favorites.viewmodel

import androidx.lifecycle.LiveData
import com.example.common.data.Favorites
import com.example.favorites.model.FavoriteRepositoryInt
import kotlinx.coroutines.launch
import mvvm.BaseViewModel

class FavoriteViewModel(private val repository: FavoriteRepositoryInt) : BaseViewModel() {
    val response: LiveData<List<Favorites>> = repository.getAll()

    fun insertAll(list: List<Favorites>) {
        launch {
            repository.insertAll(list)
        }
    }

    fun deleteFavorite(favorites: Favorites) {
        launch {
            repository.delete(favorites)
        }
    }
}