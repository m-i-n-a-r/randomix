package com.minar.randomix.preferences

import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

// A preference whose summary wraps on one more line, or one less, grows or shrinks its tile
// instead of jumping. The row is rebound in place, so the rows below slide on the default move
// while its bottom edge follows them, with the tile background drawn on the row's own bounds
class PreferenceTilesAnimator : DefaultItemAnimator() {

    init {
        // The same holder is rebound, so there's no cross-fade between two copies of the row
        supportsChangeAnimations = false
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder,
        preInfo: ItemHolderInfo,
        postInfo: ItemHolderInfo
    ): Boolean {
        val oldHeight = preInfo.bottom - preInfo.top
        val newHeight = postInfo.bottom - postInfo.top
        if (oldHolder === newHolder && oldHeight != newHeight) {
            val view = newHolder.itemView
            ValueAnimator.ofInt(oldHeight, newHeight).apply {
                duration = moveDuration
                // The curve of the default moves, so the edge and the rows below stay together
                interpolator = AccelerateDecelerateInterpolator()
                // A layout in the middle puts the full height back, the next frame takes over
                addUpdateListener { view.bottom = view.top + it.animatedValue as Int }
            }.start()
        }
        return super.animateChange(oldHolder, newHolder, preInfo, postInfo)
    }
}
