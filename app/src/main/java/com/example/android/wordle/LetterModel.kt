package com.example.android.wordle

data class LetterModel(var letter: String, val state: LetterModelState = LetterModelState.DEFAULT)

enum class LetterModelState {
    DEFAULT, IN_WORD, IN_PLACE, NOT_IN_WORD
}

enum class GameStatus {
    LOSE, WIN, WRONG_WORD, DEFAULT
}