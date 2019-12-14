package com.minar.randomix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class CustomAuthorPreference extends Preference implements View.OnClickListener {
    // Easter egg stuff, why not
    private int easterEgg = 0;

    public CustomAuthorPreference(Context context) {
        super(context);
    }

    public CustomAuthorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View v = holder.itemView;

        // Make the icons clickable
        ImageView logo = v.findViewById(R.id.imageMinar);
        ImageView l1 = v.findViewById(R.id.minarig);
        ImageView l2 = v.findViewById(R.id.minarpp);
        ImageView l3 = v.findViewById(R.id.minarps);
        ImageView l4 = v.findViewById(R.id.minargit);
        ImageView l5 = v.findViewById(R.id.minarxda);
        logo.setOnClickListener(this);
        l1.setOnClickListener(this);
        l2.setOnClickListener(this);
        l3.setOnClickListener(this);
        l4.setOnClickListener(this);
        l5.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // Vibrate and play sound using the common method in MainActivity
        Activity act = (Activity) getContext();
        Uri uri;
        switch (v.getId()) {
            case R.id.imageMinar:
                if (this.easterEgg == 3) {
                    Toast.makeText(getContext(), getContext().getString(R.string.easter_egg), Toast.LENGTH_SHORT).show();
                    this.easterEgg = 0;
                    break;
                } else this.easterEgg++;
                break;
            case R.id.minarig:
                if (act instanceof MainActivity) ((MainActivity) act).vibrate();
                uri = Uri.parse(getContext().getString(R.string.dev_instagram));
                Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
                getContext().startActivity(intent1);
                break;
            case R.id.minarpp:
                if (act instanceof MainActivity) ((MainActivity) act).vibrate();
                uri = Uri.parse(getContext().getString(R.string.dev_paypal));
                Intent intent2 = new Intent(Intent.ACTION_VIEW, uri);
                getContext().startActivity(intent2);
                break;
            case R.id.minarps:
                if (act instanceof MainActivity) ((MainActivity) act).vibrate();
                uri = Uri.parse(getContext().getString(R.string.dev_other_apps));
                Intent intent3 = new Intent(Intent.ACTION_VIEW, uri);
                getContext().startActivity(intent3);
                break;
            case R.id.minargit:
                if (act instanceof MainActivity) ((MainActivity) act).vibrate();
                uri = Uri.parse(getContext().getString(R.string.dev_github));
                Intent intent4 = new Intent(Intent.ACTION_VIEW, uri);
                getContext().startActivity(intent4);
                break;
            case R.id.minarxda:
                if (act instanceof MainActivity) ((MainActivity) act).vibrate();
                uri = Uri.parse(getContext().getString(R.string.dev_xda));
                Intent intent5 = new Intent(Intent.ACTION_VIEW, uri);
                getContext().startActivity(intent5);
                break;
        }
    }
}
