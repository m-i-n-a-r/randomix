package com.minar.randomix

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.minar.randomix.utilities.migrateAmoledTheme
import com.minar.randomix.utilities.userNightMode

class RandomixApplication : Application() {

    // The night mode has to be known before the first activity is attached, so that its
    // configuration, and then its theme, are already dark on a cold start
    override fun onCreate() {
        super.onCreate()
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPrefs.migrateAmoledTheme()
        AppCompatDelegate.setDefaultNightMode(sharedPrefs.userNightMode())
    }
}
