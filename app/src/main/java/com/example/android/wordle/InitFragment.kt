package com.example.android.wordle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.android.wordle.databinding.FragmentInitBinding
import androidx.navigation.Navigation

class InitFragment : Fragment() {

    private lateinit var binding: FragmentInitBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_init, container, false)
        binding.buttonToGame.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(InitFragmentDirections.actionInitFragmentToWordleFragment())

        }

        return binding.root
    }

}