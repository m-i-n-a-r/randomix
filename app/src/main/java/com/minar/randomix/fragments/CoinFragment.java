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
import android.widget.TextView;

import com.minar.randomix.activities.MainActivity;
import com.minar.randomix.R;

import java.util.Random;

public class CoinFragment extends androidx.fragment.app.Fragment implements OnClickListener {
    // There's a difference in animations between the first flip and the others
    private boolean notFirstFlip = false;
    // Last result to select the animation. True stays for head and false stays for tail
    private boolean lastResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_coin, container, false);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        // Hide description if needed
        if (sp.getBoolean("hide_descriptions", false))
            v.findViewById(R.id.descriptionCoin).setVisibility(View.GONE);

        ImageView flip = v.findViewById(R.id.coinButtonAnimation);
        flip.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.coinButtonAnimation) { // Make the button unclickable
            final ImageView coinAnimation = requireView().findViewById(R.id.coinButtonAnimation);
            coinAnimation.setClickable(false);

            // Vibrate and play sound using the common method in MainActivity
            Activity act = getActivity();
            if (act instanceof MainActivity) {
                ((MainActivity) act).vibrate();
                ((MainActivity) act).playSound(2);
            }

            // Reset the initial state with another animation
            if (this.notFirstFlip) {
                runResetAnimation();
                //delay the execution
                requireView().postDelayed(this::flipAndRunMainAnimation, 500);
            } else flipAndRunMainAnimation();

            // Reactivate the button after the right time
            requireView().postDelayed(() -> coinAnimation.setClickable(true), 2000);
            // Check if it's the first flip
            if (!this.notFirstFlip) this.notFirstFlip = true;
        }
    }

    // Execute the animation
    private void flipAndRunMainAnimation() {
        // Check for fragment changes. If the fragment has changed, no further operations are needed
        if (!isAdded()) return;
        // Get the text and the image used for the result, with a simple control to avoid null object references
        final String resultHead = getString(R.string.result_head);
        final String resultTail = getString(R.string.result_tail);

        final TextView textViewResult = requireView().findViewById(R.id.resultCoin);
        final ImageView coinAnimation = requireView().findViewById(R.id.coinButtonAnimation);

        // Create the animations
        final Animation animIn = new AlphaAnimation(1.0f, 0.0f);
        animIn.setDuration(1500);
        textViewResult.startAnimation(animIn);
        final Animation animOut = new AlphaAnimation(0.0f, 1.0f);
        animOut.setDuration(1000);

        // Choose a random number between 0 and 1 with 50 and 50 possibilities
        Random ran = new Random();
        int n = ran.nextInt(2);

        // Start the animated vector drawable and set the text depending on the result
        if (n == 1) {
            coinAnimation.setImageResource(R.drawable.coin_head_vector_animation);
            Drawable coinDrawable = coinAnimation.getDrawable();
            ((Animatable) coinDrawable).start();

            // Delay the execution
            requireView().postDelayed(() -> {
                textViewResult.setText(resultHead);
                textViewResult.startAnimation(animOut);
            }, 1500);
            this.lastResult = true;
        } else {
            coinAnimation.setImageResource(R.drawable.coin_tail_vector_animation);
            Drawable coinDrawable = coinAnimation.getDrawable();
            ((Animatable) coinDrawable).start();

            // Delay the execution
            requireView().postDelayed(() -> {
                textViewResult.setText(resultTail);
                textViewResult.startAnimation(animOut);
            }, 1500);
            this.lastResult = false;
        }
    }

    // Execute the reset animation
    private void runResetAnimation() {
        ImageView coinAnimation = requireView().findViewById(R.id.coinButtonAnimation);
        if (this.lastResult)
            coinAnimation.setImageResource(R.drawable.coin_head_to_start_vector_animation);
        else coinAnimation.setImageResource((R.drawable.coin_tail_to_start_vector_animation));
        Drawable coinDrawable = coinAnimation.getDrawable();
        ((Animatable) coinDrawable).start();
    }

}
