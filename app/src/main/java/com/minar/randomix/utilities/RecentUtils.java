package com.minar.randomix.utilities;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.List;

public class RecentUtils {

    // Given a formatted option list string, return the corresponding list
    public static List<String> toOptionList(String formattedOptions) {
        return Arrays.asList(formattedOptions.trim().split(" \\| "));
    }

    // Given a list of options, return the corresponding formatted string
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String fromOptionList(List<String> optionList) {
        StringBuilder result = new StringBuilder();
        for (String option : optionList) {
            result.append(option);
            if (optionList.size() > optionList.indexOf(option) + 1)
                result.append(" | ");
        }
        return result.toString();
    }
}
