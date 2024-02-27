package com.minar.randomix.fragments;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.minar.randomix.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MagicBallBottomSheet extends BottomSheetDialogFragment {
    private final MagicBallFragment magicBall;
    private SharedPreferences sp;
    private EditText answerText;
    private final List<String> loadedAnswers = new ArrayList<>();
    private ChipGroup answerChips;
    private TextView placeholder;
    private SwitchCompat customAnswersSwitch;

    MagicBallBottomSheet(MagicBallFragment magicBall) {
        this.magicBall = magicBall;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the bottom sheet, initialize the shared preferences and the recent options list
        View v = inflater.inflate(R.layout.magic_ball_bottom_sheet, container, false);
        sp = getDefaultSharedPreferences(requireContext());
        boolean customActive = sp.getBoolean("custom_answers_active", false);
        String customAnswers = sp.getString("custom_answers", "");
        loadedAnswers.clear();
        Collections.addAll(loadedAnswers, customAnswers.split(";"));
        loadedAnswers.remove(""); // Ensure no empty elements are saved
        placeholder = v.findViewById(R.id.customAnswersEmptyPlaceholder);

        // Animate the drawable in loop
        ImageView noRecentImage = v.findViewById(R.id.recentImage);
        Drawable animatedNoRecent = noRecentImage.getDrawable();
        if (animatedNoRecent instanceof Animatable2) {
            ((Animatable2) animatedNoRecent).registerAnimationCallback(new Animatable2.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    ((Animatable2) animatedNoRecent).start();
                }
            });
            ((Animatable2) animatedNoRecent).start();
        }

        // Manage the text field
        answerChips = v.findViewById(R.id.customAnswerChipGroup);
        answerText = v.findViewById(R.id.customAnswerText);
        TextInputLayout answerTextLayout = v.findViewById(R.id.customAnswerTextLayout);
        answerTextLayout.setEndIconOnClickListener(v1 -> {
            insertAnswerChip(""); // Empty answer = take it from the text field
        });
        answerText.setOnEditorActionListener(((v1, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEND) {
                // Insert in both the list and the layout
                insertAnswerChip("");
                return true;
            }
            return false;
        }));

        // Manage the switch
        customAnswersSwitch = v.findViewById(R.id.customAnswerSwitch);
        customAnswersSwitch.setChecked(customActive);
        customAnswersSwitch.setEnabled(loadedAnswers.size() >= 3);
        customAnswersSwitch.setOnCheckedChangeListener((buttonView, checked) -> {
            if (checked) {
                magicBall.setCustomAnswers(loadedAnswers.toArray(new String[0]));
            } else {
                magicBall.setCustomAnswers(null);
            }
        });

        // Manage placeholder and chips
        managePlaceholder();
        if (!loadedAnswers.isEmpty()) {
            for (String s : loadedAnswers) {
                insertAnswerChip(s);
            }
        }

        return v;
    }

    // Insert the chip for a custom answer
    private void insertAnswerChip(String answer) {
        String currentAnswer;
        if (!answer.isEmpty()) currentAnswer = answer;
        else {
            currentAnswer = answerText.getText().toString().trim();
            currentAnswer = currentAnswer.replaceAll("\\s+", " ");
            answerText.setText("");
        }

        // Check if the limit is reached
        if (loadedAnswers.size() > 100 || currentAnswer.isEmpty() || currentAnswer.equals(" ")) {
            return;
        }

        // Add to the list if the answer is not empty
        if (answer.isEmpty()) {
            loadedAnswers.add(currentAnswer);
        }
        if (loadedAnswers.size() > 2) {
            customAnswersSwitch.setEnabled(true);
            if (customAnswersSwitch.isChecked())
                magicBall.setCustomAnswers(loadedAnswers.toArray(new String[0]));
        }
        managePlaceholder();

        // Inflate the layout and its onclick action
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final Chip chip = (Chip) inflater.inflate(R.layout.custom_chip, answerChips, false);
        chip.setText(currentAnswer);
        chip.setId(loadedAnswers.size());

        // Add the chip with an animation
        answerChips.addView(chip);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.chip_enter_anim);
        chip.startAnimation(animation);

        // Remove the chip and the element from the list
        chip.setOnClickListener(view -> removeChip(chip));
    }

    // Remove a single chip
    private void removeChip(final Chip chip) {
        // Remove the chip with an animation
        if (chip == null) return;
        loadedAnswers.remove(chip.getText().toString());
        if (loadedAnswers.size() < 2) {
            customAnswersSwitch.setEnabled(false);
            magicBall.setCustomAnswers(null);
        }
        final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.chip_exit_anim);
        chip.startAnimation(animation);
        managePlaceholder();
        chip.postDelayed(() -> answerChips.removeView(chip), 400);
    }

    // Evaluate the visibility of the placeholder
    private void managePlaceholder() {
        if (loadedAnswers.size() == 0) {
            answerChips.setVisibility(View.GONE);
            placeholder.setVisibility(View.VISIBLE);
        } else {
            answerChips.setVisibility(View.VISIBLE);
            placeholder.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("custom_answers_active", customAnswersSwitch.isChecked());
        StringBuilder answersString = new StringBuilder();
        for (String s : loadedAnswers) {
            String answer = s.replace(";", "");
            answersString.append(answer).append(";");
        }
        editor.putString("custom_answers", answersString.toString());

        // Set the custom answers to make sure they are updated
        if (customAnswersSwitch.isChecked() && customAnswersSwitch.isEnabled())
            magicBall.setCustomAnswers(loadedAnswers.toArray(new String[0]));
        else magicBall.setCustomAnswers(null);

        editor.apply();
        super.onDismiss(dialog);
    }
}
