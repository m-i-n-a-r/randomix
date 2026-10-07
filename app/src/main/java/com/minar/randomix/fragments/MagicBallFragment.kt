package com.minar.randomix.fragments

import android.content.SharedPreferences
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
import com.minar.randomix.databinding.FragmentMagicBallBinding
import com.minar.randomix.utilities.ShakeToThrow
import com.minar.randomix.utilities.addPageInsets
import kotlin.random.Random

class MagicBallFragment : Fragment() {
    private var _binding: FragmentMagicBallBinding? = null
    private val binding get() = _binding!!
    private lateinit var sp: SharedPreferences
    private var shake: ShakeToThrow? = null
    private var throwing = false

    private val mainActivity get() = activity as? MainActivity

    companion object {
        const val MIN_CUSTOM_ANSWERS = 2
        const val THROW_DURATION = 1700L

        private val ANSWERS = intArrayOf(
            R.string.magic_answer_1, R.string.magic_answer_2, R.string.magic_answer_3,
            R.string.magic_answer_4, R.string.magic_answer_5, R.string.magic_answer_6,
            R.string.magic_answer_7, R.string.magic_answer_8, R.string.magic_answer_9,
            R.string.magic_answer_10, R.string.magic_answer_11, R.string.magic_answer_12,
            R.string.magic_answer_13, R.string.magic_answer_14, R.string.magic_answer_15,
            R.string.magic_answer_16, R.string.magic_answer_17, R.string.magic_answer_18,
            R.string.magic_answer_19, R.string.magic_answer_20, R.string.magic_answer_21,
            R.string.magic_answer_22, R.string.magic_answer_23, R.string.magic_answer_24,
            R.string.magic_answer_25, R.string.magic_answer_26, R.string.magic_answer_27,
            R.string.magic_answer_28, R.string.magic_answer_29, R.string.magic_answer_30,
        )
        private val RUDE_ANSWERS = intArrayOf(
            R.string.magic_answer_rude_1, R.string.magic_answer_rude_2,
            R.string.magic_answer_rude_3, R.string.magic_answer_rude_4,
            R.string.magic_answer_rude_5, R.string.magic_answer_rude_6,
        )

        // Stored as a single string, each answer followed by a semicolon
        fun loadCustomAnswers(sp: SharedPreferences): List<String> =
            (sp.getString("custom_answers", "") ?: "").split(";").filter { it.isNotEmpty() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMagicBallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        binding.magicBallScroll.addPageInsets()
        if (sp.getBoolean("hide_descriptions", false)) binding.descriptionMagicBall.isVisible = false

        binding.magicBallButtonAnimation.setOnClickListener { mainThrow() }
        binding.magicBallCustomAnswersButton.setOnClickListener {
            (binding.magicBallCustomAnswersButton.icon as? Animatable)?.start()
            mainActivity?.vibrate()
            if (childFragmentManager.findFragmentByTag(MagicBallBottomSheet.TAG) == null)
                MagicBallBottomSheet().show(childFragmentManager, MagicBallBottomSheet.TAG)
        }

        if (mainActivity?.shakeAllowed() == true)
            shake = ShakeToThrow(requireContext()) { mainThrow() }
    }

    override fun onResume() {
        super.onResume()
        if (!throwing) shake?.start()
    }

    override fun onPause() {
        shake?.stop()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // The custom answers when they are on and enough, the standard ones otherwise
    private fun pickAnswer(): String {
        val custom = loadCustomAnswers(sp)
        if (sp.getBoolean("custom_answers_active", false) && custom.size >= MIN_CUSTOM_ANSWERS)
            return custom.random()
        // The default is the same of the switch in the settings
        val pool = if (sp.getBoolean("rude_answers", false)) ANSWERS + RUDE_ANSWERS else ANSWERS
        return getString(pool[Random.nextInt(pool.size)])
    }

    private fun mainThrow() {
        if (throwing || _binding == null) return
        throwing = true
        shake?.stop()
        (binding.magicBallButtonAnimation.drawable as? Animatable)?.start()
        mainActivity?.vibrate()
        mainActivity?.playSound(3)

        val answer = pickAnswer()
        binding.resultMagicBall.startAnimation(AlphaAnimation(1f, 0f).apply { duration = THROW_DURATION })
        binding.root.postDelayed({
            if (_binding == null) return@postDelayed
            throwing = false
            if (isResumed) shake?.start()
            binding.resultMagicBall.text = answer
            binding.resultMagicBall.isSelected = true
            binding.resultMagicBall.startAnimation(AlphaAnimation(0f, 1f).apply { duration = 1000 })
        }, THROW_DURATION)
    }
}
