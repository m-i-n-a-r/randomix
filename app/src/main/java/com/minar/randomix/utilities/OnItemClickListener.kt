package com.minar.randomix.utilities

import android.view.View

interface OnItemClickListener {
    fun onItemClick(position: Int, recentList: List<String>, view: View)
    fun onItemLongClick(position: Int, view: View)
}
