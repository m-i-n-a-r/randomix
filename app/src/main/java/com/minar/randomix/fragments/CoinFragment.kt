package com.minar.randomix.fragments

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.minar.randomix.R
import com.minar.randomix.activities.MainActivity
import com.minar.randomix.databinding.FragmentCoinBinding
import com.minar.randomix.utilities.ShakeToThrow
import com.minar.randomix.utilities.addPageInsets
import kotlin.random.Random

class CoinFragment : Fragment() {
    private var _binding: FragmentCoinBinding? = null
    private val binding get() = _binding!!
    private var shake: ShakeToThrow? = null
    private var flipping = false
    private var notFirstFlip = false
    private var lastResult = false
    private var streakCount = 0

    private val mainActivity get() = activity as? MainActivity

    companion object {
        const val RESET_DURATION = 500L
        const val FLIP_DURATION = 1500L
        const val COOLDOWN_DURATION = 2000L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        binding.coinScroll.addPageInsets()
        if (sp.getBoolean("hide_descriptions", false)) binding.descriptionCoin.isVisible = false

        binding.coinButtonAnimation.setOnClickListener { mainThrow() }
        if (mainActivity?.shakeAllowed() == true)
            shake = ShakeToThrow(requireContext()) { mainThrow() }
    }

    override fun onResume() {
        super.onResume()
        if (!flipping) shake?.start()
    }

    override fun onPause() {
        shake?.stop()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun mainThrow() {
        if (flipping || _binding == null) return
        flipping = true
        shake?.stop()
        mainActivity?.vibrate()
        mainActivity?.playSound(2)

        // From the second flip on, the coin first goes back to its initial state
        if (notFirstFlip) {
            runResetAnimation()
            binding.root.postDelayed(::flipAndRunMainAnimation, RESET_DURATION)
        } else flipAndRunMainAnimation()
        notFirstFlip = true

        binding.root.postDelayed({
            if (_binding == null) return@postDelayed
            flipping = false
            if (isResumed) shake?.start()
        }, COOLDOWN_DURATION)
    }

    private fun flipAndRunMainAnimation() {
        if (_binding == null) return
        val isHead = Random.nextBoolean()
        val result = getString(if (isHead) R.string.result_head else R.string.result_tail)
        binding.resultCoin.startAnimation(AlphaAnimation(1f, 0f).apply { duration = FLIP_DURATION })

        streakCount = if (streakCount > 0 && isHead == lastResult) streakCount + 1 else 1
        lastResult = isHead

        binding.coinButtonAnimation.setImageResource(
            if (isHead) R.drawable.coin_head_vector_animation
            else R.drawable.coin_tail_vector_animation
        )
        (binding.coinButtonAnimation.drawable as? Animatable)?.start()

        val currentStreak = streakCount
        binding.root.postDelayed({
            if (_binding == null) return@postDelayed
            binding.resultCoin.text = result
            binding.resultCoin.startAnimation(AlphaAnimation(0f, 1f).apply { duration = 1000 })
            streakMessage(currentStreak)?.let { mainActivity?.showSnackbar(it) }
        }, FLIP_DURATION)
    }

    private fun streakMessage(streak: Int): String? = when (streak) {
        3 -> "🔥 Three in a row!"
        5 -> "🤯 FIVE IN A ROW!"
        10 -> "TEN? Seriously? Are you mad bro?"
        20 -> "This is literally impossible. It won't happen. No. Way. You can only see this " +
                "part of the easter egg in the source code. And if you see this: star the repo!"
        else -> null
    }

    private fun runResetAnimation() {
        binding.coinButtonAnimation.setImageResource(
            if (lastResult) R.drawable.coin_head_to_start_vector_animation
            else R.drawable.coin_tail_to_start_vector_animation
        )
        (binding.coinButtonAnimation.drawable as? Animatable)?.start()
    }
}
