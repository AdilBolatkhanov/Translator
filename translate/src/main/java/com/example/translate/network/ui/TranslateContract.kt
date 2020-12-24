package com.example.translate.network.ui

import com.example.common.data.Favorites
import com.example.common.data.TranslatedResponse
import com.example.common.mvp.MvpPresenter
import com.example.common.mvp.MvpView
import com.example.translate.network.model.TranslatedWord

interface TranslateContract {
    interface View : MvpView {
        fun showTranslatedWord(translatedWord: TranslatedWord)
        fun showAllDataFromDB(list: List<TranslatedResponse>)

    }

    interface Presenter : MvpPresenter<View> {
        fun loadFromNetwork(textToTranslate: String, langDirection: String)
        fun getAllDataFromDB()
        fun insertAllDataToDB(list: List<TranslatedResponse>)
        fun insertFavoriteRecordToDB(list: List<Favorites>)
        fun getAllFavoriteDataFromDB(): List<Favorites>
        fun deleteWordsFromDb(word1: TranslatedResponse, word2: TranslatedResponse)
    }
}