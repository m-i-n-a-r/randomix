package com.minar.randomix.fragments

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Animatable
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.color.MaterialColors
import com.minar.randomix.R
import com.minar.randomix.activities.MainActivity
import com.minar.randomix.utilities.ShakeEventListener
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

// Fragments

class UniversalDiceFragment : Fragment() {

    private var act: MainActivity? = null
    private var shakeEnabled = false
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorListener: ShakeEventListener

    private var selectedDiceType = DiceType.D6
    private var selectedDiceCount = 1   // 0 = risiko mode
    private var isAnimating = false

    // Views – bound in onCreateView
    private lateinit var mainDiceImage: ImageView
    private lateinit var risikoLayout: View
    private lateinit var resultText: TextView
    private lateinit var diceTypeGroup: MaterialButtonToggleGroup
    private lateinit var countChipGroup: ChipGroup
    private lateinit var resultCardsContainer: ChipGroup

    // Lifecycle

    override fun onResume() {
        super.onResume()
        if (shakeEnabled) registerShake()
    }

    override fun onPause() {
        if (shakeEnabled) sensorManager.unregisterListener(sensorListener)
        super.onPause()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_universal_dice, container, false)
        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if (sp.getBoolean("hide_descriptions", false))
            v.findViewById<View>(R.id.descriptionDice).visibility = View.GONE

        // Bind views
        val displayZone = v.findViewById<View>(R.id.diceDisplayZone)
        mainDiceImage = v.findViewById(R.id.universalDiceAnimation)
        risikoLayout = v.findViewById(R.id.risikoLayout)
        resultText = v.findViewById(R.id.resultDice)
        diceTypeGroup = v.findViewById(R.id.diceTypeSelection)
        countChipGroup = v.findViewById(R.id.diceCountChipGroup)
        resultCardsContainer = v.findViewById(R.id.diceResultCards)

        risikoLayout.isClickable = false

        // Restore saved state
        selectedDiceType = runCatching {
            DiceType.valueOf(sp.getString("ud_dice_type", "D6") ?: "D6")
        }.getOrDefault(DiceType.D6)
        selectedDiceCount = sp.getInt("ud_dice_count", 1).coerceIn(0, 10)

        setupDiceTypeToggle()
        setupCountChips()
        applyDiceMode()

        displayZone.setOnClickListener { if (!isAnimating) mainThrow() }

