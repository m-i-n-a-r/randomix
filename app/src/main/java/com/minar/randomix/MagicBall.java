package com.minar.randomix;


import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class MagicBall extends Fragment implements OnClickListener {
    String[] magicAnswers = new String[15];


    public MagicBall() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_magic_ball, container, false);

        Button b = (Button) v.findViewById(R.id.button_shake_ball);
        b.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_shake_ball:
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                // Vibrate
                Vibrator vib = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(50);

                // Start the animated vector drawable
                ImageView magicBallAnimation = (ImageView) getView().findViewById(R.id.magicBallAnimation);
                Drawable drawable = magicBallAnimation.getDrawable();
                if (drawable instanceof Animatable) {
                    ((Animatable) drawable).start();
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
                // Rude answers
                magicAnswers[10] = getString(R.string.magic_answer_11);
                magicAnswers[11] = getString(R.string.magic_answer_12);
                magicAnswers[12] = getString(R.string.magic_answer_13);
                magicAnswers[13] = getString(R.string.magic_answer_14);
                magicAnswers[14] = getString(R.string.magic_answer_15);

                // Choose a random number between 0 and 10 that will correspond to the answer
                Random ran = new Random();
                int n;
                if(sp.getBoolean("rude_answers",true)) n = ran.nextInt(15);else n = ran.nextInt(10);

                // Get the text view and set its value depending on n
                final TextView textViewResult = (TextView) getView().findViewById(R.id.resultMagicBall);
                textViewResult.setText(magicAnswers[n]);
                break;
        }
    }

}
