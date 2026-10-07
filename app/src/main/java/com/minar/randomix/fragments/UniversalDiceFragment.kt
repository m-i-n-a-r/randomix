package com.minar.randomix.fragments

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.content.edit
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.minar.randomix.R
import com.minar.randomix.activities.MainActivity
import com.minar.randomix.databinding.FragmentUniversalDiceBinding
import com.minar.randomix.utilities.ShakeToThrow
import com.minar.randomix.utilities.addPageInsets
import com.minar.randomix.utilities.animateNextLayoutChange
import kotlin.random.Random

// Available dice types
enum class DiceType(val sides: Int, val iconRes: Int) {
    D4(4, R.drawable.ic_dice_d4),
    D6(6, R.drawable.ic_dice_d6),
    D8(8, R.drawable.ic_dice_d8),
    D10(10, R.drawable.ic_dice_d10),
    D12(12, R.drawable.ic_dice_d12),
    D20(20, R.drawable.ic_dice_d20),
}

class UniversalDiceFragment : Fragment() {
    private var _binding: FragmentUniversalDiceBinding? = null
    private val binding get() = _binding!!
    private lateinit var sp: SharedPreferences
    private var shake: ShakeToThrow? = null

    private var selectedDiceType = DiceType.D6
    private var selectedDiceCount = 1   // 0 = 3 vs 3 mode
    private var isAnimating = false

    // The faces shown by the dice drawn with pips, to bring them back to the start before a throw
    private var lastRisikoResults = listOf<Int>()
    private var lastPipResult = 0

    private val mainActivity get() = activity as? MainActivity
    private val isRisikoMode get() = selectedDiceCount == 0

    // A single D6 shows its face with pips, as a real die. The other types keep the numbers,
    // since some of them share the same silhouette
    private val isPipMode get() = selectedDiceType == DiceType.D6 && selectedDiceCount == 1

    companion object {
        const val RESET_DURATION = 500L
        const val RISIKO_DURATION = 1800L
        const val PIP_DURATION = 1200L
        const val SHAKE_DURATION = 280L
        const val SPIN_DURATION = 1150L

        private val PIP_FACES = intArrayOf(
            R.drawable.dice_1_vector_animation, R.drawable.dice_2_vector_animation,
            R.drawable.dice_3_vector_animation, R.drawable.dice_4_vector_animation,
            R.drawable.dice_5_vector_animation, R.drawable.dice_6_vector_animation,
        )
        private val PIP_RESETS = intArrayOf(
            R.drawable.dice_1_to_start_vector_animation, R.drawable.dice_2_to_start_vector_animation,
            R.drawable.dice_3_to_start_vector_animation, R.drawable.dice_4_to_start_vector_animation,
            R.drawable.dice_5_to_start_vector_animation, R.drawable.dice_6_to_start_vector_animation,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUniversalDiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        binding.diceScroll.addPageInsets()
        if (sp.getBoolean("hide_descriptions", false)) binding.descriptionDice.isVisible = false

        // Restore saved state
        selectedDiceType = runCatching {
            DiceType.valueOf(sp.getString("ud_dice_type", "D6") ?: "D6")
        }.getOrDefault(DiceType.D6)
        selectedDiceCount = sp.getInt("ud_dice_count", 1).coerceIn(0, 10)

        setupDiceTypeToggle()
        setupCountChips()
        applyDiceMode()

        binding.diceDisplayZone.setOnClickListener { mainThrow() }
        if (mainActivity?.shakeAllowed() == true)
            shake = ShakeToThrow(requireContext()) { mainThrow() }
    }

    override fun onResume() {
        super.onResume()
        if (!isAnimating) shake?.start()
    }

    override fun onPause() {
        shake?.stop()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupDiceTypeToggle() {
        val typeMap = mapOf(
            R.id.diceTypeSelection4 to DiceType.D4,
            R.id.diceTypeSelection6 to DiceType.D6,
            R.id.diceTypeSelection8 to DiceType.D8,
            R.id.diceTypeSelection10 to DiceType.D10,
            R.id.diceTypeSelection12 to DiceType.D12,
            R.id.diceTypeSelection20 to DiceType.D20,
        )
        typeMap.entries.firstOrNull { it.value == selectedDiceType }
            ?.let { binding.diceTypeSelection.check(it.key) }

        binding.diceTypeSelection.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            typeMap[checkedId]?.let { type ->
                selectedDiceType = type
                applyDiceMode()
                savePrefs()
            }
        }
    }

    private fun setupCountChips() {
        val chipToCount = mapOf(
            R.id.chipCount1 to 1,
            R.id.chipCount2 to 2,
            R.id.chipCount3 to 3,
            R.id.chipCount4 to 4,
            R.id.chipCount5 to 5,
            R.id.chipCount6 to 6,
            R.id.chipCount7 to 7,
            R.id.chipCount8 to 8,
            R.id.chipCount9 to 9,
            R.id.chipCount10 to 10,
            R.id.chipCountRisiko to 0,
        )
        chipToCount.entries.firstOrNull { it.value == selectedDiceCount }
            ?.let { binding.diceCountChipGroup.check(it.key) }

        binding.diceCountChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val count = chipToCount[checkedIds.firstOrNull()] ?: return@setOnCheckedStateChangeListener
            selectedDiceCount = count
            applyDiceMode()
            savePrefs()
        }
    }

