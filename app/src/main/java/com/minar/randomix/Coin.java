package com.minar.randomix;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class Coin extends Fragment implements OnClickListener {

    public Coin() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_coin, container, false);

        Button b = (Button) v.findViewById(R.id.button_flip_coin);
        b.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_flip_coin:
                Vibrator vib = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(50);
                // Choose a random number between 0 and 1 with 50 and 50 possibilities
                Random ran = new Random();
                int n = ran.nextInt(2);
                // Get the text view and set its value depending on n
                final TextView textViewResult = (TextView) getView().findViewById(R.id.resultCoin);
                if(n == 1) textViewResult.setText(getString(R.string.result_positive));
                    else textViewResult.setText(getString(R.string.result_negative));
                break;
        }
    }

}
