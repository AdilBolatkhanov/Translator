package com.example.translate.network.model

data class TranslatedWord(
    val code: Int,
    val lang: String,
    val text: List<String>
)