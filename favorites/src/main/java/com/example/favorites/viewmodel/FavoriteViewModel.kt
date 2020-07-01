package com.example.favorites.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.common.data.Favorites
import com.example.common.data.ResponseClass
import com.example.favorites.model.FavoriteRepository
import com.example.favorites.util.SingleLiveEvent
import kotlinx.coroutines.launch
import mvvm.BaseViewModel
import mvvm.UseCaseResult

class FavoriteViewModel(private val repository: FavoriteRepository):BaseViewModel() {
    private val _response = MutableLiveData<List<Favorites>>()
    val response: LiveData<List<Favorites>> = _response
    val showError = SingleLiveEvent<String>()

    fun getAllResponses(){
        launch {
            val result = repository.getAll()
            when (result){
                is UseCaseResult.Success -> _response.postValue(result.data)
                is UseCaseResult.Error -> showError.value = result.exception.message
            }
        }
    }

    fun insertAll(list: List<Favorites>){
        launch {
            repository.insertAll(list)
        }
    }

    fun deleteFavorite(favorites: Favorites){
        launch {
            repository.delete(favorites)
        }
    }
}