package com.minar.randomix.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Animatable2
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.net.toUri
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.checkbox.MaterialCheckBox
import com.minar.randomix.R
import com.minar.randomix.activities.MainActivity

class RateBottomSheet(
    private val activity: MainActivity,
    private val editor: SharedPreferences.Editor
) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.rate_bottom_sheet, container, false)
        val doNotShowAgain = "do_not_show_again"

        val noRecentImage = v.findViewById<ImageView>(R.id.rateImage)
        val animatedNoRecent = noRecentImage.drawable
        if (animatedNoRecent is Animatable2) {
            animatedNoRecent.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable) { animatedNoRecent.start() }
            })
            animatedNoRecent.start()
        }

        v.findViewById<Button>(R.id.positiveButton).setOnClickListener {
            activity.startActivity(
                Intent(Intent.ACTION_VIEW, "market://details?id=${activity.packageName}".toUri())
            )
            editor.putBoolean(doNotShowAgain, true).commit()
            dismiss()
        }
        v.findViewById<Button>(R.id.negativeButton).setOnClickListener { dismiss() }
        v.findViewById<MaterialCheckBox>(R.id.neverAgainCheckbox).setOnCheckedChangeListener { _, b ->
            editor.putBoolean(doNotShowAgain, b).commit()
        }
        return v
    }
}
