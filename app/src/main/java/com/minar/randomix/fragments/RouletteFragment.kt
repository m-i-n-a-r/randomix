package com.minar.randomix.fragments

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.drawable.Animatable
import android.graphics.drawable.Animatable2
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.minar.randomix.R
import com.minar.randomix.activities.MainActivity
import com.minar.randomix.databinding.FragmentRouletteBinding
import com.minar.randomix.utilities.RecentUtils
import com.minar.randomix.utilities.ShakeToThrow
import com.minar.randomix.utilities.addPageInsets
import com.minar.randomix.utilities.animateNextLayoutChange
import java.util.Random

class RouletteFragment : Fragment() {
    private var _binding: FragmentRouletteBinding? = null
    private val binding get() = _binding!!
    private lateinit var sp: SharedPreferences
    private var shake: ShakeToThrow? = null
    private val options = mutableListOf<String>()
    private var inRangeMode = false
    private var spinning = false

    private val mainActivity get() = activity as? MainActivity

    companion object {
        const val MAX_OPTIONS = 30
        const val SPIN_DURATION = 1500L
        const val CHIP_EXIT_DURATION = 400L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRouletteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        binding.rouletteScroll.addPageInsets()
        if (sp.getBoolean("hide_descriptions", false)) binding.descriptionRoulette.isVisible = false

        // The range icon loops for as long as it is shown
        val animatedRange = binding.animatedRangeRoulette.drawable
        if (animatedRange is Animatable2) {
            animatedRange.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable) = animatedRange.start()
            })
            animatedRange.start()
        }

        binding.insertButton.setOnClickListener {
            (binding.insertButton.drawable as? Animatable)?.start()
            mainActivity?.vibrate()
            mainActivity?.playSound(1)
            insertRouletteChip("", true)
        }
        binding.recentButton.setOnClickListener {
            (binding.recentButton.drawable as? Animatable)?.start()
            mainActivity?.vibrate()
            if (childFragmentManager.findFragmentByTag(RouletteBottomSheet.TAG) == null)
                RouletteBottomSheet().show(childFragmentManager, RouletteBottomSheet.TAG)
        }
        binding.recentButton.setOnLongClickListener {
            mainActivity?.vibrate()
            RecentUtils.load(sp).lastOrNull()?.let { restoreOption(it) }
            true
        }
        binding.buttonSpinRoulette.setOnClickListener { mainThrow() }
        binding.buttonSpinRoulette.setOnLongClickListener {
            mainActivity?.vibrate()
            fillOrClear()
            true
        }
        binding.deleteAllRoulette.setOnClickListener {
            (binding.deleteAllRoulette.icon as? Animatable)?.start()
            mainActivity?.vibrate()
            confirmDeleteAll()
        }
        binding.entryRoulette.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_DONE) return@setOnEditorActionListener false
            (binding.insertButton.drawable as? Animatable)?.start()
            insertRouletteChip("", true)
            true
        }
        binding.rouletteModeSelection.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            // Restoring the state checks a button too, only a tap is felt
            if (binding.root.isLaidOut) mainActivity?.vibrate()
            applyRangeMode(checkedId == R.id.rouletteModeRange)
        }

        if (mainActivity?.shakeAllowed() == true)
            shake = ShakeToThrow(requireContext()) { mainThrow() }
        restoreState()
    }

    override fun onResume() {
        super.onResume()
        if (!spinning) shake?.start()
    }

    override fun onPause() {
        shake?.stop()
        saveState()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveState() {
        sp.edit {
            putBoolean("roulette_range_mode", inRangeMode)
            putString("roulette_range_min", binding.rangeMinRoulette.text.toString())
            putString("roulette_range_max", binding.rangeMaxRoulette.text.toString())
            putString("roulette_options", Gson().toJson(options))
        }
    }

    // Options or range, the field row above the chips follows
    private fun applyRangeMode(range: Boolean) {
        if (range == inRangeMode && binding.rangeOptionRoulette.isVisible == range) return
        inRangeMode = range
        binding.resultRoulette.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(
                if (range) R.dimen.result_range_text_size else R.dimen.result_text_size
            )
        )
        binding.rouletteContent.animateNextLayoutChange()
        binding.rangeOptionRoulette.isVisible = range
        binding.textOptionRoulette.isVisible = !range
        updateOptionsRow()
    }

    private fun restoreState() {
        binding.rouletteModeSelection.check(
            if (sp.getBoolean("roulette_range_mode", false)) R.id.rouletteModeRange
            else R.id.rouletteModeOptions
        )
        binding.rangeMinRoulette.setText(sp.getString("roulette_range_min", ""))
        binding.rangeMaxRoulette.setText(sp.getString("roulette_range_max", ""))
        val savedOptionsJson = sp.getString("roulette_options", null)
        if (!savedOptionsJson.isNullOrEmpty()) {
            val savedOptions: List<String> = Gson().fromJson(
                savedOptionsJson,
                object : TypeToken<List<String>>() {}.type
            ) ?: emptyList()
            savedOptions.forEach { insertRouletteChip(it, false) }
        }
        updateOptionsRow()
    }

    // A long press on the roulette fills it with generic values when empty, or clears it
    @SuppressLint("SetTextI18n")
    private fun fillOrClear() {
        if (spinning) return
        if (inRangeMode) {
            if (binding.rangeMinRoulette.text.isNullOrEmpty() && binding.rangeMaxRoulette.text.isNullOrEmpty()) {
                binding.rangeMinRoulette.setText("1")
                binding.rangeMaxRoulette.setText("10")
            } else {
                binding.rangeMinRoulette.setText("")
                binding.rangeMaxRoulette.setText("")
            }
        } else if (options.isEmpty()) {
            for (index in 1..3) insertRouletteChip(getString(R.string.generic_option) + index, true)
        } else confirmDeleteAll()
    }

    private fun confirmDeleteAll() {
        if (spinning || options.isEmpty()) return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_all_roulette))
            .setIcon(R.drawable.animated_delete)
            .setMessage(getString(R.string.delete_all_roulette_confirmation))
            .setPositiveButton(getString(android.R.string.ok)) { _, _ -> removeAllChips() }
            .setNegativeButton(getString(android.R.string.cancel), null)
            .show()
    }

    private fun mainThrow() {
        if (spinning || _binding == null) return
        mainActivity?.currentFocus?.clearFocus()
        val imm = requireContext().getSystemService(android.view.inputmethod.InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)

        var minValue = 0
        var maxValue = 0
        if (!inRangeMode) {
            if (options.size < 2) {
                Toast.makeText(context, getString(R.string.no_entry_roulette), Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            val min = binding.rangeMinRoulette.text.toString().toIntOrNull()
            val max = binding.rangeMaxRoulette.text.toString().toIntOrNull()
            if (min == null || max == null || min >= max) {
                Toast.makeText(context, getString(R.string.wrong_range_roulette), Toast.LENGTH_SHORT).show()
                return
            }
            minValue = min
            maxValue = max
        }

        spinning = true
        shake?.stop()
        binding.rouletteModeSelection.isEnabled = false
        binding.deleteAllRoulette.isEnabled = false
        (binding.buttonSpinRoulette.drawable as? Animatable)?.start()
        mainActivity?.vibrate()
        mainActivity?.playSound(1)

        val random = Random()
        val result = if (inRangeMode) (random.nextInt(maxValue - minValue + 1) + minValue).toString()
        else {
            RecentUtils.addRecent(sp, options)
            options[random.nextInt(options.size)]
        }

        binding.resultRoulette.startAnimation(AlphaAnimation(1f, 0f).apply { duration = SPIN_DURATION })
        binding.root.postDelayed({
            if (_binding == null) return@postDelayed
            spinning = false
            if (isResumed) shake?.start()
            binding.resultRoulette.text = result
            binding.resultRoulette.startAnimation(AlphaAnimation(0f, 1f).apply { duration = 1000 })
            binding.rouletteModeSelection.isEnabled = true
            binding.deleteAllRoulette.isEnabled = true
            if (!inRangeMode && sp.getBoolean("remove_last", false))
                chips().firstOrNull { it.text.toString() == result }?.let { removeChip(it) }
        }, SPIN_DURATION)
    }

    // The chips still in the roulette, not those on their way out
    private fun chips() = binding.rouletteChipList.children
        .filterIsInstance<Chip>()
        .filter { it.getTag(R.id.tag_chip_removing) != true }
        .toList()

    private fun insertRouletteChip(option: String, limitNumber: Boolean) {
        val allowEquals = sp.getBoolean("allow_equals", false)
        val currentOption = option.ifEmpty {
            val typed = binding.entryRoulette.text.toString().trim().replace("\\s+".toRegex(), " ")
            if (typed.isEmpty() || (!allowEquals && options.contains(typed))) return
            binding.entryRoulette.setText("")
            typed
        }
        if (options.size >= MAX_OPTIONS && limitNumber) {
            Toast.makeText(context, getString(R.string.too_much_entries_roulette), Toast.LENGTH_SHORT).show()
            return
        }
        options.add(currentOption)

        val chip = layoutInflater.inflate(
            R.layout.custom_chip, binding.rouletteChipList, false
        ) as Chip
        chip.text = currentOption
        chip.setOnClickListener { if (!spinning) removeChip(chip) }
        chip.setOnCloseIconClickListener { if (!spinning) removeChip(chip) }
        binding.rouletteChipList.addView(chip)
        chip.startAnimation(AnimationUtils.loadAnimation(context, R.anim.chip_enter_anim))
        updateOptionsRow()
    }

    private fun removeChip(chip: Chip) {
        if (chip.getTag(R.id.tag_chip_removing) == true) return
        chip.setTag(R.id.tag_chip_removing, true)
        options.remove(chip.text.toString())
        chip.startAnimation(AnimationUtils.loadAnimation(context, R.anim.chip_exit_anim))
        chip.postDelayed({ _binding?.rouletteChipList?.removeView(chip) }, CHIP_EXIT_DURATION)
        updateOptionsRow()
    }

    private fun removeAllChips() = chips().forEach { removeChip(it) }

    // The row of the options shows up with the first one, together with the button deleting
    // them all: a button of its own, not only a long press nobody would find
    private fun updateOptionsRow() {
        val visible = !inRangeMode && options.isNotEmpty()
        if (binding.rouletteChipRow.isVisible != visible)
            binding.rouletteContent.animateNextLayoutChange()
        binding.rouletteChipRow.isVisible = visible
    }

    // Replace the current options with a recent set or a ready made one
    fun restoreOption(newOptions: List<String>) {
        if (spinning) return
        if (inRangeMode) binding.rouletteModeSelection.check(R.id.rouletteModeOptions)
        removeAllChips()
        RecentUtils.withoutPin(newOptions).forEach { insertRouletteChip(it, false) }
    }
}
