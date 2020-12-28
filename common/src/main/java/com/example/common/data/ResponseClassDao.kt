package com.example.common.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ResponseClassDao {
    @Query("Select * from responseclass")
    fun getAll(): LiveData<List<ResponseClass>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(responseClasses: List<ResponseClass>)

    @Delete
    suspend fun delete(responseClass: ResponseClass)
}
