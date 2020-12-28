package com.example.common.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Favorites(
    var first: String,
    var second: String
){
    @PrimaryKey(autoGenerate = true)
    var favoritesID: Int? = null
}
