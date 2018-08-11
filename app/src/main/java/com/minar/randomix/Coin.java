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
public class Coin extends Fragment implements OnClickListener {
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
                // Vibrate
                Vibrator vib = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(50);

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

                // Check if it's the first flip
                if(!this.notFirstFlip) this.notFirstFlip = true;
                break;
        }
    }

    public void flipAndRunMainAnimation() {
        // Get the textview and the imageview used for the result
        final TextView textViewResult = (TextView) getView().findViewById(R.id.resultCoin);
        ImageView coinAnimation = (ImageView) getView().findViewById(R.id.coinButtonAnimation);
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
                    textViewResult.setText(getString(R.string.result_head));
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
                    textViewResult.setText(getString(R.string.result_tail));
                }
            }, 1500);
            this.lastResult = false;
        }
    }

    public void runResetAnimation() {
        ImageView coinAnimation = (ImageView) getView().findViewById(R.id.coinButtonAnimation);
        if (this.lastResult) coinAnimation.setImageResource(R.drawable.coin_head_to_start_vector_animation);
        else coinAnimation.setImageResource((R.drawable.coin_tail_to_start_vector_animation));
        Drawable coinDrawable = coinAnimation.getDrawable();
        ((Animatable) coinDrawable).start();
    }

}
