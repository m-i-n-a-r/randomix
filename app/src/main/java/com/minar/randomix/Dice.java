package com.minar.randomix;


import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
public class Dice extends Fragment implements OnClickListener {
    // There's a difference in animations between the first throw and the others
    private boolean notFirstThrow = false;
    // Last result to select the correct animation
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
                // Make the button unclickable
                @SuppressWarnings("ConstantConditions") // Suppress warning, it's guaranteed that getView won't be null
                final ImageView diceAnimation = (ImageView) getView().findViewById(R.id.diceButtonAnimation);
                diceAnimation.setClickable(false);
                // Vibrate and play sound using the common method in MainActivity
                Activity act = getActivity();
                if (act instanceof MainActivity) {
                    ((MainActivity) act).vibrate();
                    ((MainActivity) act).playSound(4);
                }

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

                // Reactivate the button after the right time
                getView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        diceAnimation.setClickable(true);
                    }
                }, 2000);
                // Check if it's the first throw
                if(!this.notFirstThrow) this.notFirstThrow = true;
                break;

        }

    }

    public void throwAndRunMainAnimation() {
        // Choose a random number between 1 and 6 with equal possibilities
        Random ran = new Random();
        int n = ran.nextInt(6) + 1;

        // Check for fragment changes. If the fragment has changed, no further operations are needed
        if(!isAdded()) return;
        @SuppressWarnings("ConstantConditions") // Suppress warning, it's guaranteed that getView won't be null
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
        final String result = getString(R.string.generic_result) + " " + n;

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
                textViewResult.setText(result);
                textViewResult.startAnimation(animOut);
            }
        }, 1500);
        this.lastResult = n;
    }

    public void runResetAnimation() {
        @SuppressWarnings("ConstantConditions") // Suppress warning, it's guaranteed that getView won't be null
        ImageView diceAnimation = (ImageView) getView().findViewById(R.id.diceButtonAnimation);
        String chosenDrawable = "dice_" + this.lastResult + "_to_start_vector_animation";
        int resId = getResources().getIdentifier(chosenDrawable, "drawable", "com.minar.randomix");
        diceAnimation.setImageResource(resId);
        Drawable coinDrawable = diceAnimation.getDrawable();
        ((Animatable) coinDrawable).start();
    }

}
