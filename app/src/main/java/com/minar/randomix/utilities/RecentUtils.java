package com.minar.randomix.utilities;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RecentUtils {

    // Given a formatted option list string, return the corresponding list
    public static List<String> toOptionList(String formattedOptions) {
        return Arrays.asList(formattedOptions.trim().split(" \\| "));
    }

    // Given a list of options, return the corresponding formatted string
    public static String fromOptionList(List<String> optionList) {
        // The suggestion leads to a function for api26+
        return optionList.stream().collect(Collectors.joining(" | "));
    }
}
