package com.alladin.game.ui.alladin_jump

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alladin.game.R
import com.alladin.game.core.library.GameFragment
import com.alladin.game.core.library.safe
import com.alladin.game.core.library.shortToast
import com.alladin.game.databinding.FragmentAladinJumpBinding
import com.alladin.game.domain.SharedPr
import com.alladin.game.ui._final.FragmentFinal
import com.alladin.game.ui.other.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentAlladinJump: GameFragment<FragmentAladinJumpBinding>(FragmentAladinJumpBinding::inflate) {
    private val viewModel: AlladinJumpViewModel by viewModels()
    private var moveScope = CoroutineScope(Dispatchers.Default)
    private val lamp by lazy {
        when (sp.getCurrentLamp()) {
            1 -> R.drawable.lamp01
            2 -> R.drawable.lamp02
            3 -> R.drawable.lamp03
            4 -> R.drawable.lamp04
            5 -> R.drawable.lamp05
            else -> R.drawable.lamp06
        }
    }
    private val character by lazy {
        sp.getCurrentCharacter()
    }
    private val sp by lazy {
        SharedPr(requireContext())
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtons()
        setCoins()

        lifecycleScope.launch {
            if (character == 1) {
                binding.player.layoutParams.width = dpToPx(90)
                binding.player.layoutParams.height = LayoutParams.WRAP_CONTENT
                binding.player.setImageResource(R.drawable.player01)
                binding.player.adjustViewBounds = true
            } else {
                binding.player.layoutParams.width = dpToPx(70)
                binding.player.layoutParams.height = LayoutParams.WRAP_CONTENT
                binding.player.setImageResource(R.drawable.player02)
                binding.player.adjustViewBounds = true
            }
        }

        viewModel.coinsCallback = {
            sp.setCoins(1)
            setCoins()
        }

        binding.menu.setOnClickListener {
            (requireActivity() as MainActivity).navigateBack()
        }

        binding.play.setOnClickListener {
            viewModel.pauseState = false
            viewModel.changePause(false)
            viewModel.start(
                xy.x.toInt(),
                binding.linearLayout.y.toInt(),
                binding.linearLayout.y.toInt() + dpToPx(80),
                binding.linearLayout.y.toInt() + dpToPx(80) * 2,
                dpToPx(170),
                binding.player.height,
                binding.player.width,
                dpToPx(35),
                dpToPx(30),
                dpToPx(50),
            )
        }

        binding.pause.setOnClickListener {
            if (!viewModel.isInitial) {
                viewModel.pauseState = true
                viewModel.stop()
                viewModel.stopMoving()
                viewModel.changePause(true)
            } else {
                shortToast(requireContext(), "Game didn't even start yet...")
            }
        }

        viewModel.playerXY.observe(viewLifecycleOwner) {
            binding.player.x = it.x
            binding.player.y = it.y

            if (viewModel.isInitial) {
                binding.initialPlatform.y = it.y + binding.player.height
            }

            lifecycleScope.launch {
                safe {
                    if ((it.x + binding.player.width <= 0 || it.y >= xy.y)) {
                        viewModel.teleportBack(
                            binding.player.height,
                            binding.player.width,
                            dpToPx(170)
                        )
                    }
                }
            }
        }

        viewModel.pause.observe(viewLifecycleOwner) {
            binding.pauseLayout.isVisible = it
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.trigger.collect {
                    binding.platformsLayout.removeAllViews()
                    binding.initialPlatform.isVisible = viewModel.isInitial

                    viewModel.platforms.value.forEach { platform ->
                        val platformView = ImageView(requireContext())
                        platformView.layoutParams = ViewGroup.LayoutParams(dpToPx(170), dpToPx(35))
                        platformView.setImageResource(R.drawable.platform)
                        platformView.x = platform.x
                        platformView.y = platform.y
                        binding.platformsLayout.addView(platformView)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.trigger2.collect {
                    binding.coinsLayout.removeAllViews()
                    viewModel.coins.value.forEach { star ->
                        val starView = ImageView(requireContext())
                        starView.layoutParams = ViewGroup.LayoutParams(dpToPx(30), dpToPx(30))
                        starView.setImageResource(R.drawable.coin)
                        starView.x = star.x
                        starView.y = star.y
                        binding.coinsLayout.addView(starView)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.trigger3.collect {
                    binding.lampsLayout.removeAllViews()
                    viewModel.lamps.value.forEach { lamp ->
                        val lampView = ImageView(requireContext())
                        lampView.layoutParams = ViewGroup.LayoutParams(dpToPx(50), dpToPx(50))
                        lampView.setImageResource(this@FragmentAlladinJump.lamp)
                        lampView.x = lamp.x
                        lampView.y = lamp.y
                        binding.lampsLayout.addView(lampView)
                    }
                }
            }
        }

        viewModel.lives.observe(viewLifecycleOwner) {
            binding.lives.removeAllViews()
            repeat(it) {
                val liveView = ImageView(requireContext())
                liveView.layoutParams = LinearLayout.LayoutParams(dpToPx(8), dpToPx(14))
                liveView.setImageResource(R.drawable.live)
                binding.lives.addView(liveView)
            }

            if (it == 0 && viewModel.gameState && !viewModel.pauseState) {
                viewModel.gameState = false
                viewModel.stop()
                viewModel.stopMoving()

                if (sp.getRecord() < viewModel.points.value!!) {
                    sp.setRecord(viewModel.points.value!!)
                }

                (requireActivity() as MainActivity).navigate(FragmentFinal().apply {
                    arguments = Bundle().apply {
                        putInt("SCORE", viewModel.points.value!!)
                        putInt("COINS", viewModel.coinsCount)
                    }
                })
            }
        }

        viewModel.points.observe(viewLifecycleOwner) {
            binding.score.text = it.toString()
        }

        lifecycleScope.launch {
            delay(10)
            if (viewModel.gameState && !viewModel.pauseState) {
                viewModel.start(
                    xy.x.toInt(),
                    binding.linearLayout.y.toInt(),
                    binding.linearLayout.y.toInt() + dpToPx(80),
                    binding.linearLayout.y.toInt() + dpToPx(80) * 2,
                    dpToPx(170),
                    binding.player.height,
                    binding.player.width,
                    dpToPx(35),
                    dpToPx(30),
                    dpToPx(50),
                )
            }

            if (viewModel.playerXY.value!!.x == 0f) {
                viewModel.initPlayer(dpToPx(50).toFloat(), xy.y / 2 - (binding.player.height / 2))

                delay(6000)
                if (viewModel.isInitial) {
                    viewModel.jump()
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setButtons() {
        binding.buttonLeft.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    moveScope.launch {
                        while (true) {
                            viewModel.isGoingLeft = true
                            delay(2)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    moveScope.launch {
                        while (true) {
                            viewModel.isGoingLeft = true
                            delay(2)
                        }
                    }
                    true
                }

                else -> {
                    viewModel.isGoingLeft = false
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    false
                }
            }
        }

        binding.buttonRight.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    moveScope.launch {
                        while (true) {
                            viewModel.isGoingRight = true
                            delay(2)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    moveScope.launch {
                        while (true) {
                            viewModel.isGoingRight = true
                            delay(2)
                        }
                    }
                    true
                }

                else -> {
                    viewModel.isGoingRight = false
                    moveScope.cancel()
                    viewModel.stopMoving()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    false
                }
            }
        }

        binding.buttonUp.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    moveScope.launch {
                        while (true) {
                            if (viewModel.isStopped) {
                                viewModel.jump()
                            }
                            delay(2)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    moveScope.launch {
                        while (true) {
                            if (viewModel.isStopped) {
                                viewModel.jump()
                            }
                            delay(2)
                        }
                    }
                    true
                }

                else -> {
                    moveScope.cancel()
                    viewModel.stopMoving()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    false
                }
            }
        }

        binding.buttonDown.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    moveScope.launch {
                        while (true) {
                            if (viewModel.isStopped) {
                                viewModel.goDown(dpToPx(80))
                            }
                            delay(2)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    moveScope.launch {
                        while (true) {
                            if (viewModel.isStopped) {
                                viewModel.goDown(dpToPx(80))
                            }
                            delay(2)
                        }
                    }
                    true
                }

                else -> {
                    moveScope.cancel()
                    viewModel.stopMoving()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    false
                }
            }
        }
    }

    fun setCoins() {
        binding.coins.text = sp.getCoins().toString()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
    }
}