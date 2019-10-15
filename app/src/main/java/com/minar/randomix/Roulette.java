package com.minar.randomix;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class Roulette extends androidx.fragment.app.Fragment implements OnClickListener, View.OnLongClickListener, TextView.OnEditorActionListener {
    private List<String> options = new ArrayList<>();

    public Roulette() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_roulette, container, false);
        // Set the listener
        ImageView insert = (ImageView) v.findViewById(R.id.insertButton);
        ImageView delete = (ImageView) v.findViewById(R.id.deleteButton);
        ImageView spin = (ImageView) v.findViewById(R.id.buttonSpinRoulette);
        EditText textInsert = (EditText) v.findViewById(R.id.entryRoulette);

        insert.setOnClickListener(this);
        delete.setOnClickListener(this);
        delete.setOnLongClickListener(this);
        spin.setOnClickListener(this);
        spin.setOnLongClickListener(this);
        textInsert.setOnEditorActionListener(this);
        return v;
    }

    @Override
    public boolean onLongClick(View v) {
        @SuppressWarnings("ConstantConditions") // Suppress warning, it's guaranteed that getView won't be null
        ChipGroup optionsList = getView().findViewById(R.id.rouletteChipList);
        Activity act = getActivity();
        switch (v.getId()) {
            case R.id.deleteButton:
                // Start the animated vector drawable
                ImageView deleteAnimation = (ImageView) getView().findViewById(R.id.deleteButton);
                Drawable delete = deleteAnimation.getDrawable();
                if (delete instanceof Animatable) ((Animatable) delete).start();
                // Vibrate using the common method in MainActivity
                if (act instanceof MainActivity) ((MainActivity) act).vibrate();

                // Clear the options
                if (options.isEmpty()) return true;
                options.clear();
                optionsList.removeAllViews();
                break;

            case R.id.buttonSpinRoulette:
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
                    options.clear();
                    optionsList.removeAllViews();
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
        final ImageView deleteAnimation = (ImageView) getView().findViewById(R.id.deleteButton);
        ChipGroup optionsList = getView().findViewById(R.id.rouletteChipList);
        switch (v.getId()) {
            case R.id.deleteButton:
                // Start the animated vector drawable
                Drawable delete = deleteAnimation.getDrawable();
                if (delete instanceof Animatable) ((Animatable) delete).start();
                // Vibrate and play sound using the common method in MainActivity
                if (act instanceof MainActivity) {
                    ((MainActivity) act).vibrate();
                    ((MainActivity) act).playSound(1);
                }
                if (options.isEmpty()) return;
                optionsList.removeView(getView().findViewById(options.size()));
                options.remove(options.size() - 1);
                break;

            case R.id.insertButton:
                // Start the animated vector drawable
                ImageView insertAnimation = (ImageView) getView().findViewById(R.id.insertButton);
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
                // Start the animated vector drawable, make the button unclickable during the execution
                final ImageView spinAnimation = (ImageView) getView().findViewById(R.id.buttonSpinRoulette);
                deleteAnimation.setClickable(false);
                deleteAnimation.setLongClickable(false);
                spinAnimation.setClickable(false);
                spinAnimation.setLongClickable(false);

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
                final TextView textViewResult = (TextView) getView().findViewById(R.id.resultRoulette);

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
                        deleteAnimation.setClickable(true);
                        deleteAnimation.setLongClickable(true);
                    }
                }, 1500);
                break;
        }
    }

    // Handle the keyboard actions, like enter, done, send and so on.
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        @SuppressWarnings("ConstantConditions") // Suppress warning, it's guaranteed that getView won't be null
        ChipGroup optionsList = getView().findViewById(R.id.rouletteChipList);
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEND) {
            // Start the animated vector drawable
            ImageView insertAnimation = (ImageView) getView().findViewById(R.id.insertButton);
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
        String currentOption = "";
        @SuppressWarnings("ConstantConditions") // Suppress warning, it's guaranteed that getView won't be null
        TextView entry = getView().findViewById(R.id.entryRoulette);
        final ChipGroup optionsList = getView().findViewById(R.id.rouletteChipList);

        // Delete the blank spaces between words and before and after them to avoid weird behaviors
        currentOption = entry.getText().toString().trim();
        currentOption = currentOption.replaceAll("\\s+", " ");

        // Return if the string entered is a duplicate
        if (options.contains(currentOption)) return;
        // Reset the text field eventually, it could contain whitespaces
        entry.setText("");

        // Check if the limit is reached
        if (options.size() > 14) {
            Toast.makeText(getContext(), getString(R.string.too_much_entries_roulette), Toast.LENGTH_SHORT).show();
            return;
        }

        // Add to the list
        options.add(currentOption);

        // Inflate the layout and its onclick action
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final Chip chip = (Chip)inflater.inflate(R.layout.chip_roulette, optionsList, false);
        chip.setText(currentOption);
        chip.setId(options.size());
        optionsList.addView(chip);

        // Remove the chip and the element from the list
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optionsList.removeView(view);
                options.remove(chip.getText().toString());
            }
        });
    }

}
