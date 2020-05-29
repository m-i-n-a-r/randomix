package com.minar.randomix.utilities;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;
import com.minar.randomix.MainActivity;
import com.minar.randomix.R;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Hide the statusbar (necessary because it's white)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Color the navbar like the background
        getWindow().setNavigationBarColor(ContextCompat.getColor(getBaseContext(), R.color.iconGreen));
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.app_name).toUpperCase(),
                getString(R.string.app_intro_description),
                R.drawable.intro_icon,
                getColor(R.color.iconGreen),
                getColor(R.color.textColorPrimaryInverse),
                getColor(R.color.textColorSecondaryInverse),
                R.font.opensans_semibold,
                R.font.opensans_semibold,
                R.drawable.intro_background
        ));
        // OPTIONAL METHODS
        setSkipButtonEnabled(false);
        setIndicatorEnabled(false);
        // Turn vibration on and set intensity.
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
