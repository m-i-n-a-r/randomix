package com.minar.randomix.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
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

import androidx.constraintlayout.widget.ConstraintLayout;

import com.minar.randomix.R;
import com.minar.randomix.activities.MainActivity;
import com.minar.randomix.utilities.ShakeEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class DiceFragment extends androidx.fragment.app.Fragment implements OnClickListener {
    private MainActivity act;
    // Shake variables
    private boolean shakeEnabled = false;
    private SensorManager sensorManager;
    private ShakeEventListener sensorListener;
    // There's a difference in animations between the first throw and the others
    private boolean notFirstThrow = false;
    // Last result to select the correct animation
    private int lastResult1,
            lastResult2,
            lastResult3,
            lastResult4,
            lastResult5,
            lastResult6,
            lastResult7,
            lastResult8,
            lastResult9,
            lastResult10;
    private String chosenDrawable1,
            chosenDrawable2,
            chosenDrawable3,
            chosenDrawable4,
            chosenDrawable5,
            chosenDrawable6,
            chosenDrawable7,
            chosenDrawable8,
            chosenDrawable9,
            chosenDrawable10;
    private ImageView diceAnimation1,
            diceAnimation2 = null,
            diceAnimation3 = null,
            diceAnimation4 = null,
            diceAnimation5 = null,
            diceAnimation6 = null,
            diceAnimation7 = null,
            diceAnimation8 = null,
            diceAnimation9 = null,
            diceAnimation10 = null,
            vsAnimation = null;

    @Override
    public void onResume() {
        super.onResume();
        if (shakeEnabled) {
            sensorManager.registerListener(sensorListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        if (shakeEnabled)
            sensorManager.unregisterListener(sensorListener);
        super.onPause();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dice, container, false);
        // Get the placeholder where the correct layout will be inflated
        LinearLayout diceSection = v.findViewById(R.id.diceSection);
        // Get the shared preferences and the desired number of dices, from 1 to 11
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Hide description if needed
        if (sp.getBoolean("hide_descriptions", false))
            v.findViewById(R.id.descriptionDice).setVisibility(View.GONE);
        int diceNumber = Integer.parseInt(Objects.requireNonNull(sp.getString("dice_number", "1")));
        // Choose the correct layout. diceNumber goes from 1 to 11
        ConstraintLayout diceZone;
        switch (diceNumber) {
            case 1:
                inflater.inflate(R.layout.single_dice, diceSection);
                break;
            case 2:
                inflater.inflate(R.layout.double_dice, diceSection);
                break;
            case 3:
                inflater.inflate(R.layout.triple_dice, diceSection);
                break;
            case 4:
                inflater.inflate(R.layout.hexa_dice, diceSection);
                break;
            case 5:
                inflater.inflate(R.layout.penta_dice, diceSection);
                break;
            case 6:
                inflater.inflate(R.layout.esa_dice, diceSection);
                break;
            case 7:
                inflater.inflate(R.layout.septa_dice, diceSection);
                break;
            case 8:
                inflater.inflate(R.layout.octa_dice, diceSection);
                break;
            case 9:
                inflater.inflate(R.layout.nona_dice, diceSection);
                break;
            case 10:
                inflater.inflate(R.layout.deca_dice, diceSection);
                break;
            case 11:
                inflater.inflate(R.layout.triple_v_triple_dice, diceSection);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + diceNumber);
        }
        diceZone = v.findViewById(R.id.diceZone);
        diceZone.setOnClickListener(this);

        // Manage the shake to launch option and bind the activity
        act = (MainActivity) getActivity();
        if (act != null) {
            shakeEnabled = act.shakeAllowed();
            if (shakeEnabled) {
                sensorManager = (SensorManager) act.getSystemService(Context.SENSOR_SERVICE);
                sensorListener = new ShakeEventListener();
                sensorListener.setOnShakeListener(this::mainThrow);
            }
        }
        return v;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.diceZone) {
            mainThrow();
        }
    }

    // The main method of this fragment, triggered by pressing the button or shaking the device
    private void mainThrow() {
        // Make the button un-clickable and the device un-shakeable
        if (shakeEnabled) sensorManager.unregisterListener(sensorListener);
        final ConstraintLayout diceAnimation = requireView().findViewById(R.id.diceZone);
        diceAnimation.setClickable(false);

        // Get the shared preferences and the desired number of dices, from 1 to 11
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        final int diceNumber = Integer.parseInt(Objects.requireNonNull(sp.getString("dice_number", "1")));

        // Vibrate and play sound using the common method in MainActivity
        if (act != null) {
            act.vibrate();
            act.playSound(4);
        }

        // Reset the initial state with another animation
        if (this.notFirstThrow) {
            runResetAnimation(diceNumber);

            // Delay the execution
            requireView().postDelayed(() -> throwAndRunMainAnimation(diceNumber), 500);
        } else throwAndRunMainAnimation(diceNumber);

        // Reactivate the button after the right time
        requireView().postDelayed(() -> {
            diceAnimation.setClickable(true);
            if (shakeEnabled) sensorManager.registerListener(sensorListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_UI);
        }, 2000);
        // Check if it's the first throw
        if (!this.notFirstThrow) this.notFirstThrow = true;
    }

    // Run the main animation based on the result
    @SuppressLint("DiscouragedApi")
    private void throwAndRunMainAnimation(int diceNumber) {

        // Choose a random number between 1 and 6 with equal possibilities
        Random ran = new Random();
        List<Integer> diceResults = new ArrayList<>();
        // Get up to 10 numbers depending on the dice number
        for (int i = 0; i < diceNumber; i++) {
            diceResults.add(ran.nextInt(6) + 1);
        }

        // Check for fragment changes. If the fragment has changed, no further operations are needed
        if (!isAdded()) return;

        diceAnimation1 = requireView().findViewById(R.id.diceButtonAnimation1);
        if (diceNumber > 1) diceAnimation2 = requireView().findViewById(R.id.diceButtonAnimation2);
        if (diceNumber > 2) diceAnimation3 = requireView().findViewById(R.id.diceButtonAnimation3);
        if (diceNumber > 3) diceAnimation4 = requireView().findViewById(R.id.diceButtonAnimation4);
        if (diceNumber > 4) diceAnimation5 = requireView().findViewById(R.id.diceButtonAnimation5);
        if (diceNumber > 5) diceAnimation6 = requireView().findViewById(R.id.diceButtonAnimation6);
        if (diceNumber > 6 && diceNumber < 11)
            diceAnimation7 = requireView().findViewById(R.id.diceButtonAnimation7);
        if (diceNumber > 7 && diceNumber < 11)
            diceAnimation8 = requireView().findViewById(R.id.diceButtonAnimation8);
        if (diceNumber > 8 && diceNumber < 11)
            diceAnimation9 = requireView().findViewById(R.id.diceButtonAnimation9);
        if (diceNumber == 10)
            diceAnimation10 = requireView().findViewById(R.id.diceButtonAnimation10);
        if (diceNumber == 11) vsAnimation = requireView().findViewById(R.id.diceButtonAnimationVs);

        // Set the drawable programmatically, depending on the dices number
        chosenDrawable1 = "dice_" + diceResults.get(0) + "_vector_animation";
        int resId1 = getResources().getIdentifier(chosenDrawable1, "drawable", "com.minar.randomix");
        diceAnimation1.setImageResource(resId1);
        // Animate the drawable
        Drawable diceDrawable1 = diceAnimation1.getDrawable();
        ((Animatable) diceDrawable1).start();
        this.lastResult1 = diceResults.get(0);

        if (diceNumber > 1) {
            chosenDrawable2 = "dice_" + diceResults.get(1) + "_vector_animation";
            int resId2 = getResources().getIdentifier(chosenDrawable2, "drawable", "com.minar.randomix");
            diceAnimation2.setImageResource(resId2);
            // Animate the drawable
            Drawable diceDrawable2 = diceAnimation2.getDrawable();
            ((Animatable) diceDrawable2).start();
            this.lastResult2 = diceResults.get(1);
        }
        if (diceNumber > 2) {
            chosenDrawable3 = "dice_" + diceResults.get(2) + "_vector_animation";
            int resId3 = getResources().getIdentifier(chosenDrawable3, "drawable", "com.minar.randomix");
            diceAnimation3.setImageResource(resId3);
            // Animate the drawable
            Drawable diceDrawable3 = diceAnimation3.getDrawable();
            ((Animatable) diceDrawable3).start();
            this.lastResult3 = diceResults.get(2);
        }
        if (diceNumber > 3) {
            chosenDrawable4 = "dice_" + diceResults.get(3) + "_vector_animation";
            int resId4 = getResources().getIdentifier(chosenDrawable4, "drawable", "com.minar.randomix");
            diceAnimation4.setImageResource(resId4);
            // Animate the drawable
            Drawable diceDrawable4 = diceAnimation4.getDrawable();
            ((Animatable) diceDrawable4).start();
            this.lastResult4 = diceResults.get(3);
        }
        if (diceNumber > 4) {
            chosenDrawable5 = "dice_" + diceResults.get(4) + "_vector_animation";
            int resId5 = getResources().getIdentifier(chosenDrawable5, "drawable", "com.minar.randomix");
            diceAnimation5.setImageResource(resId5);
            // Animate the drawable
            Drawable diceDrawable5 = diceAnimation5.getDrawable();
            ((Animatable) diceDrawable5).start();
            this.lastResult5 = diceResults.get(4);
        }
        if (diceNumber > 5) {
            chosenDrawable6 = "dice_" + diceResults.get(5) + "_vector_animation";
            int resId6 = getResources().getIdentifier(chosenDrawable6, "drawable", "com.minar.randomix");
            diceAnimation6.setImageResource(resId6);
            // Animate the drawable
            Drawable diceDrawable6 = diceAnimation6.getDrawable();
            ((Animatable) diceDrawable6).start();
            this.lastResult6 = diceResults.get(5);
        }
        if (diceNumber > 6 && diceNumber < 11) {
            chosenDrawable7 = "dice_" + diceResults.get(6) + "_vector_animation";
            int resId3 = getResources().getIdentifier(chosenDrawable7, "drawable", "com.minar.randomix");
            diceAnimation7.setImageResource(resId3);
            // Animate the drawable
            Drawable diceDrawable7 = diceAnimation7.getDrawable();
            ((Animatable) diceDrawable7).start();
            this.lastResult7 = diceResults.get(6);
        }
        if (diceNumber > 7 && diceNumber < 11) {
            chosenDrawable8 = "dice_" + diceResults.get(7) + "_vector_animation";
            int resId8 = getResources().getIdentifier(chosenDrawable8, "drawable", "com.minar.randomix");
            diceAnimation8.setImageResource(resId8);
            // Animate the drawable
            Drawable diceDrawable8 = diceAnimation8.getDrawable();
            ((Animatable) diceDrawable8).start();
            this.lastResult8 = diceResults.get(7);
        }
        if (diceNumber > 8 && diceNumber < 11) {
            chosenDrawable9 = "dice_" + diceResults.get(8) + "_vector_animation";
            int resId9 = getResources().getIdentifier(chosenDrawable9, "drawable", "com.minar.randomix");
            diceAnimation9.setImageResource(resId9);
            // Animate the drawable
            Drawable diceDrawable9 = diceAnimation9.getDrawable();
            ((Animatable) diceDrawable9).start();
            this.lastResult9 = diceResults.get(8);
        }
        if (diceNumber == 10) {
            chosenDrawable10 = "dice_" + diceResults.get(9) + "_vector_animation";
            int resId3 = getResources().getIdentifier(chosenDrawable10, "drawable", "com.minar.randomix");
            diceAnimation10.setImageResource(resId3);
            // Animate the drawable
            Drawable diceDrawable10 = diceAnimation10.getDrawable();
            ((Animatable) diceDrawable10).start();
            this.lastResult10 = diceResults.get(9);
        }
        if (diceNumber == 11) {
            // Animate the drawable
            Drawable vsDrawable = vsAnimation.getDrawable();
            ((Animatable) vsDrawable).start();
        }

        // Get the text view and set its value depending on n
        final TextView textViewResult = requireView().findViewById(R.id.resultDice);
        String result;
        if (diceNumber < 11)
            result = getString(R.string.generic_result) + " " + (sumElements(diceResults));
        else result = getString(R.string.generic_result) + " " +
                (lastResult1 + lastResult2 + lastResult3) + " - " +
                (lastResult4 + lastResult5 + lastResult6);

        // Create the animations
        final Animation animIn = new AlphaAnimation(1.0f, 0.0f);
        animIn.setDuration(1500);
        textViewResult.startAnimation(animIn);
        final Animation animOut = new AlphaAnimation(0.0f, 1.0f);
        animOut.setDuration(1000);

        // Delay the execution
        String finalResult = result;
        requireView().postDelayed(() -> {
            textViewResult.setText(finalResult);
            textViewResult.setSelected(true);
            textViewResult.startAnimation(animOut);
        }, 1500);
    }

    // Sum an arbitrary number of results
    private int sumElements(List<Integer> results) {
        int sum = 0;
        for (int i = 0; i < results.size(); i++) {
            sum += results.get(i);
        }
        return sum;
    }

    // Run the reset animation to return to the initial state
    @SuppressLint("DiscouragedApi")
    private void runResetAnimation(int diceNumber) {
        diceAnimation1 = requireView().findViewById(R.id.diceButtonAnimation1);
        if (diceNumber > 1) diceAnimation2 = requireView().findViewById(R.id.diceButtonAnimation2);
        if (diceNumber > 2) diceAnimation3 = requireView().findViewById(R.id.diceButtonAnimation3);
        if (diceNumber > 3) diceAnimation4 = requireView().findViewById(R.id.diceButtonAnimation4);
        if (diceNumber > 4) diceAnimation5 = requireView().findViewById(R.id.diceButtonAnimation5);
        if (diceNumber > 5) diceAnimation6 = requireView().findViewById(R.id.diceButtonAnimation6);
        if (diceNumber > 6 && diceNumber < 11)
            diceAnimation7 = requireView().findViewById(R.id.diceButtonAnimation7);
        if (diceNumber > 7 && diceNumber < 11)
            diceAnimation8 = requireView().findViewById(R.id.diceButtonAnimation8);
        if (diceNumber > 8 && diceNumber < 11)
            diceAnimation9 = requireView().findViewById(R.id.diceButtonAnimation9);
        if (diceNumber == 10)
            diceAnimation10 = requireView().findViewById(R.id.diceButtonAnimation10);

        // Choose the correct drawable and run the reset animation
        chosenDrawable1 = "dice_" + this.lastResult1 + "_to_start_vector_animation";
        int resId1 = getResources().getIdentifier(chosenDrawable1, "drawable", "com.minar.randomix");
        diceAnimation1.setImageResource(resId1);
        Drawable diceDrawable1 = diceAnimation1.getDrawable();
        ((Animatable) diceDrawable1).start();

        if (diceNumber > 1) {
            chosenDrawable2 = "dice_" + this.lastResult2 + "_to_start_vector_animation";
            int resId2 = getResources().getIdentifier(chosenDrawable2, "drawable", "com.minar.randomix");
            diceAnimation2.setImageResource(resId2);
            Drawable diceDrawable2 = diceAnimation2.getDrawable();
            ((Animatable) diceDrawable2).start();
        }

        if (diceNumber > 2) {
            chosenDrawable3 = "dice_" + this.lastResult3 + "_to_start_vector_animation";
            int resId3 = getResources().getIdentifier(chosenDrawable3, "drawable", "com.minar.randomix");
            diceAnimation3.setImageResource(resId3);
            Drawable diceDrawable3 = diceAnimation3.getDrawable();
            ((Animatable) diceDrawable3).start();
        }

        if (diceNumber > 3) {
            chosenDrawable4 = "dice_" + this.lastResult4 + "_to_start_vector_animation";
            int resId4 = getResources().getIdentifier(chosenDrawable4, "drawable", "com.minar.randomix");
            diceAnimation4.setImageResource(resId4);
            Drawable diceDrawable4 = diceAnimation4.getDrawable();
            ((Animatable) diceDrawable4).start();
        }

        if (diceNumber > 4) {
            chosenDrawable5 = "dice_" + this.lastResult5 + "_to_start_vector_animation";
            int resId5 = getResources().getIdentifier(chosenDrawable5, "drawable", "com.minar.randomix");
            diceAnimation5.setImageResource(resId5);
            Drawable diceDrawable5 = diceAnimation5.getDrawable();
            ((Animatable) diceDrawable5).start();
        }

        if (diceNumber > 5) {
            chosenDrawable6 = "dice_" + this.lastResult6 + "_to_start_vector_animation";
            int resId6 = getResources().getIdentifier(chosenDrawable6, "drawable", "com.minar.randomix");
            diceAnimation6.setImageResource(resId6);
            Drawable diceDrawable6 = diceAnimation6.getDrawable();
            ((Animatable) diceDrawable6).start();
        }

        if (diceNumber > 6 && diceNumber < 11) {
            chosenDrawable7 = "dice_" + this.lastResult7 + "_to_start_vector_animation";
            int resId7 = getResources().getIdentifier(chosenDrawable7, "drawable", "com.minar.randomix");
            diceAnimation7.setImageResource(resId7);
            Drawable diceDrawable7 = diceAnimation7.getDrawable();
            ((Animatable) diceDrawable7).start();
        }

        if (diceNumber > 7 && diceNumber < 11) {
            chosenDrawable8 = "dice_" + this.lastResult8 + "_to_start_vector_animation";
            int resId8 = getResources().getIdentifier(chosenDrawable8, "drawable", "com.minar.randomix");
            diceAnimation8.setImageResource(resId8);
            Drawable diceDrawable8 = diceAnimation8.getDrawable();
            ((Animatable) diceDrawable8).start();
        }

        if (diceNumber > 8 && diceNumber < 11) {
            chosenDrawable9 = "dice_" + this.lastResult9 + "_to_start_vector_animation";
            int resId9 = getResources().getIdentifier(chosenDrawable9, "drawable", "com.minar.randomix");
            diceAnimation9.setImageResource(resId9);
            Drawable diceDrawable9 = diceAnimation9.getDrawable();
            ((Animatable) diceDrawable9).start();
        }

        if (diceNumber == 10) {
            chosenDrawable10 = "dice_" + this.lastResult10 + "_to_start_vector_animation";
            int resId10 = getResources().getIdentifier(chosenDrawable10, "drawable", "com.minar.randomix");
            diceAnimation10.setImageResource(resId10);
            Drawable diceDrawable10 = diceAnimation10.getDrawable();
            ((Animatable) diceDrawable10).start();
        }
    }

}
