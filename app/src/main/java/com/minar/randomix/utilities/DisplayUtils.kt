package com.minar.randomix.utilities

import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.minar.randomix.R

// The system bars and the display cutout, whichever is larger on each side
private fun WindowInsetsCompat.barsAndCutout(): Insets = Insets.max(
    getInsets(WindowInsetsCompat.Type.systemBars()),
    getInsets(WindowInsetsCompat.Type.displayCutout())
)

// Room around the content of a page. The page runs edge to edge on every side, below the status
// bar and behind the floating navbar or the rail, so an opaque page hides the whole background.
// Its content keeps clear of the system bars, of the cutout, and of the navbar: below it in
// portrait, on its side in landscape, where the navbar becomes a rail. Read again on every
// dispatch, since a rotation keeps the activity and only sends new insets
fun View.addPageInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val inset = insets.barsAndCutout()
        val navbarSpace = view.resources.getDimensionPixelSize(R.dimen.floating_navbar_space)
        val rail = view.resources.getBoolean(R.bool.nav_rail)
        val rtl = view.layoutDirection == View.LAYOUT_DIRECTION_RTL
        val applied = intArrayOf(
            inset.left + if (rail && rtl) navbarSpace else 0,
            inset.top,
            inset.right + if (rail && !rtl) navbarSpace else 0,
            inset.bottom + if (rail) 0 else navbarSpace,
        )
        val last = view.getTag(R.id.tag_page_insets) as? IntArray ?: IntArray(4)
        view.setTag(R.id.tag_page_insets, applied)
        view.setPadding(
            view.paddingLeft - last[0] + applied[0],
            view.paddingTop - last[1] + applied[1],
            view.paddingRight - last[2] + applied[2],
            view.paddingBottom - last[3] + applied[3],
        )
        insets
    }
}

// Apply the insets as margins, removing those applied the time before
fun View.addInsetsByMargin(
    top: Boolean = false,
    bottom: Boolean = false,
    left: Boolean = false,
    right: Boolean = false,
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val inset = insets.barsAndCutout()
        (view.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
            fun update(enabled: Boolean, tag: Int, value: Int, current: Int): Int {
                if (!enabled) return current
                val last = view.getTag(tag) as? Int ?: 0
                view.setTag(tag, value)
                return current - last + value
            }
            params.topMargin =
                update(top, R.id.view_add_insets_margin_top_tag, inset.top, params.topMargin)
            params.bottomMargin = update(
                bottom, R.id.view_add_insets_margin_bottom_tag, inset.bottom, params.bottomMargin
            )
            params.leftMargin =
                update(left, R.id.view_add_insets_margin_left_tag, inset.left, params.leftMargin)
            params.rightMargin =
                update(right, R.id.view_add_insets_margin_right_tag, inset.right, params.rightMargin)
            view.layoutParams = params
        }
        insets
    }
}

private const val LAYOUT_CHANGE_DURATION = 250L

// The next change of visibility or size inside this group slides into place instead of jumping.
// Fades and bounds run together: in sequence, as AutoTransition does, a swap takes three beats.
// Before the first layout there is nothing on screen to animate from, so it is skipped
fun ViewGroup.animateNextLayoutChange() {
    if (!isLaidOut) return
    TransitionManager.beginDelayedTransition(
        this,
        TransitionSet()
            .setOrdering(TransitionSet.ORDERING_TOGETHER)
            .addTransition(Fade())
            .addTransition(ChangeBounds())
            .setDuration(LAYOUT_CHANGE_DURATION)
            .setInterpolator(FastOutSlowInInterpolator())
    )
}

private const val CASCADE_STAGGER = 55L
private const val CASCADE_DURATION = 300L
private const val CASCADE_OFFSET_DP = 18f

// The visible children ride in one after the other
fun ViewGroup.animateChildrenCascade() {
    val density = resources.displayMetrics.density
    children.filter { it.isVisible }.forEachIndexed { index, view ->
        view.alpha = 0f
        view.translationY = CASCADE_OFFSET_DP * density
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(CASCADE_STAGGER * index)
            .setDuration(CASCADE_DURATION)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()
    }
}
