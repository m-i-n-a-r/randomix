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
    private int firstFlip = 0;
    public Coin() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_coin, container, false);

        Button b = (Button) v.findViewById(R.id.buttonFlipCoin);
        b.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonFlipCoin:
                // Choose a random number between 0 and 1 with 50 and 50 possibilities
                Random ran = new Random();
                int n = ran.nextInt(2);
                // Get the textview and the imageview used for the result
                final TextView textViewResult = (TextView) getView().findViewById(R.id.resultCoin);
                ImageView coinAnimation = (ImageView) getView().findViewById(R.id.coinAnimation);

                // Vibrate
                Vibrator vib = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(50);

                // Start the animated vector drawable and set the text depending on the result
                if(n == 1) {
                    /*if(this.firstFlip == 0) {
                        coinAnimation.setImageResource(R.drawable.coin_head_vector_animation);
                        ((Animatable) coinDrawable).start();
                    }*/
                    coinAnimation.setImageResource(R.drawable.coin_head_vector_animation);
                    Drawable coinDrawable = coinAnimation.getDrawable();
                    ((Animatable) coinDrawable).start();
                    textViewResult.setText(getString(R.string.result_head));
                }
                else {
                    /*if(this.firstFlip == 0) {
                        coinAnimation.setImageResource(R.drawable.coin_tail_vector_animation);
                        ((Animatable) coinDrawable).start();
                    }*/
                    coinAnimation.setImageResource(R.drawable.coin_tail_vector_animation);
                    Drawable coinDrawable = coinAnimation.getDrawable();
                    ((Animatable) coinDrawable).start();
                    textViewResult.setText(getString(R.string.result_tail));
                }

                // Check if it's the first flip
                if(this.firstFlip == 0) this.firstFlip = 1;
                break;
        }
    }

}
