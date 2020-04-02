package com.minar.randomix;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 */
public class Roulette extends androidx.fragment.app.Fragment implements OnClickListener, View.OnLongClickListener, TextView.OnEditorActionListener {
    private List<String> options = new ArrayList<>();
    private List<List<String>> recentList = new ArrayList<>();


    public Roulette() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_roulette, container, false);

        // Set the listener
        ImageView insert = v.findViewById(R.id.insertButton);
        ImageView delete = v.findViewById(R.id.recentButton);
        ImageView spin = v.findViewById(R.id.buttonSpinRoulette);
        EditText textInsert = v.findViewById(R.id.entryRoulette);

        insert.setOnClickListener(this);
        delete.setOnClickListener(this);
        delete.setOnLongClickListener(this);
        spin.setOnClickListener(this);
        spin.setOnLongClickListener(this);
        textInsert.setOnEditorActionListener(this);

        // Get the object containing the recent entries
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        String recent = sp.getString("recent", "");
        Gson gson = new Gson();
        Type type = new TypeToken<List<List<String>>>() {}.getType();
        recentList = gson.fromJson(recent, type);
        if (recentList == null) recentList = new ArrayList<>();
        return v;
    }

    @Override
    public boolean onLongClick(View v) {
        Activity act = getActivity();
        switch (v.getId()) {
            case R.id.recentButton:
                // Vibrate using the common method in MainActivity
                if (act instanceof MainActivity) ((MainActivity) act).vibrate();
                break;

            case R.id.buttonSpinRoulette:
                @SuppressWarnings("ConstantConditions")
                TextView entry = getView().findViewById(R.id.entryRoulette);
                String option1 = getResources().getString(R.string.generic_option) + "1";
                String option2 = getResources().getString(R.string.generic_option) + "2";
                String option3 = getResources().getString(R.string.generic_option) + "3";
                // Vibrate using the common method in MainActivity
                if (act instanceof MainActivity) ((MainActivity) act).vibrate();

                // Insert three options manually and spin the roulette, or clear the options
                if (options.isEmpty()) {
                    entry.setText(option1);
                    insertRouletteChip();
                    entry.setText(option2);
                    insertRouletteChip();
                    entry.setText(option3);
                    insertRouletteChip();
                    break;
                } else {
                    removeAllChips();
                    break;
                }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        Activity act = getActivity();
        @SuppressWarnings("ConstantConditions")
        // Suppress warning, it's guaranteed that getView won't be null
        final ImageView recentAnimation = getView().findViewById(R.id.recentButton);
        final ChipGroup optionsList = getView().findViewById(R.id.rouletteChipList);
        switch (v.getId()) {
            case R.id.recentButton:
                // Start the animated vector drawable
                Drawable delete = recentAnimation.getDrawable();
                if (delete instanceof Animatable) ((Animatable) delete).start();
                // Vibrate and play sound using the common method in MainActivity
                if (act instanceof MainActivity) ((MainActivity) act).vibrate();

                // Open a dialog with the recent searches
                RouletteBottomSheet bottomSheet = new RouletteBottomSheet();
                bottomSheet.show(getChildFragmentManager(), "roulette_bottom_sheet");

                break;

            case R.id.insertButton:
                // Start the animated vector drawable
                ImageView insertAnimation = getView().findViewById(R.id.insertButton);
                Drawable insert = insertAnimation.getDrawable();
                if (insert instanceof Animatable) ((Animatable) insert).start();
                // Vibrate and play sound using the common method in MainActivity
                if (act instanceof MainActivity) {
                    ((MainActivity) act).vibrate();
                    ((MainActivity) act).playSound(1);
                }
                // Insert in both the list and the layout
                insertRouletteChip();
                break;

            case R.id.buttonSpinRoulette:
                // Break the case if the list is empty to avoid crashes and null pointers
                if (options.isEmpty() || options.size() == 1) {
                    Toast.makeText(getContext(), getString(R.string.no_entry_roulette), Toast.LENGTH_SHORT).show();
                    break;
                }
                // Start the animated vector drawable, make the button not clickable during the execution
                final ImageView spinAnimation = getView().findViewById(R.id.buttonSpinRoulette);

                recentAnimation.setClickable(false);
                recentAnimation.setLongClickable(false);
                spinAnimation.setClickable(false);
                spinAnimation.setLongClickable(false);
                final int childCount = optionsList.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    Chip option = (Chip) optionsList.getChildAt(i);
                    option.setClickable(false);
                }

                Drawable spin = spinAnimation.getDrawable();
                if (spin instanceof Animatable) ((Animatable) spin).start();

                // Vibrate and play sound using the common method in MainActivity
                if (act instanceof MainActivity) {
                    ((MainActivity) act).vibrate();
                    ((MainActivity) act).playSound(1);
                }
                Random ran = new Random();
                final int n = ran.nextInt(options.size());

                // Get the text view and set its value depending on n (using a delay)
                final TextView textViewResult = getView().findViewById(R.id.resultRoulette);

                // Create the animations
                final Animation animIn = new AlphaAnimation(1.0f, 0.0f);
                animIn.setDuration(1500);
                textViewResult.startAnimation(animIn);
                final Animation animOut = new AlphaAnimation(0.0f, 1.0f);
                animOut.setDuration(1000);

                getView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textViewResult.setText(options.get(n));
                        textViewResult.startAnimation(animOut);
                        spinAnimation.setClickable(true);
                        spinAnimation.setLongClickable(true);
                        recentAnimation.setClickable(true);
                        recentAnimation.setLongClickable(true);
                        for (int i = 0; i < childCount; i++) {
                            Chip option = (Chip) optionsList.getChildAt(i);
                            option.setClickable(true);
                        }
                    }
                }, 1500);

                // Save the entries in the recent entries
                insertInRecent(options);
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sp.edit();
                Gson gson = new Gson();
                String json = gson.toJson(recentList);
                editor.putString("recent", json);
                editor.apply();
                break;
        }
    }

    // Handle the keyboard actions, like enter, done, send and so on.
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEND) {
            // Start the animated vector drawable
            @SuppressWarnings("ConstantConditions") // Suppress warning, it's guaranteed that getView won't be null
            ImageView insertAnimation = getView().findViewById(R.id.insertButton);
            Drawable insert = insertAnimation.getDrawable();
            if (insert instanceof Animatable) ((Animatable) insert).start();
            // Insert in both the list and the layout
            insertRouletteChip();
            return true;
        }
        return false;
    }

    // Insert a chip in the roulette (15 chips limit)
    private void insertRouletteChip() {
        String currentOption;
        @SuppressWarnings("ConstantConditions") // Suppress warning, it's guaranteed that getView won't be null
                TextView entry = getView().findViewById(R.id.entryRoulette);
        final ChipGroup optionsList = getView().findViewById(R.id.rouletteChipList);

        // Delete the blank spaces between words and before and after them to avoid weird behaviors
        currentOption = entry.getText().toString().trim();
        currentOption = currentOption.replaceAll("\\s+", " ");

        // Return if the string entered is a duplicate
        if (options.contains(currentOption) || currentOption.equals("")) return;
        // Reset the text field eventually, it could contain whitespaces
        entry.setText("");

        // Check if the limit is reached
        if (options.size() > R.dimen.option_number_roulette) {
            Toast.makeText(getContext(), getString(R.string.too_much_entries_roulette), Toast.LENGTH_SHORT).show();
            return;
        }

        // Add to the list
        options.add(currentOption);

        // Inflate the layout and its onclick action
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final Chip chip = (Chip) inflater.inflate(R.layout.chip_roulette, optionsList, false);
        chip.setText(currentOption);
        chip.setId(options.size());

        // Add the chip with an animation
        optionsList.addView(chip);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.chip_enter_anim);
        chip.startAnimation(animation);

        // Remove the chip and the element from the list
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeChip(chip);
            }
        });
    }

    // Remove a single chip
    private void removeChip(final Chip chip) {
        @SuppressWarnings("ConstantConditions") final ChipGroup optionsList = getView().findViewById(R.id.rouletteChipList);
        // Remove the chip with an animation
        if (chip == null) return;
        final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.chip_exit_anim);
        chip.startAnimation(animation);
        chip.postDelayed(new Runnable() {
            @Override
            public void run() {
                optionsList.removeView(chip);
                options.remove(chip.getText().toString());
            }
        }, 400);
    }

    // Remove every chip in the list
    private void removeAllChips() {
        @SuppressWarnings("ConstantConditions") final ChipGroup optionsList = getView().findViewById(R.id.rouletteChipList);
        final int childCount = optionsList.getChildCount();
        for (int i = 0; i < childCount; i++) {
            Chip chip = (Chip) optionsList.getChildAt(i);
            removeChip(chip);
        }
    }

    // Insert a new list in the recent options list
    private void insertInRecent(List<String> newRecent) {
        // Check if there's a duplicate and insert
        for (List<String> elem : recentList) {
            if (newRecent.size() != elem.size()) continue;
            newRecent = new ArrayList<>(newRecent);
            elem = new ArrayList<>(elem);
            Collections.sort(newRecent);
            Collections.sort(elem);
            if (newRecent.equals(elem)) return;
        }
        recentList.add(newRecent);
    }

}
