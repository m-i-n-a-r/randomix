package com.minar.randomix.fragments

import android.content.SharedPreferences
import android.graphics.drawable.Animatable2
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.minar.randomix.R
import com.minar.randomix.databinding.MagicBallBottomSheetBinding

// The custom answers of the magic ball, saved as soon as they change: the ball reads them on
// every throw, so nothing has to be handed back
class MagicBallBottomSheet : BottomSheetDialogFragment() {
    private var _binding: MagicBallBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var sp: SharedPreferences
    private val answers = mutableListOf<String>()

    companion object {
        const val TAG = "magic_ball_bottom_sheet"
        const val MAX_ANSWERS = 100
        const val CHIP_EXIT_DURATION = 400L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MagicBallBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        answers.clear()
        answers.addAll(MagicBallFragment.loadCustomAnswers(sp))

        val animatedImage = binding.customAnswersImage.drawable
        if (animatedImage is Animatable2) {
            animatedImage.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable) = animatedImage.start()
            })
            animatedImage.start()
        }

        binding.customAnswerTextLayout.setEndIconOnClickListener { insertTypedAnswer() }
        binding.customAnswerText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_DONE) return@setOnEditorActionListener false
            insertTypedAnswer()
            true
        }

        binding.customAnswerSwitch.isChecked = sp.getBoolean("custom_answers_active", false)
        binding.customAnswerSwitch.setOnCheckedChangeListener { _, _ -> save() }

        answers.forEach(::addAnswerChip)
        updateState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun insertTypedAnswer() {
        val answer = binding.customAnswerText.text.toString().trim()
            .replace("\\s+".toRegex(), " ")
            .replace(";", "")
        binding.customAnswerText.setText("")
        if (answer.isEmpty() || answers.size >= MAX_ANSWERS) return
        answers.add(answer)
        addAnswerChip(answer)
        updateState()
        save()
    }

    private fun addAnswerChip(answer: String) {
        val chip = layoutInflater.inflate(
            R.layout.custom_chip, binding.customAnswerChipGroup, false
        ) as Chip
        chip.text = answer
        chip.setOnClickListener { removeAnswerChip(chip) }
        chip.setOnCloseIconClickListener { removeAnswerChip(chip) }
        binding.customAnswerChipGroup.addView(chip)
        chip.startAnimation(AnimationUtils.loadAnimation(context, R.anim.chip_enter_anim))
    }

    private fun removeAnswerChip(chip: Chip) {
        if (chip.getTag(R.id.tag_chip_removing) == true) return
        chip.setTag(R.id.tag_chip_removing, true)
        answers.remove(chip.text.toString())
        chip.startAnimation(AnimationUtils.loadAnimation(context, R.anim.chip_exit_anim))
        chip.postDelayed({ _binding?.customAnswerChipGroup?.removeView(chip) }, CHIP_EXIT_DURATION)
        updateState()
        save()
    }

    // The custom answers can only be used when there are enough of them to choose from
    private fun updateState() {
        val enough = answers.size >= MagicBallFragment.MIN_CUSTOM_ANSWERS
        binding.customAnswerSwitch.isEnabled = enough
        if (!enough) binding.customAnswerSwitch.isChecked = false
        binding.customAnswersEmptyPlaceholder.isVisible = answers.isEmpty()
    }

    private fun save() = sp.edit {
        putBoolean("custom_answers_active", binding.customAnswerSwitch.isChecked)
        putString("custom_answers", answers.joinToString(";") + ";")
    }
}
