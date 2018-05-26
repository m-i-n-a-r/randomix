package com.minar.randomix;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class MagicBall extends Fragment implements OnClickListener {
    String[] magicAnswers = new String[10];

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
                Vibrator vib = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(50);
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
                // Choose a random number between 0 and 10 that will correspond to the answer
                Random ran = new Random();
                int n = ran.nextInt(10);
                // Get the text view and set its value depending on n
                final TextView textViewResult = (TextView) getView().findViewById(R.id.resultMagicBall);
                textViewResult.setText(magicAnswers[n]);
                break;
        }
    }

}
