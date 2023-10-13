package com.alladin.game.ui._final

import android.os.Bundle
import android.view.View
import com.alladin.game.core.library.GameFragment
import com.alladin.game.databinding.FragmentFinalBinding
import com.alladin.game.domain.SharedPr
import com.alladin.game.ui.alladin_jump.FragmentAlladinJump
import com.alladin.game.ui.other.MainActivity

class FragmentFinal: GameFragment<FragmentFinalBinding>(FragmentFinalBinding::inflate) {
    private val sp by lazy {
        SharedPr(requireContext())
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.menu.setOnClickListener {
            (requireActivity() as MainActivity).navigateBack("menu")
        }

        binding.restart.setOnClickListener {
            (requireActivity() as MainActivity).navigateBack("menu")
            (requireActivity() as MainActivity).navigate(FragmentAlladinJump())
        }

        binding.score.text = arguments?.getInt("SCORE").toString()
        binding.record.text = sp.getRecord().toString()

        binding.coinsTotal.text = sp.getCoins().toString()
        binding.coinsPlus.text = "+" + arguments?.getInt("COINS").toString()
    }
}