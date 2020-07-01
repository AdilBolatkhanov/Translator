package com.example.translate.network.ui

import com.example.common.MvpPresenter
import com.example.common.MvpView
import com.example.common.data.TranslatedResponse
import com.example.translate.network.model.TranslatedWord
import com.example.common.data.Favorites

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