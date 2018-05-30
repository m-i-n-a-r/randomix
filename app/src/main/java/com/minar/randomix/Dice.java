package com.minar.randomix;


import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class Dice extends Fragment implements OnClickListener {


    public Dice() {
        // Required empty public constructor
    }
    private int diceResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dice, container, false);

        Button b = (Button) v.findViewById(R.id.button_throw_dice);
        b.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_throw_dice:
                Vibrator vib = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(50);
                // Start the animated vector drawable
                ImageView diceAnimation = (ImageView) getView().findViewById(R.id.diceAnimation);
                Drawable drawable = diceAnimation.getDrawable();
                if (drawable instanceof Animatable) {
                    ((Animatable) drawable).start();
                }
                // Choose a random number between 0 and 1 with 50 and 50 possibilities
                Random ran = new Random();
                int n = ran.nextInt(6) + 1;
                // Get the text view and set its value depending on n
                final TextView textViewResult = (TextView) getView().findViewById(R.id.resultDice);
                String r = getString(R.string.generic_result) + " " + n;
                textViewResult.setText(r);
                break;
        }
    }

}
