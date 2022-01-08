package com.minar.randomix.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecentUtils {

    // Given a formatted option list string, return the corresponding list
    public static List<String> toOptionList(String formattedOptions) {
        return Arrays.asList(formattedOptions.trim().split(" \\| "));
    }

    // Given a list of options, return the corresponding formatted string
    public static String fromOptionList(List<String> optionList) {
        StringBuilder result = new StringBuilder();
        // Remove the awful pinning workaround
        List<String> copy = new ArrayList<>(optionList);
        copy.remove(Constants.PIN_WORKAROUND_ENTRY);

        for (String option : copy) {
            result.append(option);
            if (copy.size() > copy.indexOf(option) + 1)
                result.append(" | ");
        }
        return result.toString();
    }
}
