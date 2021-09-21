package com.minar.randomix.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.minar.randomix.R;
import com.minar.randomix.activities.MainActivity;

public class RateBottomSheet extends BottomSheetDialogFragment {
    private final MainActivity activity;
    private final SharedPreferences.Editor editor;

    public RateBottomSheet(MainActivity activity, SharedPreferences.Editor editor) {
        this.activity = activity;
        this.editor = editor;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the bottom sheet, initialize the shared preferences and the recent options list
        View v = inflater.inflate(R.layout.rate_bottom_sheet, container, false);

        final String DO_NOT_SHOW_AGAIN = "do_not_show_again";

        // Animate the drawable in loop
        ImageView noRecentImage = v.findViewById(R.id.rateImage);
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

        Button positiveButton = v.findViewById(R.id.positiveButton);
        Button negativeButton = v.findViewById(R.id.negativeButton);
        MaterialCheckBox checkBox = v.findViewById(R.id.neverAgainCheckbox);

        // Handling the positive button
        positiveButton.setOnClickListener(view -> {
            activity.startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + activity.getPackageName())
                    )
            );
            editor.putBoolean(DO_NOT_SHOW_AGAIN, true);
            editor.commit();
            this.dismiss();
        });

        // Handling the negative button
        negativeButton.setOnClickListener(view -> this.dismiss());

        // Handling the "don't ask again" checkbox
        checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
            editor.putBoolean(DO_NOT_SHOW_AGAIN, b);
            editor.commit();
        });

        return v;
    }
}