        // Shake to throw
        act = activity as? MainActivity
        act?.let { a ->
            shakeEnabled = a.shakeAllowed()
            if (shakeEnabled) {
                sensorManager = a.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                sensorListener = ShakeEventListener().also { it.setOnShakeListener(::mainThrow) }
            }
        }
        return v
    }

    // Type toggle

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
            ?.let { diceTypeGroup.check(it.key) }

        diceTypeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            typeMap[checkedId]?.let { type ->
                selectedDiceType = type
                updateMainDiceImage()
                savePrefs()
            }
        }
    }

    // Count chips

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
            ?.let { countChipGroup.check(it.key) }

        countChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val id = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val count = chipToCount[id] ?: return@setOnCheckedStateChangeListener
            selectedDiceCount = count
            applyDiceMode()
            savePrefs()
        }
    }

    // Mode application
    private fun applyDiceMode() {
        if (isRisikoMode) {
            mainDiceImage.visibility = View.GONE
            risikoLayout.visibility = View.VISIBLE
            // Lock the type selector: risiko is always D6
            diceTypeGroup.isEnabled = false
            diceTypeGroup.alpha = 0.38f
        } else {
            risikoLayout.visibility = View.GONE
            mainDiceImage.visibility = View.VISIBLE
            diceTypeGroup.isEnabled = true
            diceTypeGroup.alpha = 1f
            updateMainDiceImage()
        }
    }

    private val isRisikoMode get() = selectedDiceCount == 0

    private fun updateMainDiceImage() {
        mainDiceImage.setImageResource(selectedDiceType.iconRes)
    }

    private fun savePrefs() {
        PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().apply {
            putString("ud_dice_type", selectedDiceType.name)
            putInt("ud_dice_count", selectedDiceCount)
            apply()
        }
    }

    // Throw

    private fun mainThrow() {
        if (isAnimating) return
        isAnimating = true
        if (shakeEnabled) sensorManager.unregisterListener(sensorListener)
        act?.vibrate()
        act?.playSound(4)

        if (isRisikoMode) throwRisiko()
        else throwNormal()
    }

    private fun throwNormal() {
        val results = List(selectedDiceCount) { Random.nextInt(selectedDiceType.sides) + 1 }
        animateSingleDie(mainDiceImage) {
            if (!isAdded) {
                isAnimating = false; return@animateSingleDie
            }
            showNormalResults(results)
            isAnimating = false
            if (shakeEnabled) registerShake()
        }
    }

    private fun throwRisiko() {
        val results = List(6) { Random.nextInt(6) + 1 }

        val diceViews = listOf(
            R.id.diceButtonAnimation1,
            R.id.diceButtonAnimation2,
            R.id.diceButtonAnimation3,
            R.id.diceButtonAnimation4,
            R.id.diceButtonAnimation5,
            R.id.diceButtonAnimation6,
        ).map { requireView().findViewById<ImageView>(it) }

        val diceDrawables = listOf(
            R.drawable.dice_1_vector_animation,
            R.drawable.dice_2_vector_animation,
            R.drawable.dice_3_vector_animation,
            R.drawable.dice_4_vector_animation,
            R.drawable.dice_5_vector_animation,
            R.drawable.dice_6_vector_animation,
        )

        val vsAnim = requireView().findViewById<ImageView>(R.id.diceButtonAnimationVs)

        results.forEachIndexed { i, value ->
            diceViews[i].setImageResource(diceDrawables[value - 1])
            (diceViews[i].drawable as? Animatable)?.start()
        }
        (vsAnim.drawable as? Animatable)?.start()

        val team1 = results.take(3).sum()
        val team2 = results.drop(3).sum()
        val resultStr = "${getString(R.string.generic_result)} $team1 — $team2"
        animateResultText(resultStr)

        requireView().postDelayed({
            resultCardsContainer.removeAllViews()
            isAnimating = false
            if (shakeEnabled) registerShake()
        }, 1800)
    }

    // Dice animation
    private fun animateSingleDie(view: ImageView, onEnd: () -> Unit) {
        val gray = 0xFF9E9E9E.toInt()
        val primary = MaterialColors.getColor(view, com.google.android.material.R.attr.colorPrimaryFixed)

        val shakeDuration = 280L
        val spinDuration = 1150L
        val totalMs = shakeDuration + spinDuration

        view.setColorFilter(gray, PorterDuff.Mode.SRC_IN)

        ObjectAnimator.ofFloat(view, "translationY", 0f, -20f, 16f, -12f, 8f, -4f, 0f).apply {
            duration = shakeDuration
            interpolator = android.view.animation.LinearInterpolator()
            start()
        }

        view.postDelayed({
            if (!isAdded) return@postDelayed

            view.pivotX = view.width / 2f
            view.pivotY = if (selectedDiceType == DiceType.D4) view.height * (14f / 24f)
            else view.height / 2f

            ObjectAnimator.ofFloat(view, "rotation", 0f, 360f).apply {
                duration = spinDuration
                interpolator = DecelerateInterpolator(2f)
                start()
            }

            ObjectAnimator.ofFloat(view, "translationX", 0f, 18f, 0f).apply {
                duration = spinDuration
                interpolator = DecelerateInterpolator(2f)
                start()
            }

            ValueAnimator.ofArgb(gray, primary).apply {
                duration = spinDuration / 2
                addUpdateListener { view.setColorFilter(it.animatedValue as Int, PorterDuff.Mode.SRC_IN) }
                start()
            }

            view.postDelayed({
                if (!isAdded) return@postDelayed
                ValueAnimator.ofArgb(primary, gray).apply {
                    duration = spinDuration / 2
                    addUpdateListener { view.setColorFilter(it.animatedValue as Int, PorterDuff.Mode.SRC_IN) }
                    start()
                }
            }, spinDuration / 2)

        }, shakeDuration)

        view.postDelayed({
            view.pivotX = view.width / 2f
            view.pivotY = view.height / 2f
            view.clearColorFilter()
            onEnd()
        }, totalMs + 80)
    }

    // Results
    private fun showNormalResults(results: List<Int>) {
        val total = results.sum()
        val resultStr = "${getString(R.string.generic_result)} $total"
        animateResultText(resultStr)
        buildResultChips(results)
    }

    private fun animateResultText(text: String) {
        val animIn = AlphaAnimation(1f, 0f).apply { duration = 300 }
        val animOut = AlphaAnimation(0f, 1f).apply { duration = 400 }
        resultText.startAnimation(animIn)
        resultText.postDelayed({
            resultText.text = text
            resultText.isSelected = true
            resultText.startAnimation(animOut)
        }, 300)
    }

    private fun buildResultChips(results: List<Int>) {
        resultCardsContainer.removeAllViews()
        if (results.size <= 1) return

        val primary = com.google.android.material.R.attr.colorPrimaryFixed
        val surface = com.google.android.material.R.attr.colorSurfaceVariant
        val onPrimary = com.google.android.material.R.attr.colorOnPrimary
        val onSurface = com.google.android.material.R.attr.colorOnSurfaceVariant

        results.forEachIndexed { i, value ->
            val isMax = value == selectedDiceType.sides
            val bgAttr = if (isMax) primary else surface
            val txtAttr = if (isMax) onPrimary else onSurface
            val chip = Chip(ContextThemeWrapper(requireContext(), R.style.Widget_App_Chip_Outline)).apply {
                text = value.toString()
                isCheckable = false
                isClickable = false
                isFocusable = false
                chipStrokeWidth = 0f
                chipBackgroundColor = ColorStateList.valueOf(
                    MaterialColors.getColor(this, bgAttr)
                )
                setTextColor(MaterialColors.getColor(this, txtAttr))
                alpha = 0f
            }
            resultCardsContainer.addView(chip)
            chip.postDelayed({
                if (!isAdded) return@postDelayed
                chip.startAnimation(AlphaAnimation(0f, 1f).apply { duration = 220 })
                chip.alpha = 1f
            }, i * 55L)
        }
    }

    private fun registerShake() {
        sensorManager.registerListener(
            sensorListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
    }
}
