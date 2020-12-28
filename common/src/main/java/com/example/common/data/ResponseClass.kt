package com.example.common.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ResponseClass(
    @ColumnInfo(name = "text")
    var text: String,
    @ColumnInfo(name = "is_me")
    var isMe: Boolean
){
    @PrimaryKey(autoGenerate = true)
    var responseId: Int? = null
}