    private fun applyDiceMode() {
        binding.diceContent.animateNextLayoutChange()
        binding.risikoLayout.root.isVisible = isRisikoMode
        binding.universalDiceAnimation.isVisible = !isRisikoMode
        // 3 vs 3 is always played with D6, the type can't be chosen
        binding.diceTypeSelection.isEnabled = !isRisikoMode
        binding.diceResultCards.removeAllViews()
        lastPipResult = 0
        binding.universalDiceAnimation.setImageResource(
            if (isPipMode) PIP_FACES[0] else selectedDiceType.iconRes
        )
    }

    private fun savePrefs() = sp.edit {
        putString("ud_dice_type", selectedDiceType.name)
        putInt("ud_dice_count", selectedDiceCount)
    }

    private fun mainThrow() {
        if (isAnimating || _binding == null) return
        isAnimating = true
        shake?.stop()
        setControlsEnabled(false)
        mainActivity?.vibrate()
        mainActivity?.playSound(4)

        when {
            isRisikoMode -> {
                // Bring the dice back to the initial state before the new throw
                if (lastRisikoResults.isNotEmpty()) {
                    runRisikoResetAnimation()
                    binding.root.postDelayed(::throwRisiko, RESET_DURATION)
                } else throwRisiko()
            }

            isPipMode -> {
                if (lastPipResult != 0) {
                    playAvd(binding.universalDiceAnimation, PIP_RESETS[lastPipResult - 1])
                    binding.root.postDelayed(::throwPips, RESET_DURATION)
                } else throwPips()
            }

            else -> throwNormal()
        }
    }

    private fun endThrow() {
        isAnimating = false
        setControlsEnabled(true)
        if (isResumed) shake?.start()
    }

    // Type and number of dice can't change while the dice are rolling
    private fun setControlsEnabled(enabled: Boolean) {
        binding.diceTypeSelection.isEnabled = enabled && !isRisikoMode
        binding.diceCountChipGroup.children.forEach { it.isEnabled = enabled }
    }

    private fun throwNormal() {
        val results = List(selectedDiceCount) { Random.nextInt(selectedDiceType.sides) + 1 }
        animateSingleDie(binding.universalDiceAnimation) {
            if (_binding == null) return@animateSingleDie
            animateResultText("${getString(R.string.generic_result)} ${results.sum()}")
            buildResultChips(results)
            endThrow()
        }
    }

    private fun throwPips() {
        if (_binding == null) return
        val result = Random.nextInt(6) + 1
        lastPipResult = result
        playAvd(binding.universalDiceAnimation, PIP_FACES[result - 1])
        animateResultText("${getString(R.string.generic_result)} $result")
        binding.root.postDelayed({
            if (_binding == null) return@postDelayed
            endThrow()
        }, PIP_DURATION)
    }

