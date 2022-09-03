package com.minar.randomix.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.minar.randomix.R;
import com.minar.randomix.utilities.AppRater;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        NavController navController;

        // Retrieve the shared preferences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sp.getString("theme_color", "system");
        String accent = sp.getString("accent_color", "system");
        String lastItem = sp.getString("last_page", "roulette");

        if (!sp.getBoolean("first", false)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("first", true);
            editor.apply();
            Intent intent = new Intent(this, IntroActivity.class); // Call the AppIntro activity
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

        switch (accent) {
            case "monet": setTheme(R.style.AppTheme_Monet);
            case "system": setTheme(R.style.AppTheme_System); // Default
        }
        if (accent.equals("monet")) setTheme(R.style.AppTheme_Monet);
        if (accent.equals("system")) setTheme(R.style.AppTheme_System); // Default
        if (accent.equals("blue")) setTheme(R.style.AppTheme_Blue);
        if (accent.equals("green")) setTheme(R.style.AppTheme_Green);
        if (accent.equals("aqua")) setTheme(R.style.AppTheme_Aqua);
        if (accent.equals("orange")) setTheme(R.style.AppTheme_Orange);
        if (accent.equals("yellow")) setTheme(R.style.AppTheme_Yellow);
        if (accent.equals("teal")) setTheme(R.style.AppTheme_Teal);
        if (accent.equals("violet")) setTheme(R.style.AppTheme_Violet);
        if (accent.equals("pink")) setTheme(R.style.AppTheme_Pink);
        if (accent.equals("lightBlue")) setTheme(R.style.AppTheme_LightBlue);
        if (accent.equals("red")) setTheme(R.style.AppTheme_Red);
        if (accent.equals("lime")) setTheme(R.style.AppTheme_Lime);
        if (accent.equals("crimson")) setTheme(R.style.AppTheme_Crimson);

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