package com.minar.randomix.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.Animatable2
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.minar.randomix.databinding.RateBottomSheetBinding
import com.minar.randomix.utilities.AppRater

class RateBottomSheet : BottomSheetDialogFragment() {
    private var _binding: RateBottomSheetBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val TAG = "rate_bottom_sheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RateBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val animatedStar = binding.rateImage.drawable
        if (animatedStar is Animatable2) {
            animatedStar.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable) = animatedStar.start()
            })
            animatedStar.start()
        }

        binding.positiveButton.setOnClickListener {
            val context = requireContext()
            AppRater.doNotShowAgain(context, true)
            try {
                startActivity(
                    Intent(Intent.ACTION_VIEW, "market://details?id=${context.packageName}".toUri())
                )
            } catch (_: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        "https://play.google.com/store/apps/details?id=${context.packageName}".toUri()
                    )
                )
            }
            dismiss()
        }
        binding.negativeButton.setOnClickListener { dismiss() }
        binding.neverAgainCheckbox.setOnCheckedChangeListener { _, checked ->
            AppRater.doNotShowAgain(requireContext(), checked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
