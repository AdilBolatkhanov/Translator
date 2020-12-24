package com.example.common.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Favorites(
    @PrimaryKey
    @ColumnInfo(name = "favorite_id")
    var favoritesID: Int,
    var first: String,
    var second: String
)
