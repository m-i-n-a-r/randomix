package com.minar.randomix;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_roulette:
                    setTitle("Roulette"); // this will set the actionbar title
                    Roulette fragmentRoulette = new Roulette();
                    FragmentTransaction fragmentTransaction1 = getFragmentManager().beginTransaction();
                    fragmentTransaction1.replace(R.id.mainFrame, fragmentRoulette, "Roulette");
                    fragmentTransaction1.commit();
                    return true;
                case R.id.navigation_coin:
                    setTitle("Coin"); // this will set the actionbar title
                    Coin fragmentCoin = new Coin();
                    FragmentTransaction fragmentTransaction2 = getFragmentManager().beginTransaction();
                    fragmentTransaction2.replace(R.id.mainFrame, fragmentCoin, "Coin");
                    fragmentTransaction2.commit();
                    return true;
                case R.id.navigation_magic_ball:
                    setTitle("Magic Ball"); // this will set the actionbar title
                    MagicBall fragmentMagicBall = new MagicBall();
                    FragmentTransaction fragmentTransaction3 = getFragmentManager().beginTransaction();
                    fragmentTransaction3.replace(R.id.mainFrame, fragmentMagicBall, "MagicBall");
                    fragmentTransaction3.commit();
                    return true;
                case R.id.navigation_dice:
                    setTitle("Dice"); // this will set the actionbar title
                    Dice fragmentDice = new Dice();
                    FragmentTransaction fragmentTransaction4 = getFragmentManager().beginTransaction();
                    fragmentTransaction4.replace(R.id.mainFrame, fragmentDice, "Dice");
                    fragmentTransaction4.commit();
                    return true;
                case R.id.navigation_settings:
                    setTitle("Settings"); // this will set the actionbar title
                    Settings fragmentSettings = new Settings();
                    FragmentTransaction fragmentTransaction5 = getFragmentManager().beginTransaction();
                    fragmentTransaction5.replace(R.id.mainFrame, fragmentSettings, "Settings");
                    fragmentTransaction5.commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // getSharedPreferences(MyPrefs, Context.MODE_PRIVATE); retrieves a specific shared preferences file
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String accent = sp.getString("accent_color", "blue");
        if (!sp.getBoolean("first", false)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("first", true);
            editor.apply();
            Intent intent = new Intent(this, IntroActivity.class); // Call the AppIntro java class
            startActivity(intent);
            finish();
        }

        // Selects the right theme to apply between light and dark
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("dark_theme", false)) {
            setTheme(R.style.AppTheme_dark);
            if (accent.equals("green")) setTheme(R.style.AppTheme_dark_green);
            if (accent.equals("red")) setTheme(R.style.AppTheme_dark_red);
            if (accent.equals("violet")) setTheme(R.style.AppTheme_dark_violet);
        }
        else {
            if (accent.equals("green")) setTheme(R.style.AppTheme_green);
            if (accent.equals("red")) setTheme(R.style.AppTheme_red);
            if (accent.equals("violet")) setTheme(R.style.AppTheme_violet);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Roulette"); // this will set the actionbar title
        Roulette fragmentRoulette = new Roulette();
        FragmentTransaction fragmentTransaction1 = getFragmentManager().beginTransaction();
        fragmentTransaction1.replace(R.id.mainFrame, fragmentRoulette, "Roulette");
        fragmentTransaction1.commit();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
