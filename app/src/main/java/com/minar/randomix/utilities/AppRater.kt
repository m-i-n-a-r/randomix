package com.minar.randomix.utilities

import com.minar.randomix.activities.MainActivity
import com.minar.randomix.fragments.RateBottomSheet
import androidx.core.content.edit

object AppRater {

    private const val DO_NOT_SHOW_AGAIN = "do_not_show_again"
    private const val APP_RATING = "app_rating"
    private const val LAUNCH_COUNT = "launch_count"
    private const val DATE_FIRST_LAUNCH = "date_first_launch"

    private const val DAYS_UNTIL_PROMPT = 2
    private const val LAUNCHES_UNTIL_PROMPT = 3

    fun appLaunched(activity: MainActivity) {
        val prefs = activity.getSharedPreferences(APP_RATING, 0)
        if (prefs.getBoolean(DO_NOT_SHOW_AGAIN, false)) return

        prefs.edit {
            val launchCount = prefs.getLong(LAUNCH_COUNT, 0) + 1
            putLong(LAUNCH_COUNT, launchCount)

            var dateFirstLaunch = prefs.getLong(DATE_FIRST_LAUNCH, 0L)
            if (dateFirstLaunch == 0L) {
                dateFirstLaunch = System.currentTimeMillis()
                putLong(DATE_FIRST_LAUNCH, dateFirstLaunch)
            }

            if (launchCount >= LAUNCHES_UNTIL_PROMPT) {
                val millisUntilPrompt = DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000L
                if (System.currentTimeMillis() >= dateFirstLaunch + millisUntilPrompt) {
                    showRateDialog(activity, this)
                }
            }
        }
    }

    private fun showRateDialog(activity: MainActivity, editor: android.content.SharedPreferences.Editor) {
        val bottomSheet = RateBottomSheet(activity, editor)
        if (bottomSheet.isAdded) return
        bottomSheet.show(activity.supportFragmentManager, "rate_bottom_sheet")
    }
}
