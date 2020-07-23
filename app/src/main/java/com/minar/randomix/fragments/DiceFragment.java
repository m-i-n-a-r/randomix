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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.minar.randomix.activities.MainActivity;
import com.minar.randomix.R;

import java.util.Objects;
import java.util.Random;

public class DiceFragment extends androidx.fragment.app.Fragment implements OnClickListener {
    // There's a difference in animations between the first throw and the others
    private boolean notFirstThrow = false;
    // Last result to select the correct animation
    private int lastResult1, lastResult2, lastResult3;
    private String chosenDrawable1, chosenDrawable2, chosenDrawable3;
    private ImageView diceAnimation1, diceAnimation2 = null, diceAnimation3 = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dice, container, false);
        // Get the placeholder where the correct layout will be inflated
        LinearLayout diceSection = v.findViewById(R.id.diceSection);
        // Get the shared preferences and the desired number of dices, from 1 to 3
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        int diceNumber = Integer.parseInt(Objects.requireNonNull(sp.getString("dice_number", "1")));
        // Choose the correct layout. diceNumber can only be 1, 2 or 3
        switch (diceNumber) {
            case 1:
                inflater.inflate(R.layout.single_dice, diceSection);
                LinearLayout singleDiceZone = v.findViewById(R.id.diceZone);
                singleDiceZone.setOnClickListener(this);
                break;
            case 2:
                inflater.inflate(R.layout.double_dice, diceSection);
                LinearLayout doubleDiceZone = v.findViewById(R.id.diceZone);
                doubleDiceZone.setOnClickListener(this);
                break;
            case 3:
                inflater.inflate(R.layout.triple_dice, diceSection);
                LinearLayout tripleDiceZone = v.findViewById(R.id.diceZone);
                tripleDiceZone.setOnClickListener(this);
                break;
        }
        return v;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.diceZone) {
            // Make the button unclickable
            @SuppressWarnings("ConstantConditions")
            // Suppress warning, it's guaranteed that getView won't be null
            final LinearLayout diceAnimation = getView().findViewById(R.id.diceZone);
            diceAnimation.setClickable(false);

            // Get the shared preferences and the desired number of dices, from 1 to 3
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            final int diceNumber = Integer.parseInt(Objects.requireNonNull(sp.getString("dice_number", "1")));

            // Vibrate and play sound using the common method in MainActivity
            Activity act = getActivity();
            if (act instanceof MainActivity) {
                ((MainActivity) act).vibrate();
                ((MainActivity) act).playSound(4);
            }

            // Reset the initial state with another animation
            if (this.notFirstThrow) {
                runResetAnimation(diceNumber);

                // Delay the execution
                getView().postDelayed(() -> throwAndRunMainAnimation(diceNumber), 500);
            } else throwAndRunMainAnimation(diceNumber);

            // Reactivate the button after the right time
            getView().postDelayed(() -> diceAnimation.setClickable(true), 2000);
            // Check if it's the first throw
            if (!this.notFirstThrow) this.notFirstThrow = true;
        }

    }

    // Run the main animation based on the result
    private void throwAndRunMainAnimation(int diceNumber) {
        // Choose a random number between 1 and 6 with equal possibilities
        int n1, n2 = 0, n3 = 0;
        Random ran = new Random();
        // Get 1, 2 or 3 numbers depending on the dices number
        n1 = ran.nextInt(6) + 1;

        if(diceNumber > 1) n2 = ran.nextInt(6) + 1;
        if(diceNumber > 2) n3 = ran.nextInt(6) + 1;

        // Check for fragment changes. If the fragment has changed, no further operations are needed
        if(!isAdded()) return;
        diceAnimation1 = requireView().findViewById(R.id.diceButtonAnimation1);

        if(diceNumber > 1) diceAnimation2 = requireView().findViewById(R.id.diceButtonAnimation2);
        if(diceNumber > 2) diceAnimation3 = requireView().findViewById(R.id.diceButtonAnimation3);

        // Set the drawable programmatically, depending on the dices number
        chosenDrawable1 = "dice_" + n1 + "_vector_animation";
        int resId1 = getResources().getIdentifier(chosenDrawable1, "drawable", "com.minar.randomix");
        diceAnimation1.setImageResource(resId1);
        // Animate the drawable
        Drawable diceDrawable1 = diceAnimation1.getDrawable();
        ((Animatable) diceDrawable1).start();

        if (diceNumber > 1) {
            chosenDrawable2 = "dice_" + n2 + "_vector_animation";
            int resId2 = getResources().getIdentifier(chosenDrawable2, "drawable", "com.minar.randomix");
            diceAnimation2.setImageResource(resId2);
            // Animate the drawable
            Drawable diceDrawable2 = diceAnimation2.getDrawable();
            ((Animatable) diceDrawable2).start();
        }
        if (diceNumber > 2) {
            chosenDrawable3 = "dice_" + n3 + "_vector_animation";
            int resId3 = getResources().getIdentifier(chosenDrawable3, "drawable", "com.minar.randomix");
            diceAnimation3.setImageResource(resId3);
            // Animate the drawable
            Drawable diceDrawable3 = diceAnimation3.getDrawable();
            ((Animatable) diceDrawable3).start();
        }

        // Get the text view and set its value depending on n
        final TextView textViewResult = requireView().findViewById(R.id.resultDice);
        final String result = getString(R.string.generic_result) + " " + (n1 + n2 + n3);

        // Create the animations
        final Animation animIn = new AlphaAnimation(1.0f, 0.0f);
        animIn.setDuration(1500);
        textViewResult.startAnimation(animIn);
        final Animation animOut = new AlphaAnimation(0.0f, 1.0f);
        animOut.setDuration(1000);

        // Delay the execution
        requireView().postDelayed(() -> {
            textViewResult.setText(result);
            textViewResult.setSelected(true);
            textViewResult.startAnimation(animOut);
        }, 1500);
        this.lastResult1 = n1;
        this.lastResult2 = n2;
        this.lastResult3 = n3;
    }

    // Run the reset animation to return to the initial state
    private void runResetAnimation(int diceNumber) {
        diceAnimation1 = requireView().findViewById(R.id.diceButtonAnimation1);

        if(diceNumber > 1) diceAnimation2 = requireView().findViewById(R.id.diceButtonAnimation2);
        if(diceNumber > 2) diceAnimation3 = requireView().findViewById(R.id.diceButtonAnimation3);

        // Choose the correct drawable and run the reset animation
        chosenDrawable1 = "dice_" + this.lastResult1 + "_to_start_vector_animation";
        int resId1 = getResources().getIdentifier(chosenDrawable1, "drawable", "com.minar.randomix");
        diceAnimation1.setImageResource(resId1);
        Drawable diceDrawable1 = diceAnimation1.getDrawable();
        ((Animatable) diceDrawable1).start();

        if(diceNumber > 1) {
            chosenDrawable2 = "dice_" + this.lastResult2 + "_to_start_vector_animation";
            int resId2 = getResources().getIdentifier(chosenDrawable2, "drawable", "com.minar.randomix");
            diceAnimation2.setImageResource(resId2);
            Drawable diceDrawable2 = diceAnimation2.getDrawable();
            ((Animatable) diceDrawable2).start();
        }

        if(diceNumber > 2) {
            chosenDrawable3 = "dice_" + this.lastResult3 + "_to_start_vector_animation";
            int resId3 = getResources().getIdentifier(chosenDrawable3, "drawable", "com.minar.randomix");
            diceAnimation3.setImageResource(resId3);
            Drawable diceDrawable3 = diceAnimation3.getDrawable();
            ((Animatable) diceDrawable3).start();
        }
    }

}
