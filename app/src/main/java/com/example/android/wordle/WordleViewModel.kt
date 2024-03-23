package com.example.android.wordle

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.Room
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random


class WordleViewModel(private val context: Context) : ViewModel() {

    private lateinit var wordDao: WordDao
    private lateinit var secretWord: String

    private var allWords: MutableList<String> = mutableListOf()

    var field: Array<Array<MutableLiveData<LetterModel>>> =
        Array(6) { Array(5) { MutableLiveData(LetterModel("")) } }

    init {
        retrieveWordsFromDatabase()
    }

    private var _currentRow = MutableLiveData(0)

    private var _currentColumn = MutableLiveData(0)

    private var _gameStatus = MutableLiveData(GameStatus.DEFAULT)
    val gameStatus: LiveData<GameStatus>
        get() = _gameStatus

    private fun gameNotOver() =
        _gameStatus.value != GameStatus.LOSE && _gameStatus.value != GameStatus.WIN

    fun setLetter(letter: String) {
        if (_currentColumn.value != 5 && gameNotOver()) {
            field[_currentRow.value!!][_currentColumn.value!!].value = LetterModel(letter)
            _currentColumn.value = _currentColumn.value!! + 1
        }
    }

    fun deleteLast() {
        if (_currentColumn.value!! > 0 && gameNotOver()) {
            field[_currentRow.value!!][_currentColumn.value!! - 1].value = LetterModel("")
            _currentColumn.value = _currentColumn.value!! - 1
            _gameStatus.value = GameStatus.DEFAULT
        }
    }

    private fun retrieveWordsFromDatabase() {
        val db = Room.databaseBuilder(context, AppDatabase::class.java, "words-database").build()
        wordDao = db.wordDao()

        runBlocking {
            val numOfWords = wordDao.countAll()
            if (numOfWords < 10) {
                readFromFile()
                launch {
                    wordDao.deleteAll()

                    for (i in 0 until allWords.size)
                        wordDao.insertAll(Word(i, allWords[i]))
                }
            } else {
                val list = wordDao.getAll()
                for (w in list)
                    allWords.add(w.entry)
            }
        }
        secretWord = allWords[Random.nextInt(0, allWords.size)]
        Log.d("secret", secretWord)
    }

    private fun readFromFile() {
        val inputStream = context.resources.openRawResource(R.raw.words)
        val bf = BufferedReader(InputStreamReader(inputStream))

        var line: String?
        line = bf.readLine()

        while (line != null) {
            allWords.add(line.trim())

            line = bf.readLine()
        }
    }

    fun checkGuess() {
        if (_currentColumn.value != 5) return

        val guess = field[_currentRow.value!!].joinToString("") { it.value!!.letter }.lowercase()
        if (guess !in allWords) {
            _gameStatus.value = GameStatus.WRONG_WORD
            return
        }

        if (guess == secretWord) {
            _gameStatus.value = GameStatus.WIN
            field[_currentRow.value!!].forEach {
                it.value = LetterModel(it.value!!.letter, LetterModelState.IN_PLACE)
            }
        } else {
            val letterInSecretWordCounters =
                secretWord.groupBy { it }.mapValues { it.value.size }
            val letterInGuessCounters =
                guess.groupBy { it }.mapValues { 0 }
                    .filter { it.key in letterInSecretWordCounters }
                    .toMutableMap()

            for ((index, char) in guess.withIndex()) {
                if (secretWord[index] == char) {
                    field[_currentRow.value!!][index].value = LetterModel(
                        field[_currentRow.value!!][index].value!!.letter,
                        LetterModelState.IN_PLACE
                    )
                    letterInGuessCounters[char] = letterInGuessCounters[char]!! + 1
                }
            }


            for ((index, char) in guess.withIndex()) {
                if (char in secretWord && secretWord[index] != char && letterInGuessCounters[char]!! < letterInSecretWordCounters[char]!!) {
                    field[_currentRow.value!!][index].value = LetterModel(
                        field[_currentRow.value!!][index].value!!.letter,
                        LetterModelState.IN_WORD
                    )
                    letterInGuessCounters[char] = letterInGuessCounters[char]!! + 1
                } else if (field[_currentRow.value!!][index].value!!.state !== LetterModelState.IN_PLACE) {
                    field[_currentRow.value!!][index].value = LetterModel(
                        field[_currentRow.value!!][index].value!!.letter,
                        LetterModelState.NOT_IN_WORD
                    )
                }
            }

            _currentRow.value = _currentRow.value!! + 1
            _currentColumn.value = 0
            if (_currentRow.value == 6) {
                _gameStatus.value = GameStatus.LOSE
            }
        }
    }

    fun restart() {
        _gameStatus.value = GameStatus.DEFAULT
        for (row in field) {
            for (element in row) {
                element.value = LetterModel("")
            }
        }
        secretWord = allWords[Random.nextInt(0, allWords.size)]
        _currentRow.value = 0
        _currentColumn.value = 0
    }
}