package com.minar.randomix.adapter

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.minar.randomix.R
import com.minar.randomix.databinding.ItemRecentListBinding
import com.minar.randomix.utilities.RecentUtils
import com.minar.randomix.utilities.getThemeColor

class RecentAdapter(
    private val recentList: List<List<String>>,
    private val onClick: (List<String>) -> Unit,
    private val onLongClick: (Int) -> Unit,
) : RecyclerView.Adapter<RecentAdapter.RecentHolder>() {

    inner class RecentHolder(val binding: ItemRecentListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onClick(recentList[position])
            }
            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onLongClick(position)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentHolder {
        val inflater = LayoutInflater.from(parent.context)
        return RecentHolder(ItemRecentListBinding.inflate(inflater, parent, false))
    }

    // A pinned set is filled like a checked segment, with a pin at its end
    override fun onBindViewHolder(holder: RecentHolder, position: Int) {
        val current = recentList[position]
        val pinned = RecentUtils.isPinned(current)
        val context = holder.itemView.context
        holder.binding.recentCard.setCardBackgroundColor(
            if (pinned) ColorStateList.valueOf(
                getThemeColor(com.google.android.material.R.attr.colorSecondaryContainer, context)
            )
            else context.getColorStateList(R.color.inner_card_background)
        )
        holder.binding.recentText.setTypeface(null, if (pinned) Typeface.BOLD else Typeface.NORMAL)
        holder.binding.recentText.text = RecentUtils.fromOptionList(current)
        holder.binding.recentPin.isVisible = pinned
    }

    override fun getItemCount(): Int = recentList.size
}
