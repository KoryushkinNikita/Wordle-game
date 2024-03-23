package com.example.android.wordle

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Word(
    @PrimaryKey val id: Int,
    val entry: String
)
