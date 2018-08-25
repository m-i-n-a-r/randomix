package com.minar.randomix;


import android.app.Activity;
import android.app.Fragment;
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

import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class MagicBall extends Fragment implements OnClickListener {
    String[] magicAnswers = new String[22];


    public MagicBall() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_magic_ball, container, false);

        ImageView shake = (ImageView) v.findViewById(R.id.magicBallButtonAnimation);
        shake.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.magicBallButtonAnimation:
                // Start the animated vector drawable, make the button unclickable during the execution
                @SuppressWarnings("ConstantConditions") // Suppress warning, it's guaranteed that getView won't be null
                final ImageView magicBallAnimation = (ImageView) getView().findViewById(R.id.magicBallButtonAnimation);
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
                // Rude answers
                magicAnswers[14] = getString(R.string.magic_answer_15);
                magicAnswers[15] = getString(R.string.magic_answer_16);
                magicAnswers[16] = getString(R.string.magic_answer_17);
                magicAnswers[17] = getString(R.string.magic_answer_18);
                magicAnswers[18] = getString(R.string.magic_answer_19);
                magicAnswers[19] = getString(R.string.magic_answer_20);
                magicAnswers[20] = getString(R.string.magic_answer_21);
                magicAnswers[21] = getString(R.string.magic_answer_22);

                // Choose a random number between 0 and 10 that will correspond to the answer
                Random ran = new Random();
                final int n;
                if(sp.getBoolean("rude_answers",true)) n = ran.nextInt(22);
                else n = ran.nextInt(15);

                // Get the text view and set its value depending on n
                final TextView textViewResult = (TextView) getView().findViewById(R.id.resultMagicBall);

                // Create the animations
                final Animation animIn = new AlphaAnimation(1.0f, 0.0f);
                animIn.setDuration(1500);
                textViewResult.startAnimation(animIn);
                final Animation animOut = new AlphaAnimation(0.0f, 1.0f);
                animOut.setDuration(1000);

                // Delay the execution
                getView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textViewResult.setText(magicAnswers[n]);
                        textViewResult.startAnimation(animOut);
                        magicBallAnimation.setClickable(true);
                    }
                }, 1500);
                break;
        }
    }
}
