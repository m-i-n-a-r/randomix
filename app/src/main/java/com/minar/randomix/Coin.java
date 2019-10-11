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
public class Coin extends androidx.fragment.app.Fragment implements OnClickListener {
    // There's a difference in animations between the first flip and the others
    private boolean notFirstFlip = false;
    // Last result to select the animation. True stays for head and false stays for tail
    private boolean lastResult;

    public Coin() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_coin, container, false);

        ImageView flip = (ImageView) v.findViewById(R.id.coinButtonAnimation);
        flip.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.coinButtonAnimation:
                // Make the button unclickable
                @SuppressWarnings("ConstantConditions") // Suppress warning, it's guaranteed that getView won't be null
                final ImageView coinAnimation = (ImageView) getView().findViewById(R.id.coinButtonAnimation);
                coinAnimation.setClickable(false);

                // Vibrate and play sound using the common method in MainActivity
                Activity act = getActivity();
                if (act instanceof MainActivity) {
                    ((MainActivity) act).vibrate();
                    ((MainActivity) act).playSound(2);
                }

                // Reset the initial state with another animation
                if(this.notFirstFlip) {
                    runResetAnimation();
                    //delay the execution
                    getView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            flipAndRunMainAnimation();
                        }
                    }, 500);
                }
                else flipAndRunMainAnimation();

                // Reactivate the button after the right time
                getView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        coinAnimation.setClickable(true);
                    }
                }, 2000);
                // Check if it's the first flip
                if(!this.notFirstFlip) this.notFirstFlip = true;
                break;
        }
    }

    private void flipAndRunMainAnimation() {
        // Check for fragment changes. If the fragment has changed, no further operations are needed
        if(!isAdded()) return;
        // Get the textview and the imageview used for the result, with a simple control to avoid null object references
        final String resultHead = getString(R.string.result_head);
        final String resultTail = getString(R.string.result_tail);
        @SuppressWarnings("ConstantConditions") // Suppress warning, it's guaranteed that getView won't be null
        final TextView textViewResult = (TextView) getView().findViewById(R.id.resultCoin);
        final ImageView coinAnimation = (ImageView) getView().findViewById(R.id.coinButtonAnimation);

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
        if(n == 1) {
            coinAnimation.setImageResource(R.drawable.coin_head_vector_animation);
            Drawable coinDrawable = coinAnimation.getDrawable();
            ((Animatable) coinDrawable).start();

            // Delay the execution
            getView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    textViewResult.setText(resultHead);
                    textViewResult.startAnimation(animOut);
                }
            }, 1500);
            this.lastResult = true;
        }
        else {
            coinAnimation.setImageResource(R.drawable.coin_tail_vector_animation);
            Drawable coinDrawable = coinAnimation.getDrawable();
            ((Animatable) coinDrawable).start();

            // Delay the execution
            getView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    textViewResult.setText(resultTail);
                    textViewResult.startAnimation(animOut);
                }
            }, 1500);
            this.lastResult = false;
        }
    }

    private void runResetAnimation() {
        @SuppressWarnings("ConstantConditions") // Suppress warning, it's guaranteed that getView won't be null
        ImageView coinAnimation = (ImageView) getView().findViewById(R.id.coinButtonAnimation);
        if (this.lastResult) coinAnimation.setImageResource(R.drawable.coin_head_to_start_vector_animation);
        else coinAnimation.setImageResource((R.drawable.coin_tail_to_start_vector_animation));
        Drawable coinDrawable = coinAnimation.getDrawable();
        ((Animatable) coinDrawable).start();
    }

}
