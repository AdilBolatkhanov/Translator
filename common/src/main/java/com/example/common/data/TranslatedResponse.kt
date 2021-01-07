package com.example.common.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TranslatedResponse(
    @ColumnInfo(name = "text")
    var text: String,
    @ColumnInfo(name = "is_me")
    var isMe: Boolean
){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "response_id")
    var responseId: Int? = null
}