    private fun throwRisiko() {
        if (_binding == null) return
        val results = List(6) { Random.nextInt(6) + 1 }
        risikoDice().forEachIndexed { index, die -> playAvd(die, PIP_FACES[results[index] - 1]) }
        (binding.risikoLayout.diceButtonAnimationVs.drawable as? Animatable)?.start()

        lastRisikoResults = results
        val team1 = results.take(3).sum()
        val team2 = results.drop(3).sum()
        animateResultText("${getString(R.string.generic_result)} $team1 — $team2")

        binding.root.postDelayed({
            if (_binding == null) return@postDelayed
            endThrow()
        }, RISIKO_DURATION)
    }

    private fun risikoDice() = listOf(
        binding.risikoLayout.diceButtonAnimation1,
        binding.risikoLayout.diceButtonAnimation2,
        binding.risikoLayout.diceButtonAnimation3,
        binding.risikoLayout.diceButtonAnimation4,
        binding.risikoLayout.diceButtonAnimation5,
        binding.risikoLayout.diceButtonAnimation6,
    )

    private fun runRisikoResetAnimation() = risikoDice().forEachIndexed { index, die ->
        playAvd(die, PIP_RESETS[lastRisikoResults[index] - 1])
    }

    private fun playAvd(view: ImageView, drawable: Int) {
        view.setImageResource(drawable)
        (view.drawable as? Animatable)?.start()
    }

    // A shake, then a spin while the die lights up in the accent and fades back
    private fun animateSingleDie(view: ImageView, onEnd: () -> Unit) {
        val gray = MaterialColors.getColor(view, com.google.android.material.R.attr.colorOutline)
        val primary = MaterialColors.getColor(view, androidx.appcompat.R.attr.colorPrimary)
        view.setColorFilter(gray, PorterDuff.Mode.SRC_IN)

        ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f, -20f, 16f, -12f, 8f, -4f, 0f).apply {
            duration = SHAKE_DURATION
            interpolator = LinearInterpolator()
            start()
        }

        view.postDelayed({
            if (_binding == null) return@postDelayed
            view.pivotX = view.width / 2f
            // The D4 is a triangle, its center of mass is lower than the center of the icon
            view.pivotY = if (selectedDiceType == DiceType.D4) view.height * (14f / 24f)
            else view.height / 2f

            ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f).apply {
                duration = SPIN_DURATION
                interpolator = DecelerateInterpolator(2f)
                start()
            }
            ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0f, 18f, 0f).apply {
                duration = SPIN_DURATION
                interpolator = DecelerateInterpolator(2f)
                start()
            }
            ValueAnimator.ofArgb(gray, primary, gray).apply {
                duration = SPIN_DURATION
                addUpdateListener {
                    view.setColorFilter(it.animatedValue as Int, PorterDuff.Mode.SRC_IN)
                }
                start()
            }
        }, SHAKE_DURATION)

        view.postDelayed({
            view.pivotX = view.width / 2f
            view.pivotY = view.height / 2f
            view.clearColorFilter()
            onEnd()
        }, SHAKE_DURATION + SPIN_DURATION + 80)
    }

    private fun animateResultText(text: String) {
        binding.resultDice.startAnimation(AlphaAnimation(1f, 0f).apply { duration = 300 })
        binding.resultDice.postDelayed({
            if (_binding == null) return@postDelayed
            binding.resultDice.text = text
            binding.resultDice.isSelected = true
            binding.resultDice.startAnimation(AlphaAnimation(0f, 1f).apply { duration = 400 })
        }, 300)
    }

    // One chip per die, the highest faces stand out
    private fun buildResultChips(results: List<Int>) {
        binding.diceResultCards.removeAllViews()
        if (results.size <= 1) return
        results.forEachIndexed { index, value ->
            val chip = layoutInflater.inflate(
                R.layout.dice_result_chip, binding.diceResultCards, false
            ) as Chip
            chip.text = value.toString()
            chip.isChecked = value == selectedDiceType.sides
            chip.alpha = 0f
            binding.diceResultCards.addView(chip)
            chip.animate().alpha(1f).setStartDelay(index * 55L).setDuration(220).start()
        }
    }
}
