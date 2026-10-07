package com.minar.randomix.utilities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.minar.randomix.fragments.RateBottomSheet

object AppRater {

    private const val DO_NOT_SHOW_AGAIN = "do_not_show_again"
    private const val APP_RATING = "app_rating"
    private const val LAUNCH_COUNT = "launch_count"
    private const val DATE_FIRST_LAUNCH = "date_first_launch"

    private const val DAYS_UNTIL_PROMPT = 2
    private const val LAUNCHES_UNTIL_PROMPT = 3

    fun appLaunched(activity: AppCompatActivity) {
        val prefs = activity.getSharedPreferences(APP_RATING, 0)
        if (prefs.getBoolean(DO_NOT_SHOW_AGAIN, false)) return

        val launchCount = prefs.getLong(LAUNCH_COUNT, 0) + 1
        var dateFirstLaunch = prefs.getLong(DATE_FIRST_LAUNCH, 0L)
        if (dateFirstLaunch == 0L) dateFirstLaunch = System.currentTimeMillis()
        prefs.edit {
            putLong(LAUNCH_COUNT, launchCount)
            putLong(DATE_FIRST_LAUNCH, dateFirstLaunch)
        }

        val millisUntilPrompt = DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000L
        if (launchCount >= LAUNCHES_UNTIL_PROMPT &&
            System.currentTimeMillis() >= dateFirstLaunch + millisUntilPrompt
        ) {
            val manager = activity.supportFragmentManager
            if (manager.findFragmentByTag(RateBottomSheet.TAG) == null)
                RateBottomSheet().show(manager, RateBottomSheet.TAG)
        }
    }

    fun doNotShowAgain(context: Context, value: Boolean) =
        context.getSharedPreferences(APP_RATING, 0).edit { putBoolean(DO_NOT_SHOW_AGAIN, value) }
}
