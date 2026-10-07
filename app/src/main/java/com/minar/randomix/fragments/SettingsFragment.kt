package com.minar.randomix.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.minar.randomix.R
import com.minar.randomix.preferences.PreferenceTilesAnimator
import com.minar.randomix.preferences.PreferenceTilesDecoration
import com.minar.randomix.utilities.addPageInsets
import com.minar.randomix.utilities.userNightMode

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Room for the floating navbar below the last tile
        val recyclerView = view.findViewById<RecyclerView>(androidx.preference.R.id.recycler_view)
        recyclerView.clipToPadding = false
        recyclerView.addPageInsets()
        // Tiles instead of dividers: the groups already tell the categories apart
        setDivider(null)
        recyclerView.addItemDecoration(PreferenceTilesDecoration(this))
        recyclerView.itemAnimator = PreferenceTilesAnimator()
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    // The theme and the accent are applied when the activity is created, the navigation brings
    // the settings back by itself. A new night mode recreates the activity on its own, and it
    // must be in place before, or the new activity would pick the theme on the old one
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            "theme_color" -> {
                val nightMode = sharedPreferences.userNightMode()
                if (nightMode != AppCompatDelegate.getDefaultNightMode())
                    AppCompatDelegate.setDefaultNightMode(nightMode)
                else ActivityCompat.recreate(requireActivity())
            }

            "accent_color", "amoled_dark" -> ActivityCompat.recreate(requireActivity())
        }
    }
}
