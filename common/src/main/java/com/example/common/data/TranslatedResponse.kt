package com.example.common.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TranslatedResponse(
    @PrimaryKey
    @ColumnInfo(name = "response_id")
    var responseId: Int,
    @ColumnInfo(name = "text")
    var text: String,
    @ColumnInfo(name = "is_me")
    var isMe: Boolean
)
