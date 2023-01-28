package com.minar.randomix.fragments;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean customActive = sp.getBoolean("custom_answers_active", false);
        String customAnswers = sp.getString("custom_answers", "");
        Collections.addAll(loadedAnswers, customAnswers.split(";"));
        loadedAnswers.remove("");
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
        answerText.setOnEditorActionListener(((v1, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEND) {
                // Insert in both the list and the layout
                insertAnswerChip("");
                return true;
            }
            return false;
        }));

        // Manage placeholder and chips
        managePlaceholder();
        if (!loadedAnswers.isEmpty())
            for (String s : loadedAnswers.toArray(new String[0])) {
                System.out.println("Inserting: " + s);
                insertAnswerChip(s);
            }

        // Manage the switch
        customAnswersSwitch = v.findViewById(R.id.customAnswerSwitch);
        if (!customActive || loadedAnswers.size() < 3)
            customAnswersSwitch.setActivated(false);
        else
            customAnswersSwitch.setActivated(false);
        customAnswersSwitch.setOnCheckedChangeListener((buttonView, checked) -> {
            if (checked) {
                magicBall.setCustomAnswers(loadedAnswers.toArray(new String[0]));
            } else {
                magicBall.setCustomAnswers(null);
            }
        });

        return v;
    }

    // Insert the chip for a custom answer
    private void insertAnswerChip(String answer) {
        String currentAnswer;
        if (!answer.equals("")) currentAnswer = answer;
        else {
            currentAnswer = answerText.getText().toString().trim();
            currentAnswer = currentAnswer.replaceAll("\\s+", " ");
            answerText.setText("");
        }

        // Check if the limit is reached
        if (loadedAnswers.size() > 150) {
            return;
        }

        // Add to the list
        loadedAnswers.add(currentAnswer);
        managePlaceholder();

        // Inflate the layout and its onclick action
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final Chip chip = (Chip) inflater.inflate(R.layout.chip_roulette, answerChips, false);
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
        final Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.chip_exit_anim);
        chip.startAnimation(animation);
        managePlaceholder();
        chip.postDelayed(() -> {
            answerChips.removeView(chip);
            magicBall.setCustomAnswers(loadedAnswers.toArray(new String[0]));
        }, 400);
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
            answersString.append(s).append(";");
        }
        editor.putString("custom_answers", answersString.toString());
        editor.apply();
        super.onDismiss(dialog);
    }
}
