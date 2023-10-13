package com.alladin.game.ui.beginning

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.alladin.game.R
import com.alladin.game.core.library.GameFragment
import com.alladin.game.databinding.FragmentBeginningBinding
import com.alladin.game.domain.SharedPr
import com.alladin.game.ui.alladin_jump.FragmentAlladinJump
import com.alladin.game.ui.other.MainActivity
import com.alladin.game.ui.settings.FragmentSettings

class FragmentBeginning : GameFragment<FragmentBeginningBinding>(FragmentBeginningBinding::inflate) {
    private val sp by lazy {
        SharedPr(requireContext())
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sp.buyLamp(1)
        binding.play.setOnClickListener {
            (requireActivity() as MainActivity).navigate(FragmentAlladinJump())
        }

        binding.settings.setOnClickListener {
            (requireActivity() as MainActivity).navigate(FragmentSettings())
        }

        binding.exit.setOnClickListener {
            requireActivity().finish()
        }


        binding.privacyText.setOnClickListener {
            requireActivity().startActivity(
                Intent(
                    ACTION_VIEW,
                    Uri.parse("https://www.google.com")
                )
            )
        }
    }
}