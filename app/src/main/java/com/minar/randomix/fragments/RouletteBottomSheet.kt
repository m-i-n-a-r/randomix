package com.minar.randomix.fragments

import android.content.SharedPreferences
import android.graphics.drawable.Animatable2
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.minar.randomix.adapter.RecentAdapter
import com.minar.randomix.databinding.RouletteBottomSheetBinding
import com.minar.randomix.utilities.Constants
import com.minar.randomix.utilities.RecentUtils

// Shown by the roulette in its own fragment manager, which is how it reaches it back
class RouletteBottomSheet : BottomSheetDialogFragment() {
    private var _binding: RouletteBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var sp: SharedPreferences
    private lateinit var recentList: MutableList<MutableList<String>>
    private lateinit var adapter: RecentAdapter

    private val roulette get() = parentFragment as? RouletteFragment

    companion object {
        const val TAG = "roulette_bottom_sheet"
        private val LETTERS = ('A'..'Z').map { it.toString() }
        private val NUMBERS = (1..100).map { it.toString() }
        private val CARDS = listOf("♠", "♥", "♦", "♣").flatMap { suit ->
            listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
                .map { value -> "$value$suit" }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RouletteBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        recentList = RecentUtils.load(sp)

        val animatedNoRecent = binding.recentImage.drawable
        if (animatedNoRecent is Animatable2) {
            animatedNoRecent.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable) = animatedNoRecent.start()
            })
            animatedNoRecent.start()
        }

        binding.lettersChip.setOnClickListener { roulette?.restoreOption(LETTERS) }
        binding.numbersChip.setOnClickListener { roulette?.restoreOption(NUMBERS) }
        binding.cardsChip.setOnClickListener { roulette?.restoreOption(CARDS) }

        adapter = RecentAdapter(
            recentList,
            onClick = { options -> roulette?.restoreOption(options) },
            onLongClick = ::togglePin,
        )
        binding.recentList.layoutManager = LinearLayoutManager(requireContext())
        binding.recentList.adapter = adapter
        swipeToDelete().attachToRecyclerView(binding.recentList)
        updatePlaceholder()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun swipeToDelete() = ItemTouchHelper(
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return
                recentList.removeAt(position)
                adapter.notifyItemRemoved(position)
                RecentUtils.save(sp, recentList)
                updatePlaceholder()
            }
        })

    private fun togglePin(position: Int) {
        val item = recentList[position]
        if (RecentUtils.isPinned(item)) item.remove(Constants.PIN_WORKAROUND_ENTRY)
        else item.add(Constants.PIN_WORKAROUND_ENTRY)
        adapter.notifyItemChanged(position)
        RecentUtils.save(sp, recentList)
    }

    private fun updatePlaceholder() {
        binding.recentNoResult.isVisible = recentList.isEmpty()
    }
}
