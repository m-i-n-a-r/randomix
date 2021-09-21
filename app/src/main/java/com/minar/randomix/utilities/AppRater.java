package com.minar.randomix.utilities;

import android.content.SharedPreferences;

import com.minar.randomix.activities.MainActivity;
import com.minar.randomix.fragments.RateBottomSheet;

// Utility class, to rate the app after a certain amount of uses. Replaces gene-rate
public final class AppRater {

    private static final String DO_NOT_SHOW_AGAIN = "do_not_show_again";
    private static final String APP_RATING = "app_rating";
    private static final String LAUNCH_COUNT = "launch_count";
    private static final String DATE_FIRST_LAUNCH = "date_first_launch";

    private static final int DAYS_UNTIL_PROMPT = 2; // Min number of days
    private static final int LAUNCHES_UNTIL_PROMPT = 3; // Min number of launches

    public static void appLaunched(MainActivity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(APP_RATING, 0);
        if (prefs.getBoolean(DO_NOT_SHOW_AGAIN, false)) return;
        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launchCount = prefs.getLong(LAUNCH_COUNT, 0) + 1;
        editor.putLong(LAUNCH_COUNT, launchCount);

        // Get date of first launch
        long dateFirstLaunch = prefs.getLong(DATE_FIRST_LAUNCH, 0);
        if (dateFirstLaunch == 0L) {
            dateFirstLaunch = System.currentTimeMillis();
            editor.putLong(DATE_FIRST_LAUNCH, dateFirstLaunch);
        }

        // Wait at least n days before opening
        if (launchCount >= LAUNCHES_UNTIL_PROMPT) {
            long millisUntilPrompt = DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000;
            if (System.currentTimeMillis() >= dateFirstLaunch + millisUntilPrompt) {
                showRateDialog(activity, editor);
            }
        }
        editor.apply();
    }

    // Show the bottom sheet regarding the app rating
    private static void showRateDialog(MainActivity activity, SharedPreferences.Editor editor) {
        final RateBottomSheet bottomSheet = new RateBottomSheet(activity, editor);
        if (bottomSheet.isAdded()) return;
        bottomSheet.show(activity.getSupportFragmentManager(), "rate_bottom_sheet");
    }
}