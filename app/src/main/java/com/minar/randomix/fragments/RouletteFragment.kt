package com.minar.randomix.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Animatable
import android.graphics.drawable.Animatable2
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.minar.randomix.R
import com.minar.randomix.activities.MainActivity
import com.minar.randomix.utilities.Constants
import com.minar.randomix.utilities.ShakeEventListener
import java.util.Random

class RouletteFragment : Fragment(), View.OnClickListener, View.OnLongClickListener,
    TextView.OnEditorActionListener {

    private var act: MainActivity? = null
    private var shakeEnabled = false
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorListener: ShakeEventListener
    private val options = mutableListOf<String>()
    private val bottomSheet = RouletteBottomSheet(this)
    private var inRangeMode = false
    private lateinit var optionText: EditText
    private lateinit var rangeMin: EditText
    private lateinit var rangeMax: EditText
    private lateinit var result: TextView
    private lateinit var rangeSwitch: SwitchCompat
    private lateinit var chipList: ChipGroup
    private lateinit var rangeArea: LinearLayout
    private lateinit var standardArea: LinearLayout
    private var sp: SharedPreferences? = null

    override fun onResume() {
        super.onResume()
        if (shakeEnabled) sensorManager.registerListener(
            sensorListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onPause() {
        if (shakeEnabled) sensorManager.unregisterListener(sensorListener)
        saveState()
        super.onPause()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_roulette, container, false)
        sp = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if (sp!!.getBoolean("hide_descriptions", false))
            v.findViewById<View>(R.id.descriptionRoulette).visibility = View.GONE

        val insert = v.findViewById<ImageView>(R.id.insertButton)
        val recent = v.findViewById<ImageView>(R.id.recentButton)
        val spin = v.findViewById<ImageView>(R.id.buttonSpinRoulette)
        optionText = v.findViewById(R.id.entryRoulette)
        rangeMin = v.findViewById(R.id.rangeMinRoulette)
        rangeMax = v.findViewById(R.id.rangeMaxRoulette)
        result = v.findViewById(R.id.resultRoulette)
        rangeSwitch = v.findViewById(R.id.rangeSwitchRoulette)
        chipList = v.findViewById(R.id.rouletteChipList)
        rangeArea = v.findViewById(R.id.rangeOptionRoulette)
        standardArea = v.findViewById(R.id.textOptionRoulette)
        val range = v.findViewById<ImageView>(R.id.animatedRangeRoulette)

        val animatedRange = range.drawable
        if (animatedRange is Animatable2) {
            animatedRange.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable) { animatedRange.start() }
            })
            animatedRange.start()
        }

        insert.setOnClickListener(this)
        recent.setOnClickListener(this)
        recent.setOnLongClickListener(this)
        spin.setOnClickListener(this)
        spin.setOnLongClickListener(this)
        optionText.setOnEditorActionListener(this)

        rangeSwitch.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                result.textSize = 62f
                rangeArea.visibility = View.VISIBLE
                standardArea.visibility = View.GONE
                chipList.visibility = View.INVISIBLE
                inRangeMode = true
            } else {
                result.textSize = 36f
                rangeArea.visibility = View.GONE
                standardArea.visibility = View.VISIBLE
                chipList.visibility = View.VISIBLE
                inRangeMode = false
            }
        }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreState()
    }

    private fun saveState() {
        val currentMin = if (::rangeMin.isInitialized) rangeMin.text.toString() else ""
        val currentMax = if (::rangeMax.isInitialized) rangeMax.text.toString() else ""
        sp?.edit {
            putBoolean("roulette_range_mode", inRangeMode)
            putString("roulette_range_min", currentMin)
            putString("roulette_range_max", currentMax)
            putString("roulette_options", Gson().toJson(options))
        }
    }

    private fun restoreState() {
        val savedRangeMode = sp?.getBoolean("roulette_range_mode", false) ?: false
        val savedMin = sp?.getString("roulette_range_min", "") ?: ""
        val savedMax = sp?.getString("roulette_range_max", "") ?: ""
        val savedOptionsJson = sp?.getString("roulette_options", null)

        if (savedRangeMode) rangeSwitch.isChecked = true

        if (savedMin.isNotEmpty()) rangeMin.setText(savedMin)
        if (savedMax.isNotEmpty()) rangeMax.setText(savedMax)

        if (!savedOptionsJson.isNullOrEmpty()) {
            val savedOptions: List<String> = Gson().fromJson(
                savedOptionsJson,
                object : TypeToken<List<String>>() {}.type
            ) ?: emptyList()
            for (option in savedOptions) insertRouletteChip(option, false)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onLongClick(v: View): Boolean {
        (activity as? MainActivity)?.vibrate()
        when (v.id) {
            R.id.recentButton -> {
                bottomSheet.restoreLatest(requireContext())
                return true
            }
            R.id.buttonSpinRoulette -> {
                if (!inRangeMode) {
                    if (options.isEmpty()) {
                        insertRouletteChip(getString(R.string.generic_option) + "1", true)
                        insertRouletteChip(getString(R.string.generic_option) + "2", true)
                        insertRouletteChip(getString(R.string.generic_option) + "3", true)
                    } else {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(android.R.string.dialog_alert_title))
                            .setMessage(getString(R.string.delete_all_roulette_confirmation))
                            .setPositiveButton(getString(android.R.string.ok)) { _, _ -> removeAllChips() }
                            .setNegativeButton(getString(android.R.string.cancel)) { _, _ -> }
                            .show()
                    }
                } else {
                    if (rangeMin.text.isNotEmpty() || rangeMax.text.isNotEmpty()) {
                        rangeMin.setText(""); rangeMax.setText("")
                    } else {
                        rangeMin.setText("1"); rangeMax.setText("10")
                    }
                }
                return true
            }
        }
        return true
    }

    override fun onClick(v: View) {
        val view = requireView()
        val recentAnimation = view.findViewById<ImageView>(R.id.recentButton)
        when (v.id) {
            R.id.recentButton -> {
                (recentAnimation.drawable as? Animatable)?.start()
                (activity as? MainActivity)?.vibrate()
                if (!bottomSheet.isAdded)
                    bottomSheet.show(childFragmentManager, "roulette_bottom_sheet")
            }
            R.id.insertButton -> {
                val insertAnimation = requireView().findViewById<ImageView>(R.id.insertButton)
                (insertAnimation.drawable as? Animatable)?.start()
                (activity as? MainActivity)?.vibrate()
                (activity as? MainActivity)?.playSound(1)
                insertRouletteChip("", true)
            }
            R.id.buttonSpinRoulette -> mainThrow()
        }
    }

    private fun mainThrow() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)

        val spinAnimation = requireView().findViewById<ImageView>(R.id.buttonSpinRoulette)
        val optionsList = requireView().findViewById<ChipGroup>(R.id.rouletteChipList)
        val recentAnimation = requireView().findViewById<ImageView>(R.id.recentButton)

        var minValue = -1; var maxValue = -1
        if (!inRangeMode) {
            if (options.size < 2) {
                Toast.makeText(context, getString(R.string.no_entry_roulette), Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            try {
                minValue = rangeMin.text.toString().toInt()
                maxValue = rangeMax.text.toString().toInt()
            } catch (_: Exception) {}
            if (minValue == -1 || maxValue == -1 || minValue >= maxValue) {
                Toast.makeText(context, getString(R.string.wrong_range_roulette), Toast.LENGTH_SHORT).show()
                return
            }
        }

        if (shakeEnabled) sensorManager.unregisterListener(sensorListener)
        recentAnimation.isClickable = false; recentAnimation.isLongClickable = false
        spinAnimation.isClickable = false; spinAnimation.isLongClickable = false
        rangeSwitch.isClickable = false

        val childCount = optionsList.childCount
        for (i in 0 until childCount) (optionsList.getChildAt(i) as? Chip)?.isClickable = false

        (spinAnimation.drawable as? Animatable)?.start()
        act?.vibrate(); act?.playSound(1)

        val ran = Random()
        val n = if (inRangeMode) ran.nextInt(maxValue - minValue + 1) + minValue
                else ran.nextInt(options.size).also { bottomSheet.updateRecent(options, requireContext()) }

        val animIn = AlphaAnimation(1f, 0f).apply { duration = 1500 }
        val animOut = AlphaAnimation(0f, 1f).apply { duration = 1000 }
        result.startAnimation(animIn)

        requireView().postDelayed({
            if (shakeEnabled) sensorManager.registerListener(
                sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI
            )
            result.text = if (inRangeMode) n.toString() else options[n]
            result.startAnimation(animOut)
            spinAnimation.isClickable = true; spinAnimation.isLongClickable = true
            recentAnimation.isClickable = true; recentAnimation.isLongClickable = true
            rangeSwitch.isClickable = true
            for (i in 0 until childCount) (optionsList.getChildAt(i) as? Chip)?.isClickable = true
            val removeLast = sp?.getBoolean("remove_last", false) ?: false
            if (removeLast && !inRangeMode) removeChip(optionsList.getChildAt(n) as? Chip)
        }, 1500)
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId in listOf(EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_SEND)) {
            val insertAnimation = requireView().findViewById<ImageView>(R.id.insertButton)
            (insertAnimation.drawable as? Animatable)?.start()
            insertRouletteChip("", true)
            return true
        }
        return false
    }

    private fun insertRouletteChip(option: String, limitNumber: Boolean) {
        val allowEquals = sp?.getBoolean("allow_equals", false) ?: false
        val currentOption = option.ifEmpty {
            val t = optionText.text.toString().trim().replace("\\s+".toRegex(), " ")
            if ((!allowEquals && options.contains(t)) || t.isEmpty()) return
            optionText.setText("")
            t
        }
        val optionsList = requireView().findViewById<ChipGroup>(R.id.rouletteChipList)
        if (options.size > 29 && limitNumber) {
            Toast.makeText(context, getString(R.string.too_much_entries_roulette), Toast.LENGTH_SHORT).show()
            return
        }
        options.add(currentOption)

        val chip = LayoutInflater.from(activity).inflate(R.layout.custom_chip, optionsList, false) as Chip
        chip.text = currentOption
        chip.id = options.size
        optionsList.addView(chip)
        chip.startAnimation(AnimationUtils.loadAnimation(context, R.anim.chip_enter_anim))
        chip.setOnClickListener { removeChip(chip) }
    }

    private fun removeChip(chip: Chip?) {
        if (chip == null) return
        val optionsList = requireView().findViewById<ChipGroup>(R.id.rouletteChipList)
        val spinAnimation = requireView().findViewById<ImageView>(R.id.buttonSpinRoulette)
        options.remove(chip.text.toString())
        spinAnimation.isClickable = false; spinAnimation.isLongClickable = false
        chip.startAnimation(AnimationUtils.loadAnimation(context, R.anim.chip_exit_anim))
        chip.postDelayed({
            optionsList.removeView(chip)
            spinAnimation.isClickable = true; spinAnimation.isLongClickable = true
        }, 400)
    }

    private fun removeAllChips() {
        val optionsList = requireView().findViewById<ChipGroup>(R.id.rouletteChipList)
        val childCount = optionsList.childCount
        for (i in 0 until childCount) removeChip(optionsList.getChildAt(i) as? Chip)
    }

    fun restoreOption(option: List<String>) {
        removeAllChips()
        for (item in option) {
            if (item == Constants.PIN_WORKAROUND_ENTRY) continue
            insertRouletteChip(item, false)
        }
    }
}
