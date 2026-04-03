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
import com.google.android.material.navigation.NavigationBarView
import com.minar.randomix.R
import com.minar.randomix.utilities.AppRater

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val sp = PreferenceManager.getDefaultSharedPreferences(this)

        if (!sp.getBoolean("first", false)) {
            sp.edit {
                putString(
                    "accent_color", when (Build.VERSION.SDK_INT) {
                        in 23..31 -> "blue"
                        else -> "monet"
                    }
                )
                putBoolean("first", true)
            }
            super.onCreate(savedInstanceState)
            startActivity(android.content.Intent(this, IntroActivity::class.java))
            finish()
            return
        }

        val accent = sp.getString("accent_color", "blue") ?: "blue"
        val theme = sp.getString("theme_color", "system") ?: "system"
        val lastItem = sp.getString("last_page", "roulette") ?: "roulette"

        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark", "black" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )

        setTheme(
            if (theme == "black") when (accent) {
                "monet" -> R.style.AppTheme_Monet_PerfectDark
                "blue" -> R.style.AppTheme_Blue_PerfectDark
                "green" -> R.style.AppTheme_Green_PerfectDark
                "aqua" -> R.style.AppTheme_Aqua_PerfectDark
                "orange" -> R.style.AppTheme_Orange_PerfectDark
                "yellow" -> R.style.AppTheme_Yellow_PerfectDark
                "teal" -> R.style.AppTheme_Teal_PerfectDark
                "violet" -> R.style.AppTheme_Violet_PerfectDark
                "pink" -> R.style.AppTheme_Pink_PerfectDark
                "lightBlue" -> R.style.AppTheme_LightBlue_PerfectDark
                "red" -> R.style.AppTheme_Red_PerfectDark
                "lime" -> R.style.AppTheme_Lime_PerfectDark
                "crimson" -> R.style.AppTheme_Crimson_PerfectDark
                else -> R.style.AppTheme_Blue_PerfectDark
            } else when (accent) {
                "monet" -> R.style.AppTheme_Monet
                "blue" -> R.style.AppTheme_Blue
                "green" -> R.style.AppTheme_Green
                "aqua" -> R.style.AppTheme_Aqua
                "orange" -> R.style.AppTheme_Orange
                "yellow" -> R.style.AppTheme_Yellow
                "teal" -> R.style.AppTheme_Teal
                "violet" -> R.style.AppTheme_Violet
                "pink" -> R.style.AppTheme_Pink
                "lightBlue" -> R.style.AppTheme_LightBlue
                "red" -> R.style.AppTheme_Red
                "lime" -> R.style.AppTheme_Lime
                "crimson" -> R.style.AppTheme_Crimson
                else -> R.style.AppTheme_Blue
            }
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (accent == "monet") DynamicColors.applyToActivityIfAvailable(this)

        val fragmentContainer =
            findViewById<androidx.fragment.app.FragmentContainerView>(R.id.navHostFragment)
        val navigation = findViewById<NavigationBarView>(R.id.navigation)

        ViewCompat.setOnApplyWindowInsetsListener(fragmentContainer) { view, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(view.paddingLeft, top, view.paddingRight, view.paddingBottom)
            insets
        }

        if (navigation is BottomNavigationView) {
            ViewCompat.setOnApplyWindowInsetsListener(navigation) { view, insets ->
                val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, bottom)
                insets
            }
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(navigation) { view, insets ->
                val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                val left = insets.getInsets(WindowInsetsCompat.Type.displayCutout()).left
                view.setPadding(left, top, view.paddingRight, view.paddingBottom)
                insets
            }
        }

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
                R.id.navigationRoulette -> "roulette"
                R.id.navigationCoin -> "coin"
                R.id.navigationMagicBall -> "magicBall"
                R.id.navigationDice -> "dice"
                R.id.navigationSettings -> "settings"
                else -> null
            }
            pageKey?.let {
                sp.edit { putString("last_page", it) }
                navController.navigate(item.itemId, null, navOptions)
            }
            true
        }
        navigation.setOnItemReselectedListener { }

        when (lastItem) {
            "coin" -> navigation.selectedItemId = R.id.navigationCoin
            "dice" -> navigation.selectedItemId = R.id.navigationDice
            "magicBall" -> navigation.selectedItemId = R.id.navigationMagicBall
            "settings" -> navigation.selectedItemId = R.id.navigationSettings
        }

        AppRater.appLaunched(this)
    }

    // Vibrate using a standard vibration pattern
    // or use system Haptic feedback if vibration is disabled
    fun vibrate() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val active = sp.getBoolean("vibration", true)
        if (!active) return

        // Deprecated for no reason
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                this.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        // Create a short vibration for earlier Android versions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && Build.VERSION.SDK_INT > 26)
            vib.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        // Or use system Haptic feedback if available
        else
            if (Build.VERSION.SDK_INT >= 30 && vib.areEffectsSupported(VibrationEffect.EFFECT_CLICK)[0] == Vibrator.VIBRATION_EFFECT_SUPPORT_YES)
                vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            else
                @Suppress("DEPRECATION")
                vib.vibrate(30)
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
