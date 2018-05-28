package com.minar.randomix;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class Roulette extends Fragment implements OnClickListener {
    private String currentOption = "";

    public Roulette() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_roulette, container, false);
        // Set the listener on the animated button
        ImageView insert = (ImageView) v.findViewById(R.id.insertButton);
        insert.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        Vibrator vib = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        switch (v.getId()) {
            case R.id.insertButton:
                // Start the animated vector drawable
                ImageView insertAnimation = (ImageView) getView().findViewById(R.id.insertButton);
                Drawable drawable = insertAnimation.getDrawable();
                if (drawable instanceof Animatable) {
                    ((Animatable) drawable).start();
                }
                vib.vibrate(30);
                break;
        }
    }

}
