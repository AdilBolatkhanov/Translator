package com.example.translate.network.ui

import android.util.Log
import com.example.common.BasePresenter
import com.example.common.data.*
import com.example.translate.network.api.API_KEY
import com.example.translate.network.api.TranslateApiService
import com.example.translate.network.model.TranslatedWord
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class TranslatePresenter(
    private val translateApiService: TranslateApiService,
    private val translatedResponseDao: TranslatedResponseDao,
    private val favoritesDao: FavoritesDao
) :
    BasePresenter<TranslateContract.View>(),
    TranslateContract.Presenter {

    override fun loadFromNetwork(textToTranslate: String, langDirection: String) {
        translateApiService.translate(API_KEY, textToTranslate, langDirection, "plain")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { translated: TranslatedWord ->
                    view?.showTranslatedWord(translated)
                },
                {
                    Log.d("TranslateFragment", "Error in fetching data from network");
                }
            ).disposeOnCleared()

    }

    override fun getAllDataFromDB() {
        translatedResponseDao.getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { responseList ->
                    view?.showAllDataFromDB(responseList)
                },
                {
                    Log.d("TranslateFragment", "Error in inserting")
                }
            ).disposeOnCleared()
    }

    override fun insertAllDataToDB(list: List<TranslatedResponse>) {
        translatedResponseDao.insertAll(list)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d("TranslateFragment", "Inserted")
            }, {
                Log.d("TranslateFragment", "Error in inserting")
            }).disposeOnCleared()

    }

    override fun insertFavoriteRecordToDB(list: List<Favorites>) {
        favoritesDao.insertAll(list)
    }

    override fun getAllFavoriteDataFromDB(): List<Favorites> {
        return favoritesDao.getAll()
    }

    override fun deleteWordsFromDb(word1: TranslatedResponse, word2: TranslatedResponse) {
        translatedResponseDao.delete(word1)
            .subscribeOn(Schedulers.io())
            .concatWith(
                translatedResponseDao.delete(word2)
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    Log.d("TranslateFragment", "Deletion completed")
                }, {
                    Log.d("TranslateFragment", "Error in deleting")
                }
            ).disposeOnCleared()
    }


}