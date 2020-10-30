package com.minar.randomix.fragments;


import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.minar.randomix.activities.MainActivity;
import com.minar.randomix.R;

import java.util.Random;

public class MagicBallFragment extends androidx.fragment.app.Fragment implements OnClickListener {
    private final String[] magicAnswers = new String[36];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_magic_ball, container, false);

        ImageView shake = v.findViewById(R.id.magicBallButtonAnimation);
        shake.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.magicBallButtonAnimation) {// Start the animated vector drawable, make the button unclickable during the execution
            // Suppress warning, it's guaranteed that getView won't be null
            final ImageView magicBallAnimation = getView().findViewById(R.id.magicBallButtonAnimation);
            magicBallAnimation.setClickable(false);
            Drawable drawable = magicBallAnimation.getDrawable();
            if (drawable instanceof Animatable) {
                ((Animatable) drawable).start();
            }

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            // Vibrate and play sound using the common method in MainActivity
            Activity act = getActivity();
            if (act instanceof MainActivity) {
                ((MainActivity) act).vibrate();
                ((MainActivity) act).playSound(3);
            }

            // Initialize the answers array
            magicAnswers[0] = getString(R.string.magic_answer_1);
            magicAnswers[1] = getString(R.string.magic_answer_2);
            magicAnswers[2] = getString(R.string.magic_answer_3);
            magicAnswers[3] = getString(R.string.magic_answer_4);
            magicAnswers[4] = getString(R.string.magic_answer_5);
            magicAnswers[5] = getString(R.string.magic_answer_6);
            magicAnswers[6] = getString(R.string.magic_answer_7);
            magicAnswers[7] = getString(R.string.magic_answer_8);
            magicAnswers[8] = getString(R.string.magic_answer_9);
            magicAnswers[9] = getString(R.string.magic_answer_10);
            magicAnswers[10] = getString(R.string.magic_answer_11);
            magicAnswers[11] = getString(R.string.magic_answer_12);
            magicAnswers[12] = getString(R.string.magic_answer_13);
            magicAnswers[13] = getString(R.string.magic_answer_14);
            magicAnswers[14] = getString(R.string.magic_answer_15);
            magicAnswers[15] = getString(R.string.magic_answer_16);
            magicAnswers[16] = getString(R.string.magic_answer_17);
            magicAnswers[17] = getString(R.string.magic_answer_18);
            magicAnswers[18] = getString(R.string.magic_answer_19);
            magicAnswers[19] = getString(R.string.magic_answer_20);
            magicAnswers[20] = getString(R.string.magic_answer_21);
            magicAnswers[21] = getString(R.string.magic_answer_22);
            magicAnswers[22] = getString(R.string.magic_answer_23);
            magicAnswers[23] = getString(R.string.magic_answer_24);
            magicAnswers[24] = getString(R.string.magic_answer_25);
            magicAnswers[25] = getString(R.string.magic_answer_26);
            magicAnswers[26] = getString(R.string.magic_answer_27);
            magicAnswers[27] = getString(R.string.magic_answer_28);
            magicAnswers[28] = getString(R.string.magic_answer_29);
            magicAnswers[29] = getString(R.string.magic_answer_30);

            // Rude answers
            magicAnswers[30] = getString(R.string.magic_answer_rude_1);
            magicAnswers[31] = getString(R.string.magic_answer_rude_2);
            magicAnswers[32] = getString(R.string.magic_answer_rude_3);
            magicAnswers[33] = getString(R.string.magic_answer_rude_4);
            magicAnswers[34] = getString(R.string.magic_answer_rude_5);
            magicAnswers[35] = getString(R.string.magic_answer_rude_6);

            // Choose a random number between 0 and the answer number
            Random ran = new Random();
            final int n;
            if (sp.getBoolean("rude_answers", true)) n = ran.nextInt(36);
            else n = ran.nextInt(30);

            // Get the text view and set its value depending on n
            final TextView textViewResult = getView().findViewById(R.id.resultMagicBall);

            // Create the animations
            final Animation animIn = new AlphaAnimation(1.0f, 0.0f);
            animIn.setDuration(1500);
            textViewResult.startAnimation(animIn);
            final Animation animOut = new AlphaAnimation(0.0f, 1.0f);
            animOut.setDuration(1000);

            // Delay the execution
            getView().postDelayed(() -> {
                textViewResult.setText(magicAnswers[n]);
                textViewResult.setSelected(true);
                textViewResult.startAnimation(animOut);
                magicBallAnimation.setClickable(true);
            }, 1500);
        }
    }
}
