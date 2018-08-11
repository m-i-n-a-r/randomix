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
    // There's a difference in animations between the first flip and the others
    private boolean notFirstThrow = false;
    // Last result to select the animation. True stays for head and false stays for tail
    private int lastResult;

    public Dice() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dice, container, false);

        ImageView launch = (ImageView) v.findViewById(R.id.diceButtonAnimation);
        launch.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.diceButtonAnimation:
                // Vibrate
                Vibrator vib = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(50);

                // Reset the initial state with another animation
                if(this.notFirstThrow) {
                    runResetAnimation();
                    // Delay the execution
                    getView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            throwAndRunMainAnimation();
                        }
                    }, 500);
                }
                else throwAndRunMainAnimation();

                // Check if it's the first throw
                if(!this.notFirstThrow) this.notFirstThrow = true;
                break;

        }

    }

    public void throwAndRunMainAnimation() {
        // Choose a random number between 1 and 6 with equal possibilities
        Random ran = new Random();
        int n = ran.nextInt(6) + 1;

        final ImageView diceAnimation = (ImageView) getView().findViewById(R.id.diceButtonAnimation);

        // Set the drawable programmatically
        String chosenDrawable = "dice_" + n + "_vector_animation";
        int resId = getResources().getIdentifier(chosenDrawable, "drawable", "com.minar.randomix");
        diceAnimation.setImageResource(resId);

        // Animate the drawable
        Drawable diceDrawable = diceAnimation.getDrawable();
        ((Animatable) diceDrawable).start();

        // Get the text view and set its value depending on n
        final TextView textViewResult = (TextView) getView().findViewById(R.id.resultDice);
        final String r = getString(R.string.generic_result) + " " + n;

        // Delay the execution
        getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                textViewResult.setText(r);
            }
        }, 1500);
        this.lastResult = n;
    }

    public void runResetAnimation() {
        ImageView diceAnimation = (ImageView) getView().findViewById(R.id.diceButtonAnimation);
        String chosenDrawable = "dice_" + this.lastResult + "_to_start_vector_animation";
        int resId = getResources().getIdentifier(chosenDrawable, "drawable", "com.minar.randomix");
        diceAnimation.setImageResource(resId);
        Drawable coinDrawable = diceAnimation.getDrawable();
        ((Animatable) coinDrawable).start();
    }

}
