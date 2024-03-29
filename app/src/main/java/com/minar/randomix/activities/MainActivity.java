package com.minar.randomix.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.elevation.SurfaceColors;
import com.minar.randomix.R;
import com.minar.randomix.utilities.AppRater;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        NavController navController;

        // Retrieve the shared preferences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        if (!sp.getBoolean("first", false)) {
            SharedPreferences.Editor editor = sp.edit();
            // Set default accent
            switch (Build.VERSION.SDK_INT) {
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                    editor.putString("accent_color", "blue");
                    break;
                case 31:
                    editor.putString("accent_color", "system");
                    break;
                default:
                    editor.putString("accent_color", "monet");
                    break;
            }
            editor.putBoolean("first", true);
            editor.apply();
            Intent intent = new Intent(this, IntroActivity.class); // Call the AppIntro activity
            startActivity(intent);
            finish();
        }

        String accent = sp.getString("accent_color", "blue");
        String theme = sp.getString("theme_color", "system");
        String lastItem = sp.getString("last_page", "roulette");

        // Set the base theme and the accent
        switch (theme) {
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }

        // Monet on Android 12+ only, system on Android 11+
        switch (accent) {
            case "monet":
                setTheme(R.style.AppTheme_Monet);
                break;
            case "system":
                setTheme(R.style.AppTheme_System);
                break;
            case "blue":
                setTheme(R.style.AppTheme_Blue);
                break;
            case "green":
                setTheme(R.style.AppTheme_Green);
                break;
            case "aqua":
                setTheme(R.style.AppTheme_Aqua);
                break;
            case "orange":
                setTheme(R.style.AppTheme_Orange);
                break;
            case "yellow":
                setTheme(R.style.AppTheme_Yellow);
                break;
            case "teal":
                setTheme(R.style.AppTheme_Teal);
                break;
            case "violet":
                setTheme(R.style.AppTheme_Violet);
                break;
            case "pink":
                setTheme(R.style.AppTheme_Pink);
                break;
            case "lightBlue":
                setTheme(R.style.AppTheme_LightBlue);
                break;
            case "red":
                setTheme(R.style.AppTheme_Red);
                break;
            case "lime":
                setTheme(R.style.AppTheme_Lime);
                break;
            case "crimson":
                setTheme(R.style.AppTheme_Crimson);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the bottom navigation bar and configure it for the navigation plugin
        BottomNavigationView navigation = findViewById(R.id.navigation);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        navController = Objects.requireNonNull(navHostFragment).getNavController();

        NavOptions options = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setEnterAnim(R.anim.nav_enter_anim)
                .setExitAnim(R.anim.nav_exit_anim)
                .setPopEnterAnim(R.anim.nav_pop_enter_anim)
                .setPopExitAnim(R.anim.nav_pop_exit_anim)
                .setPopUpTo(R.id.nav_graph, true)
                .build();
        // Only way to get the animations back at the moment
        navigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigationRoulette) {
                // Default starting page
                sp.edit().putString("last_page", "roulette").apply();
                navController.navigate(R.id.navigationRoulette, null, options);
            }
            if (item.getItemId() == R.id.navigationCoin) {
                sp.edit().putString("last_page", "coin").apply();
                navController.navigate(R.id.navigationCoin, null, options);
            }
            if (item.getItemId() == R.id.navigationMagicBall) {
                sp.edit().putString("last_page", "magicBall").apply();
                navController.navigate(R.id.navigationMagicBall, null, options);
            }
            if (item.getItemId() == R.id.navigationDice) {
                sp.edit().putString("last_page", "dice").apply();
                navController.navigate(R.id.navigationDice, null, options);
            }
            if (item.getItemId() == R.id.navigationSettings) {
                sp.edit().putString("last_page", "settings").apply();
                navController.navigate(R.id.navigationSettings, null, options);
            }
            return true;
        });
        navigation.setOnItemReselectedListener(item -> {
            // Just do nothing when an item is reselected from the bottom navigation bar
        });
        // Reopen the last selected tab
        switch (lastItem) {
            case "coin":
                navigation.setSelectedItemId(R.id.navigationCoin);
                break;
            case "dice":
                navigation.setSelectedItemId(R.id.navigationDice);
                break;
            case "magicBall":
                navigation.setSelectedItemId(R.id.navigationMagicBall);
                break;
            case "settings":
                navigation.setSelectedItemId(R.id.navigationSettings);
                break;
        }

        // Navigation bar management (if executed before, it doesn't work)
        if (accent.equals("monet")) {
            DynamicColors.applyToActivityIfAvailable(this);
            getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));
        }

        // Rating stuff
        AppRater.appLaunched(this);
    }

    // Some utility functions, used from every fragment connected to this activity
    public void vibrate() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Vibrator vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        if (sp.getBoolean("vibration", true))
            // Vibrate if the vibration in options is set to on
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
        if (mp != null) {
            mp.start();
            mp.setOnCompletionListener(MediaPlayer::release);
        }
    }

    public boolean shakeAllowed() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getBoolean("shake", false);
    }
}