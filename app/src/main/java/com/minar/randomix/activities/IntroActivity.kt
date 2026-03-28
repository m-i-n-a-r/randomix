package com.minar.randomix.activities

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.github.appintro.model.SliderPagerBuilder
import com.minar.randomix.R

class IntroActivity : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.navigationBarColor = ContextCompat.getColor(this, R.color.iconGreen)

        super.onCreate(savedInstanceState)

        val pageOne = SliderPagerBuilder()
            .title(getString(R.string.app_name).uppercase())
            .description(getString(R.string.app_intro_description))
            .imageDrawable(R.drawable.intro_icon)
            .titleTypefaceFontRes(R.font.opensans_semibold)
            .descriptionTypefaceFontRes(R.font.opensans_semibold)
            .backgroundColorRes(R.color.textColorPrimaryInverse)
            .titleColorRes(R.color.textColorSecondaryInverse)
            .backgroundDrawable(R.drawable.intro_background)
            .build()

        isSkipButtonEnabled = false
        isIndicatorEnabled = false
        showStatusBar(false) // AppIntro handles the full-screen / status-bar hiding
        addSlide(AppIntroFragment.createInstance(pageOne))

        isVibrate = true
        vibrateDuration = 50
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
