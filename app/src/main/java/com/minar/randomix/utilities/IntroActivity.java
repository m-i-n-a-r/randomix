package com.minar.randomix.utilities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.minar.randomix.MainActivity;
import com.minar.randomix.R;

public class IntroActivity extends AppIntro2 {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Hide the statusbar (necessary because it's white)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Color the navbar like the background
        getWindow().setNavigationBarColor(ContextCompat.getColor(getBaseContext(), R.color.introBackgroundColor));

        super.onCreate(savedInstanceState);

        // Default slide
        // Just set a title, description, background and image. AppIntro will do the rest
        addSlide(AppIntroFragment.newInstance(getString(R.string.app_extended_name).toUpperCase(), getString(R.string.intro_typeface), getString(R.string.app_intro_description), getString(R.string.intro_typeface), R.drawable.icon, getColor(R.color.introBackgroundColor), getColor(R.color.textColorPrimaryInverse), getColor(R.color.textColorSecondaryInverse)));

        // OPTIONAL METHODS
        // Override bar/separator color.
        // Use a conversion from int to hex code string
        setBarColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & getColor(R.color.introBackgroundColor)))));

        // Hide Skip/Done button.
        showSkipButton(false);
        setProgressButtonEnabled(true);
        // Use progress indicator instead of dots
        setProgressIndicator();

        // Turn vibration on and set intensity.
        setVibrate(true);
        setVibrateIntensity(50);

        // End animation
        setSlideOverAnimation();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Intent intent = new Intent(this, MainActivity.class); // Call the Main Activity java class
        startActivity(intent);
        finish();
    }
}
