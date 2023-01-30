package com.minar.randomix.fragments;

import android.app.Activity;
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
import android.widget.TextView;

import com.minar.randomix.R;
import com.minar.randomix.activities.MainActivity;
import com.minar.randomix.utilities.ShakeEventListener;

import java.util.Collections;
import java.util.Random;

public class MagicBallFragment extends androidx.fragment.app.Fragment implements OnClickListener {
    private MainActivity act;
    // Shake variables
    private boolean shakeEnabled = false;
    private SensorManager sensorManager;
    private ShakeEventListener sensorListener;
    private String[] magicAnswers;
    private final MagicBallBottomSheet bottomSheet = new MagicBallBottomSheet(this);
    private String[] customAnswers = null;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_magic_ball, container, false);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        // Hide description if needed
        if (sp.getBoolean("hide_descriptions", false))
            v.findViewById(R.id.descriptionMagicBall).setVisibility(View.GONE);

        ImageView shake = v.findViewById(R.id.magicBallButtonAnimation);
        ImageView recentAnimation = v.findViewById(R.id.magicBallRecentButton);

        shake.setOnClickListener(this);
        recentAnimation.setOnClickListener(this);

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
        Activity act = getActivity();
        final View view = requireView();
        int pressedId = v.getId();
        ImageView recentAnimation = view.findViewById(R.id.magicBallRecentButton);

        if (pressedId == R.id.magicBallRecentButton) {
            // Start the animated vector drawable
            Drawable recent = recentAnimation.getDrawable();
            if (recent instanceof Animatable) ((Animatable) recent).start();
            // Vibrate using the common method in MainActivity
            if (act instanceof MainActivity) ((MainActivity) act).vibrate();

            // Open a dialog with the recent answers sets
            if (bottomSheet.isAdded()) return;
            bottomSheet.show(getChildFragmentManager(), "magic_ball_bottom_sheet");
            return;
        }
        if (pressedId == R.id.magicBallButtonAnimation) {
            mainThrow();
        }
    }

    // Used from the bottom sheet to set a custom set of answers
    public void setCustomAnswers(String[] answers) {
        this.customAnswers = answers;
    }

    private void mainThrow() {
        // Start the animated vector drawable, make the button un-clickable during the execution
        final ImageView magicBallAnimation = requireView().findViewById(R.id.magicBallButtonAnimation);
        if (shakeEnabled) sensorManager.unregisterListener(sensorListener);
        magicBallAnimation.setClickable(false);
        Drawable drawable = magicBallAnimation.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean customAnswersEnabled = sp.getBoolean("custom_answers_active", false);
        if (customAnswersEnabled) {
            String customAnswers = sp.getString("custom_answers", "");
            if (!customAnswers.isEmpty()) this.customAnswers = customAnswers.split(";");
        }

        // Vibrate and play sound using the common method in MainActivity
        if (act != null) {
            act.vibrate();
            act.playSound(3);
        }

        // Initialize the answers array
        if (customAnswers != null) magicAnswers = customAnswers;
        else {
            magicAnswers = new String[36];
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
        }

        // Choose a random number between 0 and the answer number
        Random ran = new Random();
        final int n;
        if (customAnswers != null && customAnswers.length > 2)
            n = ran.nextInt(customAnswers.length);
        else {
            if (sp.getBoolean("rude_answers", true)) n = ran.nextInt(36);
            else n = ran.nextInt(30);
        }
        // Get the text view and set its value depending on n
        final TextView textViewResult = requireView().findViewById(R.id.resultMagicBall);

        // Create the animations
        final Animation animIn = new AlphaAnimation(1.0f, 0.0f);
        animIn.setDuration(1700);
        textViewResult.startAnimation(animIn);
        final Animation animOut = new AlphaAnimation(0.0f, 1.0f);
        animOut.setDuration(1000);

        // Delay the execution
        requireView().postDelayed(() -> {
            if (shakeEnabled) sensorManager.registerListener(sensorListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_UI);
            textViewResult.setText(magicAnswers[n]);
            textViewResult.setSelected(true);
            textViewResult.startAnimation(animOut);
            magicBallAnimation.setClickable(true);
        }, 1700);

    }
}
