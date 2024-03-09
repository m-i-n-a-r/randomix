package com.minar.randomix.fragments;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.minar.randomix.R;
import com.minar.randomix.activities.MainActivity;
import com.minar.randomix.utilities.ShakeEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class UniversalDiceFragment extends androidx.fragment.app.Fragment implements OnClickListener {
    private MainActivity act;
    // Shake variables
    private boolean shakeEnabled = false;
    private SensorManager sensorManager;
    private ShakeEventListener sensorListener;

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
        View v = inflater.inflate(R.layout.fragment_universal_dice, container, false);
        // Get the shared preferences and the desired number of dices, from 1 to 11
        SharedPreferences sp = getDefaultSharedPreferences(requireContext());

        // Hide description if needed
        if (sp.getBoolean("hide_descriptions", false))
            v.findViewById(R.id.descriptionDice).setVisibility(View.GONE);

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
        SharedPreferences sp = getDefaultSharedPreferences(requireContext());
        final int diceNumber = Integer.parseInt(Objects.requireNonNull(sp.getString("dice_number", "1")));

        // Vibrate and play sound using the common method in MainActivity
        if (act != null) {
            act.vibrate();
            act.playSound(4);
        }

        // Reset the initial state with another animation
        throwAndRunMainAnimation(diceNumber);

        // Reactivate the button after the right time
        requireView().postDelayed(() -> {
            diceAnimation.setClickable(true);
            if (shakeEnabled) sensorManager.registerListener(sensorListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_UI);
        }, 2000);
    }

    // Run the main animation based on the result
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

        // Get the text view and set its value depending on n
        final TextView textViewResult = requireView().findViewById(R.id.resultDice);
        String result;
        if (diceNumber < 11)
            result = getString(R.string.generic_result) + " " + (sumElements(diceResults));
        else result = getString(R.string.generic_result) + " ";

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

    // Run a roll-like animation for a given drawable
    private void runRollAnimation(ImageView image) {
        // Setup the preview view (one single screenshot, less time wasted)
        ObjectAnimator animation = ObjectAnimator.ofFloat(image, "rotationX", 0.0f, 1800f);
        animation.setDuration(3000);
        animation.setInterpolator(new BounceInterpolator());
        animation.start();
    }
}
