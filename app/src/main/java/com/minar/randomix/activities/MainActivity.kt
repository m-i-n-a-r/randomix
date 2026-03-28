package com.minar.randomix.activities

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.minar.randomix.R
import com.minar.randomix.utilities.AppRater

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Edge-to-edge: let content draw behind status bar and navigation bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val sp = PreferenceManager.getDefaultSharedPreferences(this)

        // First-launch: set defaults and show intro
        if (!sp.getBoolean("first", false)) {
            sp.edit {
                putString(
                    "accent_color", when (Build.VERSION.SDK_INT) {
                        in 23..29 -> "blue"
                        31        -> "system"
                        else      -> "monet"
                    }
                )
                putBoolean("first", true)
            }
            super.onCreate(savedInstanceState)
            startActivity(android.content.Intent(this, IntroActivity::class.java))
            finish()
            return
        }

        val accent   = sp.getString("accent_color", "blue")    ?: "blue"
        val theme    = sp.getString("theme_color",  "system")  ?: "system"
        val lastItem = sp.getString("last_page",    "roulette") ?: "roulette"

        // Night mode
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                "light"          -> AppCompatDelegate.MODE_NIGHT_NO
                "dark", "black"  -> AppCompatDelegate.MODE_NIGHT_YES
                else             -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )

        // Theme
        setTheme(
            if (theme == "black") when (accent) {
                "monet"     -> R.style.AppTheme_Monet_PerfectDark
                "system"    -> R.style.AppTheme_System_PerfectDark
                "blue"      -> R.style.AppTheme_Blue_PerfectDark
                "green"     -> R.style.AppTheme_Green_PerfectDark
                "aqua"      -> R.style.AppTheme_Aqua_PerfectDark
                "orange"    -> R.style.AppTheme_Orange_PerfectDark
                "yellow"    -> R.style.AppTheme_Yellow_PerfectDark
                "teal"      -> R.style.AppTheme_Teal_PerfectDark
                "violet"    -> R.style.AppTheme_Violet_PerfectDark
                "pink"      -> R.style.AppTheme_Pink_PerfectDark
                "lightBlue" -> R.style.AppTheme_LightBlue_PerfectDark
                "red"       -> R.style.AppTheme_Red_PerfectDark
                "lime"      -> R.style.AppTheme_Lime_PerfectDark
                "crimson"   -> R.style.AppTheme_Crimson_PerfectDark
                else        -> R.style.AppTheme_Blue_PerfectDark
            } else when (accent) {
                "monet"     -> R.style.AppTheme_Monet
                "system"    -> R.style.AppTheme_System
                "blue"      -> R.style.AppTheme_Blue
                "green"     -> R.style.AppTheme_Green
                "aqua"      -> R.style.AppTheme_Aqua
                "orange"    -> R.style.AppTheme_Orange
                "yellow"    -> R.style.AppTheme_Yellow
                "teal"      -> R.style.AppTheme_Teal
                "violet"    -> R.style.AppTheme_Violet
                "pink"      -> R.style.AppTheme_Pink
                "lightBlue" -> R.style.AppTheme_LightBlue
                "red"       -> R.style.AppTheme_Red
                "lime"      -> R.style.AppTheme_Lime
                "crimson"   -> R.style.AppTheme_Crimson
                else        -> R.style.AppTheme_Blue
            }
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Monet dynamic colors
        if (accent == "monet") DynamicColors.applyToActivityIfAvailable(this)

        // Insets
        // Status bar → top padding on the fragment container so that fragment
        // content starts below the status bar without touching every fragment layout.
        val fragmentContainer = findViewById<androidx.fragment.app.FragmentContainerView>(R.id.navHostFragment)
        ViewCompat.setOnApplyWindowInsetsListener(fragmentContainer) { view, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(view.paddingLeft, top, view.paddingRight, view.paddingBottom)
            insets
        }

        // Navigation bar → bottom padding on BottomNavigationView so its
        // background fills the gesture/button nav bar area correctly.
        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        ViewCompat.setOnApplyWindowInsetsListener(navigation) { view, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, bottom)
            insets
        }

        // Navigation controller
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setEnterAnim(R.anim.nav_enter_anim)
            .setExitAnim(R.anim.nav_exit_anim)
            .setPopEnterAnim(R.anim.nav_pop_enter_anim)
            .setPopExitAnim(R.anim.nav_pop_exit_anim)
            .setPopUpTo(R.id.nav_graph, true)
            .build()

        navigation.setOnItemSelectedListener { item ->
            val pageKey = when (item.itemId) {
                R.id.navigationRoulette  -> "roulette"
                R.id.navigationCoin      -> "coin"
                R.id.navigationMagicBall -> "magicBall"
                R.id.navigationDice      -> "dice"
                R.id.navigationSettings  -> "settings"
                else                     -> null
            }
            pageKey?.let {
                sp.edit { putString("last_page", it) }
                navController.navigate(item.itemId, null, navOptions)
            }
            true
        }
        navigation.setOnItemReselectedListener { /* no-op */ }

        // Restore last tab
        when (lastItem) {
            "coin"      -> navigation.selectedItemId = R.id.navigationCoin
            "dice"      -> navigation.selectedItemId = R.id.navigationDice
            "magicBall" -> navigation.selectedItemId = R.id.navigationMagicBall
            "settings"  -> navigation.selectedItemId = R.id.navigationSettings
        }

        AppRater.appLaunched(this)
    }

    // Utility helpers called from fragments

    fun vibrate() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        if (!sp.getBoolean("vibration", true)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator.vibrate(
                VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            (getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(30)
        }
    }

    fun playSound(fragmentNumber: Int) {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        if (!sp.getBoolean("sound", false)) return
        val resId = when (fragmentNumber) {
            1 -> R.raw.roulette_sound
            2 -> R.raw.coin_sound
            3 -> R.raw.magicball_sound
            4 -> R.raw.dice_sound
            else -> return
        }
        val mp = MediaPlayer.create(this, resId) ?: return
        mp.start()
        mp.setOnCompletionListener { it.release() }
    }

    fun shakeAllowed(): Boolean =
        PreferenceManager.getDefaultSharedPreferences(this).getBoolean("shake", false)
}
