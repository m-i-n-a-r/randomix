package com.minar.randomix.utilities

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// The recent sets of options of the roulette, stored as json. A pinned set carries a fake entry
object RecentUtils {
    private const val RECENT_KEY = "recent"
    private const val MAX_RECENT = 10

    fun load(sp: SharedPreferences): MutableList<MutableList<String>> {
        val json = sp.getString(RECENT_KEY, null)
        if (json.isNullOrEmpty()) return mutableListOf()
        return Gson().fromJson(
            json, object : TypeToken<MutableList<MutableList<String>>>() {}.type
        ) ?: mutableListOf()
    }

    fun save(sp: SharedPreferences, recentList: List<List<String>>) =
        sp.edit { putString(RECENT_KEY, Gson().toJson(recentList)) }

    fun isPinned(options: List<String>) = options.contains(Constants.PIN_WORKAROUND_ENTRY)

    // Save a set of options, unless the same set is already there. When the list is full the
    // oldest set not pinned makes room, and if every set is pinned nothing is saved
    fun addRecent(sp: SharedPreferences, options: List<String>) {
        val recentList = load(sp)
        val sortedNew = options.sorted()
        if (recentList.any { recent -> recent.filter { it != Constants.PIN_WORKAROUND_ENTRY }.sorted() == sortedNew })
            return
        if (recentList.size >= MAX_RECENT) {
            val removable = recentList.firstOrNull { !isPinned(it) } ?: return
            recentList.remove(removable)
        }
        recentList.add(options.toMutableList())
        save(sp, recentList)
    }

    // The options of a recent set, without the pin
    fun withoutPin(options: List<String>) = options.filter { it != Constants.PIN_WORKAROUND_ENTRY }

    fun fromOptionList(optionList: List<String>): String = withoutPin(optionList).joinToString(" | ")
}
