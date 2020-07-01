package com.example.common.utility

import com.example.common.data.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

 object Utility {
    private val database = Firebase.database
    fun writeToFBDB(
        id: String,
        name: String,
        email: String,
        url: String,
        countWord: String,
        time: String
    ) {
        val myRef: DatabaseReference = database.getReference("users/$id")
        val user = User(url, name, email, countWord, time)
        myRef.setValue(user)
    }
}