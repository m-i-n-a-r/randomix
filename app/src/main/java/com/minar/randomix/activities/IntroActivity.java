package com.minar.randomix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;
import com.github.appintro.model.SliderPage;
import com.github.appintro.model.SliderPagerBuilder;
import com.minar.randomix.R;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Hide the status bar (necessary because it's white)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Color the navbar like the background
        getWindow().setNavigationBarColor(ContextCompat.getColor(getBaseContext(), R.color.iconGreen));
        super.onCreate(savedInstanceState);

        SliderPage pageOne = new SliderPagerBuilder()
                .title(getString(R.string.app_name).toUpperCase())
                .description(getString(R.string.app_intro_description))
                .imageDrawable(R.drawable.intro_icon)
                .titleTypefaceFontRes(R.font.opensans_semibold)
                .descriptionTypefaceFontRes(R.font.opensans_semibold)
                .backgroundColorRes(R.color.textColorPrimaryInverse)
                .titleColorRes(R.color.textColorSecondaryInverse)
                .backgroundDrawable(R.drawable.intro_background)
                .build();

        setSkipButtonEnabled(false);
        setIndicatorEnabled(false);
        showStatusBar(false);
        addSlide(AppIntroFragment.createInstance(pageOne));

        // Turn vibration on and set intensity
        setVibrate(true);
        setVibrateDuration(50);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Intent intent = new Intent(this, MainActivity.class); // Call the Main Activity java class
        startActivity(intent);
        finish();
    }
}
