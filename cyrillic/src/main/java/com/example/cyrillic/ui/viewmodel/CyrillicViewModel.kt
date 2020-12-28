package com.example.cyrillic.ui.viewmodel

import androidx.lifecycle.LiveData
import com.example.common.data.Favorites
import com.example.common.data.ResponseClass
import com.example.cyrillic.model.CyrillicRepositoryInt
import com.example.cyrillic.util.SingleLiveEvent
import kotlinx.coroutines.launch
import mvvm.BaseViewModel

class CyrillicViewModel(private val repository: CyrillicRepositoryInt) : BaseViewModel() {
    val response: LiveData<List<ResponseClass>> = repository.getAll()
    val showError = SingleLiveEvent<String>()

    fun insertAll(list: List<ResponseClass>) {
        launch {
            repository.insertAll(list)
        }
    }

    fun insertAllFavorites(positionOfItem: Int) {
        launch {
            val first: String
            val second: String

            response.value?.let { responseMessageList ->
                if (responseMessageList[positionOfItem].isMe) {
                    first = responseMessageList[positionOfItem].text
                    second = responseMessageList[positionOfItem + 1].text
                } else {
                    first = responseMessageList[positionOfItem - 1].text
                    second = responseMessageList[positionOfItem].text
                }
                repository.insertFavorites(listOf(Favorites(first, second)))
            }
        }
    }

    fun deleteResponse(positionOfItem1: Int, positionOfItem2: Int) {
        launch {
            response.value?.let { responseMessageList ->
                val removed1 = responseMessageList[positionOfItem1]
                val removed2 = responseMessageList[positionOfItem2]
                repository.delete(removed1)
                repository.delete(removed2)
            }
        }
    }
}