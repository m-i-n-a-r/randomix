package com.minar.randomix.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.minar.randomix.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RouletteBottomSheet extends BottomSheetDialogFragment {
    private List<List<String>> recentList;
    private final RouletteFragment roulette;

    RouletteBottomSheet(RouletteFragment roulette) {
        this.roulette = roulette;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.roulette_bottom_sheet, container, false);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        String recent = sp.getString("recent", "");
        Gson gson = new Gson();
        Type type = new TypeToken<List<List<String>>>() {}.getType();
        recentList = gson.fromJson(recent, type);
        if (recentList == null) recentList = new ArrayList<>();
        LinearLayout rouletteBottomSheet = v.findViewById(R.id.rouletteBottomSheet);
        addToRecentLayout(rouletteBottomSheet);

        return v;
    }

    // Insert the recent options in the layout
    private void addToRecentLayout(LinearLayout rouletteBottomSheet) {
        int index = 0;
        if (recentList == null || recentList.isEmpty()) {
            TextView noOption = new TextView(getContext());
            noOption.setText(getResources().getString(R.string.bottom_sheet_no_option));
            noOption.setTextSize(16);
            noOption.setTextColor(getResources().getColor(R.color.goodGray, requireActivity().getTheme()));
            noOption.setPadding(96, 24, 96, 24);
            noOption.setGravity(Gravity.CENTER_HORIZONTAL);
            rouletteBottomSheet.addView(noOption);
        }
        else {
            for (List<String> recent : recentList) {
                // The suggetion leads to a function for api26+
                String concatString = recent.stream().collect(Collectors.joining(" | "));
                TextView previousOption = new TextView(getContext());
                previousOption.setText(concatString);
                previousOption.setId(index);
                previousOption.setTextSize(15);
                previousOption.setPadding(28, 18, 28, 18);
                previousOption.setGravity(Gravity.CENTER_HORIZONTAL);
                // Ripple effect
                TypedValue outValue = new TypedValue();
                requireActivity().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                previousOption.setBackgroundResource(outValue.resourceId);
                previousOption.setOnClickListener(view -> {
                    int optionNumber = previousOption.getId();
                    roulette.restoreOption(recentList.get(optionNumber));
                });
                rouletteBottomSheet.addView(previousOption);
                index++;
            }
        }
    }

    // Update the stored value of the recent options
    void updateRecent(List<String> options, Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        if(recentList == null) {
            String recent = sp.getString("recent", "");
            Type type = new TypeToken<List<List<String>>>() {}.getType();
            recentList = gson.fromJson(recent, type);
            if (recentList == null) recentList = new ArrayList<>();
        }
        insertInRecent(options);
        SharedPreferences.Editor editor = sp.edit();
        String json = gson.toJson(recentList);
        editor.putString("recent", json);
        editor.apply();
    }

    void restoreLatest(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        if(recentList == null) {
            String recent = sp.getString("recent", "");
            Type type = new TypeToken<List<List<String>>>() {}.getType();
            recentList = gson.fromJson(recent, type);
            if (recentList == null) recentList = new ArrayList<>();
        }
        if(recentList.size() > 0) {
            List<String> lastOption = recentList.get(recentList.size() - 1);
            roulette.restoreOption(lastOption);
        }
    }

    // Insert a new list in the recent options list
    private void insertInRecent(List<String> newRecent) {
        // Check if there's a duplicate, create a copy to avoid overwriting
        List<String> values = new ArrayList<>(newRecent);
        for (List<String> elem : recentList) {
            if (newRecent.size() == elem.size()) {
                newRecent = new ArrayList<>(newRecent);
                elem = new ArrayList<>(elem);
                Collections.sort(newRecent);
                Collections.sort(elem);
                if (newRecent.equals(elem)) return;
            }
        }
        // Keep 10 recent only
        this.recentList.add(values);
        if (recentList.size() > 10) recentList.remove(0);
    }

}
