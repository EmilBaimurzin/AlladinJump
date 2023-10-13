package com.alladin.game.ui.settings

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.alladin.game.R
import com.alladin.game.core.library.GameFragment
import com.alladin.game.core.library.shortToast
import com.alladin.game.databinding.FragmentSettingsBinding
import com.alladin.game.domain.SharedPr
import com.alladin.game.ui.other.MainActivity

class FragmentSettings : GameFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {
    private val viewModel: SettingsViewModel by viewModels()
    private val sp by lazy {
        SharedPr(requireContext())
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setCoins()

        if (viewModel.currentCharacter.value!! == 0) {
            viewModel.setCharacter(sp.getCurrentCharacter())
        }

        if (viewModel.currentLamp.value!! == 0) {
            viewModel.setLamp(sp.getCurrentLamp())
        }

        binding.select.setOnClickListener {
            if (viewModel.currentPage.value!!) {
                sp.setCurrentCharacter(viewModel.currentCharacter.value!!)
            } else {
                if (sp.isLampBought(viewModel.currentLamp.value!!)) {
                    sp.setCurrentLamp(viewModel.currentLamp.value!!)
                } else {
                    if (sp.getCoins() >= 200) {
                        sp.setCoins(-200)
                        sp.buyLamp(viewModel.currentLamp.value!!)
                        sp.setCurrentLamp(viewModel.currentLamp.value!!)
                        setCoins()
                    } else {
                        shortToast(requireContext(), "Not enough coins")
                    }
                }
                checkLampPriceVisibility()
            }
        }

        binding.close.setOnClickListener {
            (requireActivity() as MainActivity).navigateBack()
        }

        binding.left.setOnClickListener {
            viewModel.left()
        }

        binding.right.setOnClickListener {
            viewModel.right()
        }

        binding.turnLeft.setOnClickListener {
            viewModel.changePage()
        }

        binding.turnRight.setOnClickListener {
            viewModel.changePage()
        }

        viewModel.currentCharacter.observe(viewLifecycleOwner) {
            if (it == 1) {
                binding.characterImg.setImageResource(R.drawable.player01)
            } else {
                binding.characterImg.setImageResource(R.drawable.player02)
            }
        }

        viewModel.currentLamp.observe(viewLifecycleOwner) {
            binding.lampImg.setImageResource(
                when (it) {
                    1 -> R.drawable.lamp01
                    2 -> R.drawable.lamp02
                    3 -> R.drawable.lamp03
                    4 -> R.drawable.lamp04
                    5 -> R.drawable.lamp05
                    else -> R.drawable.lamp06
                }
            )
            checkLampPriceVisibility()
        }

        viewModel.currentPage.observe(viewLifecycleOwner) {
            binding.apply {
                characterImg.isVisible = it
                lampImg.isVisible = !it
                turnLeft.isVisible = !it
                turnRight.isVisible = it
                selectedOption.text = if (it) "Character" else "Lamp"
            }
            checkLampPriceVisibility()
        }
    }

    private fun setCoins() {
        binding.balance.text = sp.getCoins().toString()
    }

    private fun checkLampPriceVisibility() {
        if (!viewModel.currentPage.value!!) {
            binding.price.isVisible = !sp.isLampBought(viewModel.currentLamp.value!!)
        } else {
            binding.price.isVisible = false
        }
    }
}