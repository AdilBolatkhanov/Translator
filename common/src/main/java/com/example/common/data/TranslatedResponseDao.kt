package com.example.common.data

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Maybe

@Dao
interface TranslatedResponseDao {
    @Query("Select * from translatedresponse")
    fun getAll(): Maybe<List<TranslatedResponse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(translatedResponse: List<TranslatedResponse>): Completable

    @Delete
    fun delete(translatedResponse: TranslatedResponse): Completable
}