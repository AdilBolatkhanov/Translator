package com.example.cyrillic.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.common.data.Favorites
import com.example.common.data.FavoritesDao
import com.example.common.data.ResponseClass
import com.example.cyrillic.model.KirillizaRepository
import com.example.cyrillic.util.SingleLiveEvent
import kotlinx.coroutines.launch
import mvvm.BaseViewModel
import mvvm.UseCaseResult

class KirillizaViewModel(private val repository: KirillizaRepository): BaseViewModel() {
    private val _response = MutableLiveData<List<ResponseClass>>()
    val response: LiveData<List<ResponseClass>> = _response
     val showError = SingleLiveEvent<String>()

    val favoritesResponse = SingleLiveEvent<List<Favorites>>()

    fun getAllResponses(){
        launch {
            val result = repository.getAll()
            when (result){
                is UseCaseResult.Success -> _response.postValue(result.data)
                is UseCaseResult.Error -> showError.value = result.exception.message
            }
        }
    }

    fun insertAll(list: List<ResponseClass>){
        launch {
            repository.insertAll(list)
        }
    }

    fun insertAllFavorites(list: List<Favorites>){
        launch {
            repository.insertFavorites(list)
        }
    }

    fun getAllFavorites(){
        launch {
            val result = repository.getAllFavorites()
            when (result){
                is UseCaseResult.Success -> favoritesResponse.postValue(result.data)
                is UseCaseResult.Error -> showError.value = result.exception.message
            }
        }
    }

    fun deleteResponse(responseClass: ResponseClass){
        launch {
            repository.delete(responseClass)
        }
    }
}