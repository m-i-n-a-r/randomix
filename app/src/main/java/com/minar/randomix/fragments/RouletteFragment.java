package com.minar.randomix.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.minar.randomix.R;
import com.minar.randomix.activities.MainActivity;
import com.minar.randomix.utilities.Constants;
import com.minar.randomix.utilities.ShakeEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RouletteFragment extends androidx.fragment.app.Fragment implements OnClickListener, View.OnLongClickListener, TextView.OnEditorActionListener {
    private MainActivity act;
    // Shake variables
    private boolean shakeEnabled = false;
    private SensorManager sensorManager;
    private ShakeEventListener sensorListener;
    private final List<String> options = new ArrayList<>();
    private final RouletteBottomSheet bottomSheet = new RouletteBottomSheet(this);
    private boolean inRangeMode = false;
    private EditText optionText;
    private EditText rangeMin;
    private EditText rangeMax;
    private TextView result;
    private SharedPreferences sp = null;

    @Override
    public void onResume() {
        super.onResume();
        if (shakeEnabled) {
            sensorManager.registerListener(sensorListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        if (shakeEnabled)
            sensorManager.unregisterListener(sensorListener);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_roulette, container, false);

        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        // Hide description if needed
        if (sp.getBoolean("hide_descriptions", false))
            v.findViewById(R.id.descriptionRoulette).setVisibility(View.GONE);

        // Set the listeners and the views
        ImageView insert = v.findViewById(R.id.insertButton);
        ImageView recent = v.findViewById(R.id.recentButton);
        ImageView spin = v.findViewById(R.id.buttonSpinRoulette);
        optionText = v.findViewById(R.id.entryRoulette);
        rangeMin = v.findViewById(R.id.rangeMinRoulette);
        rangeMax = v.findViewById(R.id.rangeMaxRoulette);
        result = v.findViewById(R.id.resultRoulette);
        SwitchMaterial rangeSwitch = v.findViewById(R.id.rangeSwitchRoulette);
        ChipGroup chipList = v.findViewById(R.id.rouletteChipList);
        ImageView range = v.findViewById(R.id.animatedRangeRoulette);
        LinearLayout rangeArea = v.findViewById(R.id.rangeOptionRoulette);
        LinearLayout standardArea = v.findViewById(R.id.textOptionRoulette);

        // Animate the avd, even if it's initially gone
        Drawable animatedRange = range.getDrawable();
        if (animatedRange instanceof Animatable2) {
            ((Animatable2) animatedRange).registerAnimationCallback(new Animatable2.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    ((Animatable2) animatedRange).start();
                }
            });
            ((Animatable2) animatedRange).start();
        }

        insert.setOnClickListener(this);
        recent.setOnClickListener(this);
        recent.setOnLongClickListener(this);
        spin.setOnClickListener(this);
        spin.setOnLongClickListener(this);
        optionText.setOnEditorActionListener(this);
        rangeSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked) {
                result.setTextSize(62);
                rangeArea.setVisibility(View.VISIBLE);
                standardArea.setVisibility(View.GONE);
                chipList.setVisibility(View.INVISIBLE);
                inRangeMode = true;
            } else {
                result.setTextSize(36);
                rangeArea.setVisibility(View.GONE);
                standardArea.setVisibility(View.VISIBLE);
                chipList.setVisibility(View.VISIBLE);
                inRangeMode = false;
            }
        });

        // Manage the shake to launch option and bind the activity
        act = (MainActivity) getActivity();
        if (act != null) {
            shakeEnabled = act.shakeAllowed();
            if (shakeEnabled) {
                sensorManager = (SensorManager) act.getSystemService(Context.SENSOR_SERVICE);
                sensorListener = new ShakeEventListener();
                sensorListener.setOnShakeListener(this::mainThrow);
            }
        }
        return v;
    }

    @Override
    public boolean onLongClick(View v) {
        Activity act = getActivity();
        // Vibrate using the common method in MainActivity
        if (act instanceof MainActivity) ((MainActivity) act).vibrate();
        int pressedId = v.getId();
        if (pressedId == R.id.recentButton) {
            // Restore the last used options
            bottomSheet.restoreLatest(getContext());
            return true;
        }
        if (pressedId == R.id.buttonSpinRoulette) {
            if (!inRangeMode) {
                // Insert three quick options, or clear them
                if (options.isEmpty()) {
                    String option1 = getResources().getString(R.string.generic_option) + "1";
                    String option2 = getResources().getString(R.string.generic_option) + "2";
                    String option3 = getResources().getString(R.string.generic_option) + "3";
                    insertRouletteChip(option1, true);
                    insertRouletteChip(option2, true);
                    insertRouletteChip(option3, true);
                } else {
                    // Ask for confirmation and eventually remove every chip
                    new MaterialAlertDialogBuilder(requireContext())
                            // Use strings from the Android sources
                            .setTitle(getString(android.R.string.dialog_alert_title))
                            .setMessage(getString(R.string.delete_all_roulette_confirmation))
                            .setPositiveButton(getString(android.R.string.ok), (dialogInterface, i) -> removeAllChips())
                            .setNegativeButton(getString(android.R.string.cancel), (dialogInterface, i) -> {
                            })
                            .show();
                }
            }
            // Populate the range text fields
            else {
                if (rangeMin.getText().length() != 0 || rangeMax.getText().length() != 0) {
                    rangeMin.setText("");
                    rangeMax.setText("");
                } else {
                    rangeMin.setText(String.valueOf(1));
                    rangeMax.setText(String.valueOf(10));
                }
            }
            return true;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        Activity act = getActivity();
        final View view = requireView();
        final ImageView recentAnimation = view.findViewById(R.id.recentButton);
        int pressedId = v.getId();

        // Open recent choices
        if (pressedId == R.id.recentButton) {
            // Start the animated vector drawable
            Drawable recent = recentAnimation.getDrawable();
            if (recent instanceof Animatable) ((Animatable) recent).start();
            // Vibrate and play sound using the common method in MainActivity
            if (act instanceof MainActivity) ((MainActivity) act).vibrate();

            // Open a dialog with the recent searches
            if (bottomSheet.isAdded()) return;
            bottomSheet.show(getChildFragmentManager(), "roulette_bottom_sheet");
            return;
        }

        // Insert entry in roulette
        if (pressedId == R.id.insertButton) {
            // Start the animated vector drawable
            ImageView insertAnimation = requireView().findViewById(R.id.insertButton);
            Drawable insert = insertAnimation.getDrawable();
            if (insert instanceof Animatable) ((Animatable) insert).start();
            // Vibrate and play sound using the common method in MainActivity
            if (act instanceof MainActivity) {
                ((MainActivity) act).vibrate();
                ((MainActivity) act).playSound(1);
            }
            // Insert in both the list and the layout
            insertRouletteChip("", true);
            return;
        }

        // Spin the roulette
        if (pressedId == R.id.buttonSpinRoulette) {
            mainThrow();
        }
    }

    private void mainThrow() {
        final ImageView spinAnimation = requireView().findViewById(R.id.buttonSpinRoulette);
        final ChipGroup optionsList = requireView().findViewById(R.id.rouletteChipList);
        final SwitchMaterial rangeSwitch = requireView().findViewById(R.id.rangeSwitchRoulette);
        final ImageView recentAnimation = requireView().findViewById(R.id.recentButton);
        int minValue = -1, maxValue = -1;
        if (!inRangeMode) {
            // Send a toast if the list is empty to avoid crashes and null pointers
            if (options.size() < 2) {
                Toast.makeText(getContext(), getString(R.string.no_entry_roulette), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            // Send a toast if the ranges are empty or have wrong values
            try {
                minValue = Integer.parseInt(rangeMin.getText().toString());
                maxValue = Integer.parseInt(rangeMax.getText().toString());
            } catch (Exception ignored) {
            }
            if (minValue == -1 || maxValue == -1 || minValue >= maxValue) {
                Toast.makeText(getContext(), getString(R.string.wrong_range_roulette), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // Make the button un-clickable and the device un-shakeable
        if (shakeEnabled) sensorManager.unregisterListener(sensorListener);
        recentAnimation.setClickable(false);
        recentAnimation.setLongClickable(false);
        spinAnimation.setClickable(false);
        spinAnimation.setLongClickable(false);
        rangeSwitch.setClickable(false);
        final int childCount = optionsList.getChildCount();
        for (int i = 0; i < childCount; i++) {
            Chip option = (Chip) optionsList.getChildAt(i);
            option.setClickable(false);
        }

        Drawable spin = spinAnimation.getDrawable();
        if (spin instanceof Animatable) ((Animatable) spin).start();

        // Vibrate and play sound using the common method in MainActivity
        if (act != null) {
            act.vibrate();
            act.playSound(1);
        }

        int n;
        if (inRangeMode) {
            // Best way to generate number in range
            n = ThreadLocalRandom.current().nextInt(minValue, maxValue + 1);
        } else {
            Random ran = new Random();

            n = ran.nextInt(options.size());
            // Insert in the recent list
            bottomSheet.updateRecent(options, getContext());
        }
        // Create the animations
        final Animation animIn = new AlphaAnimation(1.0f, 0.0f);
        animIn.setDuration(1500);
        result.startAnimation(animIn);
        final Animation animOut = new AlphaAnimation(0.0f, 1.0f);
        animOut.setDuration(1000);

        requireView().postDelayed(() -> {
            if (shakeEnabled) sensorManager.registerListener(sensorListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_UI);
            if (inRangeMode) result.setText(String.valueOf(n));
            else result.setText(options.get(n));
            result.startAnimation(animOut);
            spinAnimation.setClickable(true);
            spinAnimation.setLongClickable(true);
            recentAnimation.setClickable(true);
            recentAnimation.setLongClickable(true);
            rangeSwitch.setClickable(true);
            for (int i = 0; i < childCount; i++) {
                Chip option = (Chip) optionsList.getChildAt(i);
                option.setClickable(true);
            }
            // Remove the selected option from the list, if the option is enabled
            boolean removeLast = sp.getBoolean("remove_last", false);
            if (removeLast) removeChip((Chip) optionsList.getChildAt(n));
        }, 1500);
    }

    // Handle the keyboard actions, like enter, done, send and so on.
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEND) {
            // Start the animated vector drawable
            ImageView insertAnimation = requireView().findViewById(R.id.insertButton);
            Drawable insert = insertAnimation.getDrawable();
            if (insert instanceof Animatable) ((Animatable) insert).start();
            // Insert in both the list and the layout
            insertRouletteChip("", true);
            return true;
        }
        return false;
    }

    // Insert a chip in the roulette (15 chips limit)
    private void insertRouletteChip(String option, boolean limitNumber) {
        String currentOption;
        boolean allowEquals = sp.getBoolean("allow_equals", false);
        if (!option.equals("")) currentOption = option;
        else {
            currentOption = optionText.getText().toString().trim();
            currentOption = currentOption.replaceAll("\\s+", " ");
            // Return if the string entered is a duplicate, reset the text field
            if ((!allowEquals && options.contains(currentOption)) || currentOption.equals(""))
                return;
            optionText.setText("");
        }
        final ChipGroup optionsList = requireView().findViewById(R.id.rouletteChipList);

        // Check if the limit is reached
        if (options.size() > 29 && limitNumber) {
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
        chip.setOnClickListener(view -> removeChip(chip));
    }

    // Remove a single chip
    private void removeChip(final Chip chip) {
        final ChipGroup optionsList = requireView().findViewById(R.id.rouletteChipList);
        final ImageView spinAnimation = requireView().findViewById(R.id.buttonSpinRoulette);
        // Remove the chip with an animation
        if (chip == null) return;
        options.remove(chip.getText().toString());
        spinAnimation.setClickable(false);
        spinAnimation.setLongClickable(false);
        final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.chip_exit_anim);
        chip.startAnimation(animation);
        chip.postDelayed(() -> {
            optionsList.removeView(chip);
            spinAnimation.setClickable(true);
            spinAnimation.setLongClickable(true);
        }, 400);
    }

    // Remove every chip in the list
    private void removeAllChips() {
        final ChipGroup optionsList = requireView().findViewById(R.id.rouletteChipList);
        final int childCount = optionsList.getChildCount();
        for (int i = 0; i < childCount; i++) {
            Chip chip = (Chip) optionsList.getChildAt(i);
            removeChip(chip);
        }
    }

    // Insert a certain combination in the roulette
    void restoreOption(List<String> option) {
        removeAllChips();
        for (String item : option) {
            // It's awful. I know.
            if (item.equals(Constants.PIN_WORKAROUND_ENTRY)) continue;
            insertRouletteChip(item, false);
        }
    }

}
