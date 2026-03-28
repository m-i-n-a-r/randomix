package com.minar.randomix.fragments

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Animatable2
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.minar.randomix.R
import com.minar.randomix.adapter.RecentAdapter
import com.minar.randomix.utilities.Constants
import com.minar.randomix.utilities.OnItemClickListener
import androidx.core.content.edit

class RouletteBottomSheet(private val roulette: RouletteFragment) : BottomSheetDialogFragment() {

    private var recentList: MutableList<MutableList<String>> = mutableListOf()
    private var sp: SharedPreferences? = null
    private lateinit var adapter: RecentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.roulette_bottom_sheet, container, false)
        sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val recent = sp!!.getString("recent", "")
        recentList = Gson().fromJson(recent, object : TypeToken<MutableList<MutableList<String>>>() {}.type) ?: mutableListOf()

        val recentListLayout = v.findViewById<RecyclerView>(R.id.recentList)
        val rouletteBottomSheet = v.findViewById<ConstraintLayout>(R.id.rouletteBottomSheet)
        populateRecentLayout(recentListLayout, rouletteBottomSheet)

        val noRecentImage = v.findViewById<ImageView>(R.id.recentImage)
        val animatedNoRecent = noRecentImage.drawable
        if (animatedNoRecent is Animatable2) {
            animatedNoRecent.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable) { animatedNoRecent.start() }
            })
            animatedNoRecent.start()
        }

        v.findViewById<Chip>(R.id.lettersChip).setOnClickListener {
            val alphabetList = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".map { it.toString() }.toMutableList()
            roulette.restoreOption(alphabetList)
        }
        v.findViewById<Chip>(R.id.numbersChip).setOnClickListener {
            val numbers = (0..100).map { it.toString() }.toMutableList()
            roulette.restoreOption(numbers)
        }
        return v
    }

    private fun populateRecentLayout(recentListLayout: RecyclerView, rouletteBottomSheet: ConstraintLayout) {
        if (recentList.isEmpty()) {
            rouletteBottomSheet.findViewById<View>(R.id.recentNoResult).visibility = View.VISIBLE
        } else {
            adapter = RecentAdapter(requireContext(), recentList)
            recentListLayout.layoutManager = LinearLayoutManager(requireContext())
            recentListLayout.adapter = adapter
            adapter.setOnItemClickListener(object : OnItemClickListener {
                override fun onItemClick(position: Int, recentList: List<String>, view: View) {
                    roulette.restoreOption(recentList.toMutableList())
                }
                override fun onItemLongClick(position: Int, view: View) {
                    pinRecent(position, requireContext())
                }
            })
            getItemTouchHelper().attachToRecyclerView(recentListLayout)
        }
    }

    private fun getItemTouchHelper(): ItemTouchHelper {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                deleteRecent(viewHolder.adapterPosition, requireContext())
            }
        }
        return ItemTouchHelper(callback)
    }

    fun deleteRecent(index: Int, context: Context) {
        fetchRecentList(context)
        recentList.removeAt(index)
        adapter.notifyItemRemoved(index)
        sp!!.edit { putString("recent", Gson().toJson(recentList)) }
    }

    fun pinRecent(position: Int, context: Context) {
        fetchRecentList(context)
        val item = recentList[position]
        if (!item.contains(Constants.PIN_WORKAROUND_ENTRY)) item.add(Constants.PIN_WORKAROUND_ENTRY)
        else item.remove(Constants.PIN_WORKAROUND_ENTRY)
        adapter.notifyItemChanged(position)
        sp!!.edit { putString("recent", Gson().toJson(recentList)) }
    }

    fun updateRecent(options: List<String>, context: Context) {
        fetchRecentList(context)
        insertInRecent(options)
        sp!!.edit { putString("recent", Gson().toJson(recentList)) }
    }

    fun restoreLatest(context: Context) {
        fetchRecentList(context)
        if (recentList.isNotEmpty()) roulette.restoreOption(recentList.last())
    }

    private fun insertInRecent(newRecent: List<String>) {
        val values = newRecent.toMutableList()
        for (recent in recentList) {
            if (newRecent.size == recent.size || newRecent.size == recent.size - 1) {
                val sortedNew = newRecent.sorted()
                val sortedRecent = recent.toMutableList().also { it.remove(Constants.PIN_WORKAROUND_ENTRY) }.sorted()
                if (sortedNew == sortedRecent) return
            }
        }
        if (recentList.size > 9) {
            val removable = recentList.firstOrNull { !it.contains(Constants.PIN_WORKAROUND_ENTRY) }
            if (removable != null) {
                recentList.remove(removable)
                recentList.add(values)
            }
        } else {
            recentList.add(values)
        }
    }

    private fun fetchRecentList(context: Context) {
        if (recentList.isEmpty()) {
            if (sp == null) sp = PreferenceManager.getDefaultSharedPreferences(context)
            val recent = sp!!.getString("recent", "")
            recentList = Gson().fromJson(recent, object : TypeToken<MutableList<MutableList<String>>>() {}.type) ?: mutableListOf()
        }
    }
}
