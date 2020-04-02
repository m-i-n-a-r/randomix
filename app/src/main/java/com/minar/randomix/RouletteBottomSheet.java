package com.minar.randomix;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import java.util.List;
import java.util.stream.Collectors;

public class RouletteBottomSheet extends BottomSheetDialogFragment {
    private List<List<String>> recentList = new ArrayList<>();

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
            previousOption.setGravity(Gravity.CENTER_HORIZONTAL);
            rouletteBottomSheet.addView(previousOption);
            index++;
        }
    }
}
