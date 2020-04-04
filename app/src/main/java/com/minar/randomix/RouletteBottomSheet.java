package com.minar.randomix;

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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RouletteBottomSheet extends BottomSheetDialogFragment {
    private List<List<String>> recentList;
    private Roulette roulette;

    RouletteBottomSheet(Roulette roulette) {
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
        if (recentList.size() > 0) addToRecentLayout(rouletteBottomSheet);

        return v;
    }

    // Insert the recent options in the layout
    private void addToRecentLayout(LinearLayout rouletteBottomSheet) {
        int index = 0;
        for (List<String> recent : recentList) {
            String concatString= recent.stream().collect(Collectors.joining(" | "));
            TextView previousOption = new TextView(getContext());
            previousOption.setText(concatString);
            previousOption.setId(index);
            previousOption.setTextSize(16);
            previousOption.setPadding(0,16,0,16);
            previousOption.setGravity(Gravity.CENTER_HORIZONTAL);
            // Ripple effect
            TypedValue outValue = new TypedValue();
            Objects.requireNonNull(getActivity()).getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            previousOption.setBackgroundResource(outValue.resourceId);
            previousOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int optionNumber = previousOption.getId();
                    roulette.restoreOption(recentList.get(optionNumber));
                }
            });
            rouletteBottomSheet.addView(previousOption);
            index++;
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

    // Insert a new list in the recent options list
    private void insertInRecent(List<String> newRecent) {
        // Check if there's a duplicate and insert
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
        if (recentList.size() > 10) recentList.remove(0);
        recentList.add(newRecent);
    }

}
