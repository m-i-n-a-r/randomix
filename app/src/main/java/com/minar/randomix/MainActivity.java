package com.minar.randomix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pixplicity.generate.Rate;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        NavController navController;

        // getSharedPreferences(MyPrefs, Context.MODE_PRIVATE); retrieves a specific shared preferences file
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sp.getString("theme_color", "system");
        String accent = sp.getString("accent_color", "blue");

        if (!sp.getBoolean("first", false)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("first", true);
            editor.apply();
            Intent intent = new Intent(this, IntroActivity.class); // Call the AppIntro java class
            startActivity(intent);
            finish();
        }

        // Set the base theme and the accent
        if (theme.equals("system"))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (theme.equals("dark"))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        if (theme.equals("light"))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        if (accent.equals("green")) setTheme(R.style.AppTheme_green);
        if (accent.equals("orange")) setTheme(R.style.AppTheme_orange);
        if (accent.equals("yellow")) setTheme(R.style.AppTheme_yellow);
        if (accent.equals("teal")) setTheme(R.style.AppTheme_teal);
        if (accent.equals("violet")) setTheme(R.style.AppTheme_violet);
        if (accent.equals("pink")) setTheme(R.style.AppTheme_pink);
        if (accent.equals("lightBlue")) setTheme(R.style.AppTheme_lightBlue);
        if (accent.equals("red")) setTheme(R.style.AppTheme_red);
        if (accent.equals("lime")) setTheme(R.style.AppTheme_lime);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the bottom navigation bar and configure it for the navigation plugin
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navController = Navigation.findNavController(this, R.id.navHostFragment);
        NavigationUI.setupWithNavController(navigation, navController);

        // Gene-rate configuration and detect if night mode is enabled to set the appropriate theme
        boolean isLight = false;
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                isLight = false;
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                isLight = true;
                break;
        }
        // Use a normal dialog instead of the buggy snackbar. Some texts are never used
        Rate rate = new Rate.Builder(this)
                .setTriggerCount(10)
                .setRepeatCount(10)
                .setMinimumInstallTime(Math.toIntExact(TimeUnit.DAYS.toMillis(3)))
                .setMessage(R.string.rating_message)
                .setPositiveButton(R.string.positive_button)
                .setCancelButton(R.string.cancel_button)
                .setNegativeButton(R.string.feedback_text)
                .setNeverAgainText(R.string.not_again_button)
                .setFeedbackText(R.string.feedback_text)
                .setFeedbackAction(Uri.parse("mailto:" + getResources().getString(R.string.dev_email)))
                .setLightTheme(isLight)
                .build();

        rate.count();
        rate.showRequest();
    }

    // Some utility functions, used from every fragment connected to this activity
    public void vibrate() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Vibrator vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        if (sp.getBoolean("vibration", true))
            // Vibrate if the vibration in options is set to on
            // noinspection ConstantConditions
            vib.vibrate(30);
    }

    public void playSound(int fragmentNumber) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sp.getBoolean("sound", false)) return;
        // Assign the sound depending on the fragment number
        MediaPlayer mp = null;
        if (fragmentNumber == 1) mp = MediaPlayer.create(this, R.raw.roulette_sound);
        if (fragmentNumber == 2) mp = MediaPlayer.create(this, R.raw.coin_sound);
        if (fragmentNumber == 3) mp = MediaPlayer.create(this, R.raw.magicball_sound);
        if (fragmentNumber == 4) mp = MediaPlayer.create(this, R.raw.dice_sound);
        // Play sound if the sound in options is set to on
        // noinspection ConstantConditions
        mp.start();
        mp.setOnCompletionListener(MediaPlayer::release);
    }
}