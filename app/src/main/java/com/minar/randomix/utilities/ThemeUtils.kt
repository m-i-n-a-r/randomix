package com.minar.randomix.utilities

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.google.android.material.color.MaterialColors
import com.minar.randomix.R

// The accent themes are a flat list of styles, each one with an amoled twin, so the accent
// preference has to be mapped to a style by hand. Doing it here keeps the mapping in a single place
@StyleRes
fun accentThemeRes(accent: String?, perfectDark: Boolean): Int = when (accent) {
    "monet" -> if (perfectDark) R.style.AppTheme_Monet_PerfectDark else R.style.AppTheme_Monet
    "aqua" -> if (perfectDark) R.style.AppTheme_Aqua_PerfectDark else R.style.AppTheme_Aqua
    "green" -> if (perfectDark) R.style.AppTheme_Green_PerfectDark else R.style.AppTheme_Green
    "orange" -> if (perfectDark) R.style.AppTheme_Orange_PerfectDark else R.style.AppTheme_Orange
    "yellow" -> if (perfectDark) R.style.AppTheme_Yellow_PerfectDark else R.style.AppTheme_Yellow
    "teal" -> if (perfectDark) R.style.AppTheme_Teal_PerfectDark else R.style.AppTheme_Teal
    "violet" -> if (perfectDark) R.style.AppTheme_Violet_PerfectDark else R.style.AppTheme_Violet
    "pink" -> if (perfectDark) R.style.AppTheme_Pink_PerfectDark else R.style.AppTheme_Pink
    "lightBlue" -> if (perfectDark) R.style.AppTheme_LightBlue_PerfectDark else R.style.AppTheme_LightBlue
    "red" -> if (perfectDark) R.style.AppTheme_Red_PerfectDark else R.style.AppTheme_Red
    "lime" -> if (perfectDark) R.style.AppTheme_Lime_PerfectDark else R.style.AppTheme_Lime
    "crimson" -> if (perfectDark) R.style.AppTheme_Crimson_PerfectDark else R.style.AppTheme_Crimson
    // Default (blue)
    else -> if (perfectDark) R.style.AppTheme_Blue_PerfectDark else R.style.AppTheme_Blue
}

// Amoled used to be a theme value on its own, it is a switch over the dark one now
fun SharedPreferences.migrateAmoledTheme() {
    if (getString("theme_color", "system") != "black") return
    edit {
        putString("theme_color", "dark")
        putBoolean("amoled_dark", true)
    }
}

// Pure black applies to whatever ends up being dark, the system theme included. The resources
// decide, not the preference: the amoled styles only exist in values-night, and until the night
// mode reaches the configuration they would resolve to nothing
fun SharedPreferences.isAmoledActive(context: Context): Boolean {
    if (!getBoolean("amoled_dark", false)) return false
    if (getString("theme_color", "system") == "light") return false
    return context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

// The night mode chosen by the user, as AppCompat wants it
fun SharedPreferences.userNightMode(): Int = when (getString("theme_color", "system")) {
    "light" -> AppCompatDelegate.MODE_NIGHT_NO
    "dark" -> AppCompatDelegate.MODE_NIGHT_YES
    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
}

// Apply the night mode and the accent chosen by the user. Must be called before setContentView(),
// otherwise the views are inflated with the previous theme
fun AppCompatActivity.applyUserTheme(sharedPrefs: SharedPreferences) {
    sharedPrefs.migrateAmoledTheme()
    val accent = sharedPrefs.getString("accent_color", "blue")

    AppCompatDelegate.setDefaultNightMode(sharedPrefs.userNightMode())

    // Each amoled style inherits from its accent, so a single theme is enough
    setTheme(accentThemeRes(accent, perfectDark = sharedPrefs.isAmoledActive(this)))

    // Dynamic colors leave the on*Container roles on the static baseline in light
    if (accent == "monet") setTheme(R.style.ThemeOverlay_App_MonetContainerFix)
}

// Resolve a color attribute of the current theme. MaterialColors follows the attribute down to a
// color resource too, as the dynamic colors are, where a bare TypedValue stops at the resource id
fun getThemeColor(@AttrRes attrRes: Int, context: Context): Int =
    MaterialColors.getColor(context, attrRes, Color.TRANSPARENT)
