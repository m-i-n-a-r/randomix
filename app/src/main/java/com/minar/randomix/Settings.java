package com.minar.randomix;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class Settings extends PreferenceFragment implements OnSharedPreferenceChangeListener, OnClickListener {
    // Easter egg stuff, why not
    int easterEgg = 0;

    public Settings() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Make the icons clickable
        ImageView logo = v.findViewById(R.id.imageMinar);
        ImageView l1 = v.findViewById(R.id.minarig);
        ImageView l2 = v.findViewById(R.id.minarps);
        ImageView l3 = v.findViewById(R.id.minargit);
        ImageView l4 = v.findViewById(R.id.minarxda);
        logo.setOnClickListener(this);
        l1.setOnClickListener(this);
        l2.setOnClickListener(this);
        l3.setOnClickListener(this);
        l4.setOnClickListener(this);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("dark_theme")) restartActivity();
        if (key.equals("accent_color")) restartActivity();
    }

    @Override
    public void onClick(View v) {
        // Vibrate and play sound using the common method in MainActivity
        Activity act = getActivity();
        Uri uri;
        switch (v.getId()) {
            case R.id.imageMinar:
                if (this.easterEgg == 3) {
                    Toast.makeText(getContext(), getString(R.string.easter_egg), Toast.LENGTH_SHORT).show();
                    this.easterEgg = 0;
                    break;
                }
                else this.easterEgg++;
                break;
            case R.id.minarig:
                if (act instanceof MainActivity) ((MainActivity) act).vibrate();
                uri = Uri.parse(getString(R.string.dev_instagram));
                Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent1);
                break;
            case R.id.minarps:
                if (act instanceof MainActivity) ((MainActivity) act).vibrate();
                uri = Uri.parse(getString(R.string.dev_other_apps));
                Intent intent2 = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent2);
                break;
            case R.id.minargit:
                if (act instanceof MainActivity) ((MainActivity) act).vibrate();
                uri = Uri.parse(getString(R.string.dev_github));
                Intent intent3 = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent3);
                break;
            case R.id.minarxda:
                if (act instanceof MainActivity) ((MainActivity) act).vibrate();
                uri = Uri.parse(getString(R.string.dev_xda));
                Intent intent4 = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent4);
                break;
        }
    }

    public void restartActivity() {
        getActivity().finish();
        final Intent intent = getActivity().getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getActivity().startActivity(intent);
    }

}
