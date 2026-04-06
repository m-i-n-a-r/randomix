package com.minar.randomix.adapter

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.minar.randomix.R
import com.minar.randomix.utilities.Constants
import com.minar.randomix.utilities.OnItemClickListener
import com.minar.randomix.utilities.RecentUtils

class RecentAdapter(
    context: Context,
    private val recentList: List<List<String>>
) : RecyclerView.Adapter<RecentAdapter.RecentHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var listener: OnItemClickListener? = null

    inner class RecentHolder(itemView: View, val adapter: RecentAdapter) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

        val optionList: TextView = itemView.findViewById(R.id.recentText)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View) {
            val position = layoutPosition
            listener?.onItemClick(position, recentList[position], v)
        }

        override fun onLongClick(v: View): Boolean {
            listener?.onItemLongClick(layoutPosition, v)
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentHolder {
        val view = inflater.inflate(R.layout.item_recent_list, parent, false)
        return RecentHolder(view, this)
    }

    override fun onBindViewHolder(holder: RecentHolder, position: Int) {
        val current = recentList[position]
        if (current.contains(Constants.PIN_WORKAROUND_ENTRY)) {
            holder.optionList.setTypeface(null, Typeface.BOLD)
            holder.optionList.setTextColor(getThemeAccentColor(holder.adapter.inflater.context))
        } else {
            holder.optionList.setTypeface(null, Typeface.NORMAL)
            holder.optionList.setTextColor(
                MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorOutline)
            )
        }
        holder.optionList.text = RecentUtils.fromOptionList(current)
    }

    override fun getItemCount(): Int = recentList.size

    fun setOnItemClickListener(l: OnItemClickListener) {
        listener = l
    }

    companion object {
        fun getThemeAccentColor(context: Context): Int {
            val value = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimaryFixed, value, true)
            return value.data
        }
    }
}
