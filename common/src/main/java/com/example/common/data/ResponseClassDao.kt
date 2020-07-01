package com.example.common.data

import androidx.room.*
import com.example.common.data.ResponseClass
import io.reactivex.Completable
import io.reactivex.Maybe

@Dao
interface ResponseClassDao {
    @Query("Select * from responseclass")
    fun getAll(): List<ResponseClass>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(responseClasses: List<ResponseClass>)

    @Delete
    fun delete(responseClass: ResponseClass)
}
