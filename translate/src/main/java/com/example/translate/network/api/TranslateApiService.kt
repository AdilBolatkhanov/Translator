package com.example.translate.network.api

import com.example.translate.network.model.TranslatedWord
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val API_KEY = ""

interface TranslateApiService {

    companion object {
        fun create(): TranslateApiService {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://translate.yandex.net/api/v1.5/tr.json/")
                .build()

            return retrofit.create(TranslateApiService::class.java)
        }
    }

    @GET("translate")
    fun translate(
        @Query("key") apiKey: String,
        @Query("text") text: String,
        @Query("lang") lang: String,
        @Query("format") format: String
    ): Single<TranslatedWord>
}