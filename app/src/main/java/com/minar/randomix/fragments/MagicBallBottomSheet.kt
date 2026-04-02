package com.minar.randomix.fragments

import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.drawable.Animatable2
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout
import com.minar.randomix.R
import androidx.core.content.edit

class MagicBallBottomSheet(private val magicBall: MagicBallFragment) : BottomSheetDialogFragment() {

    private lateinit var sp: SharedPreferences
    private lateinit var answerText: EditText
    private val loadedAnswers = mutableListOf<String>()
    private lateinit var answerChips: ChipGroup
    private lateinit var placeholder: TextView
    private lateinit var customAnswersSwitch: SwitchCompat

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.magic_ball_bottom_sheet, container, false)
        sp = PreferenceManager.getDefaultSharedPreferences(requireContext())

        val customActive = sp.getBoolean("custom_answers_active", false)
        val customAnswers = sp.getString("custom_answers", "") ?: ""
        loadedAnswers.clear()
        loadedAnswers.addAll(customAnswers.split(";").filter { it.isNotEmpty() })
        placeholder = v.findViewById(R.id.customAnswersEmptyPlaceholder)

        val noRecentImage = v.findViewById<ImageView>(R.id.recentImage)
        val animatedNoRecent = noRecentImage.drawable
        if (animatedNoRecent is Animatable2) {
            animatedNoRecent.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable) { animatedNoRecent.start() }
            })
            animatedNoRecent.start()
        }

        answerChips = v.findViewById(R.id.customAnswerChipGroup)
        answerText = v.findViewById(R.id.customAnswerText)
        val answerTextLayout = v.findViewById<TextInputLayout>(R.id.customAnswerTextLayout)
        answerTextLayout.setEndIconOnClickListener { insertAnswerChip("") }
        answerText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId in listOf(EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_SEND)) {
                insertAnswerChip(""); true
            } else false
        }

        customAnswersSwitch = v.findViewById(R.id.customAnswerSwitch)
        customAnswersSwitch.isChecked = customActive
        customAnswersSwitch.isEnabled = loadedAnswers.size >= 3
        customAnswersSwitch.setOnCheckedChangeListener { _, checked ->
            if (checked) magicBall.setCustomAnswers(loadedAnswers.toTypedArray())
            else magicBall.setCustomAnswers(null)
        }

        managePlaceholder()
        loadedAnswers.forEach { insertAnswerChip(it) }

        return v
    }

    private fun insertAnswerChip(answer: String) {
        val currentAnswer = answer.ifEmpty {
            val t = answerText.text.toString().trim().replace("\\s+".toRegex(), " ")
            answerText.setText("")
            t
        }
        if (loadedAnswers.size > 100 || currentAnswer.isEmpty() || currentAnswer == " ") return
        if (answer.isEmpty()) loadedAnswers.add(currentAnswer)
        if (loadedAnswers.size > 2) {
            customAnswersSwitch.isEnabled = true
            if (customAnswersSwitch.isChecked) magicBall.setCustomAnswers(loadedAnswers.toTypedArray())
        }
        managePlaceholder()

        val chip = LayoutInflater.from(activity).inflate(R.layout.custom_chip, answerChips, false) as Chip
        chip.text = currentAnswer
        chip.id = loadedAnswers.size
        answerChips.addView(chip)
        chip.startAnimation(AnimationUtils.loadAnimation(context, R.anim.chip_enter_anim))
        chip.setOnClickListener { removeChip(chip) }
    }

    private fun removeChip(chip: Chip) {
        loadedAnswers.remove(chip.text.toString())
        if (loadedAnswers.size < 2) {
            customAnswersSwitch.isEnabled = false
            magicBall.setCustomAnswers(null)
        }
        chip.startAnimation(AnimationUtils.loadAnimation(context, R.anim.chip_exit_anim))
        managePlaceholder()
        chip.postDelayed({ answerChips.removeView(chip) }, 400)
    }

    private fun managePlaceholder() {
        if (loadedAnswers.isEmpty()) {
            answerChips.visibility = View.GONE
            placeholder.visibility = View.VISIBLE
        } else {
            answerChips.visibility = View.VISIBLE
            placeholder.visibility = View.GONE
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        sp.edit {
            putBoolean("custom_answers_active", customAnswersSwitch.isChecked)
            val answersString = loadedAnswers.joinToString(";") { it.replace(";", "") } + ";"
            putString("custom_answers", answersString)
            if (customAnswersSwitch.isChecked && customAnswersSwitch.isEnabled)
                magicBall.setCustomAnswers(loadedAnswers.toTypedArray())
            else magicBall.setCustomAnswers(null)
        }
        super.onDismiss(dialog)
    }
}
