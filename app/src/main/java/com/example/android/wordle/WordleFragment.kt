package com.example.android.wordle

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.wordle.databinding.WordleFragmentBinding

class WordleFragment : Fragment() {

    private lateinit var binding: WordleFragmentBinding
    private lateinit var viewModel: WordleViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.wordle_fragment, container, false)
        val wordleViewModelFactory = WordleViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, wordleViewModelFactory).get(WordleViewModel::class.java)

        initialise_widgets()
        return binding.root
    }


    fun initialise_widgets() {
        val lettersDisplayed = listOf(
            listOf(binding.r00, binding.r01, binding.r02, binding.r03, binding.r04),
            listOf(binding.r10, binding.r11, binding.r12, binding.r13, binding.r14),
            listOf(binding.r20, binding.r21, binding.r22, binding.r23, binding.r24),
            listOf(binding.r30, binding.r31, binding.r32, binding.r33, binding.r34),
            listOf(binding.r40, binding.r41, binding.r42, binding.r43, binding.r44),
            listOf(binding.r50, binding.r51, binding.r52, binding.r53, binding.r54)
        )


        for ((rowIndex, row) in viewModel.field.withIndex()) {
            for ((colIndex, element) in row.withIndex()) {
                element.observe(viewLifecycleOwner) {
                    lettersDisplayed[rowIndex][colIndex].text = it.letter
                    val color = when (it.state) {
                        LetterModelState.DEFAULT -> Color.WHITE
                        LetterModelState.IN_WORD -> Color.YELLOW
                        LetterModelState.IN_PLACE -> Color.GREEN
                        LetterModelState.NOT_IN_WORD -> Color.RED
                    }
                    lettersDisplayed[rowIndex][colIndex].setBackgroundColor(color)
                }
            }
        }

        val lettersEnterButtons = listOf(
            binding.a,
            binding.b,
            binding.c,
            binding.d,
            binding.e,
            binding.f,
            binding.g,
            binding.h,
            binding.i,
            binding.j,
            binding.k,
            binding.l,
            binding.m,
            binding.n,
            binding.o,
            binding.p,
            binding.r,
            binding.q,
            binding.s,
            binding.t,
            binding.u,
            binding.v,
            binding.w,
            binding.x,
            binding.y,
            binding.z
        )

        lettersEnterButtons.forEach { button ->
            button.setOnClickListener {
                viewModel.setLetter(button.text.toString())
            }
        }

        binding.del.setOnClickListener {
            viewModel.deleteLast()
        }

        viewModel.gameStatus.observe(viewLifecycleOwner) {
            val text = when (it) {
                GameStatus.LOSE -> resources.getString(R.string.you_lose)
                GameStatus.WIN -> resources.getString(R.string.you_win)
                GameStatus.WRONG_WORD -> resources.getString(R.string.invalid_word)
                GameStatus.DEFAULT -> ""
                else -> ""
            }

            binding.messageField.text = text
            if (it == GameStatus.WIN || it == GameStatus.LOSE) {
                binding.restart.visibility = View.VISIBLE
            }
        }

        binding.restart.setOnClickListener {
            viewModel.restart()
        }


        binding.enter.setOnClickListener {
            viewModel.checkGuess()
        }

    }

}
