package com.minar.randomix.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.minar.randomix.R;
import com.minar.randomix.adapter.RecentAdapter;
import com.minar.randomix.utilities.Constants;
import com.minar.randomix.utilities.OnItemClickListener;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RouletteBottomSheet extends BottomSheetDialogFragment {
    private final RouletteFragment roulette;
    private List<List<String>> recentList;
    private SharedPreferences sp;
    private RecentAdapter adapter;

    RouletteBottomSheet(RouletteFragment roulette) {
        this.roulette = roulette;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the bottom sheet, initialize the shared preferences and the recent options list
        View v = inflater.inflate(R.layout.roulette_bottom_sheet, container, false);
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        String recent = sp.getString("recent", "");
        Gson gson = new Gson();
        Type type = new TypeToken<List<List<String>>>() {
        }.getType();
        recentList = gson.fromJson(recent, type);
        if (recentList == null) recentList = new ArrayList<>();

        // Take the recycler view and the main layout, and populate the recycler
        RecyclerView recentListLayout = v.findViewById(R.id.recentList);
        ConstraintLayout rouletteBottomSheet = v.findViewById(R.id.rouletteBottomSheet);
        populateRecentLayout(recentListLayout, rouletteBottomSheet);

        // Animate the drawable in loop
        ImageView noRecentImage = v.findViewById(R.id.recentImage);
        Drawable animatedNoRecent = noRecentImage.getDrawable();
        if (animatedNoRecent instanceof Animatable2) {
            ((Animatable2) animatedNoRecent).registerAnimationCallback(new Animatable2.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    ((Animatable2) animatedNoRecent).start();
                }
            });
            ((Animatable2) animatedNoRecent).start();
        }

        Chip lettersChip = v.findViewById(R.id.lettersChip);
        lettersChip.setOnClickListener(view -> {
            String alphabet = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z".toUpperCase();
            List<String> alphabetList = new ArrayList<>(Arrays.asList(alphabet.split(",")));
            roulette.restoreOption(alphabetList);
        });
        Chip numbersChip = v.findViewById(R.id.numbersChip);
        numbersChip.setOnClickListener(view -> {
            List<String> numbers = new ArrayList<>(100 + 1);
            for (int i = 0; i <= 100; i++) numbers.add(String.valueOf(i));
            roulette.restoreOption(numbers);
        });
        return v;
    }

    // Insert the recent options in the recycler view
    private void populateRecentLayout(RecyclerView recentListLayout, ConstraintLayout rouletteBottomSheet) {
        // Case 1, placeholder
        if (recentList == null || recentList.isEmpty())
            rouletteBottomSheet.findViewById(R.id.recentNoResult).setVisibility(View.VISIBLE);

            // Case 2, populate the recycler
        else {
            adapter = new RecentAdapter(getContext(), recentList);
            recentListLayout.setLayoutManager(new LinearLayoutManager(requireContext()));
            recentListLayout.setAdapter(adapter);
            adapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(int position, List<String> optionList, View view) {
                    roulette.restoreOption(optionList);
                }

                @Override
                public void onItemLongClick(int position, View view) {
                    // Pin this option list (i.e. avoid deletion)
                    pinRecent(position, requireContext());
                }
            });
            // Create a new helper to manage the swipes
            ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                    // Remove swiped item from list and notify the RecyclerView
                    int position = viewHolder.getAdapterPosition();
                    deleteRecent(position, requireContext());

                }
            };

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
            itemTouchHelper.attachToRecyclerView(recentListLayout);
        }
    }

    // Update the stored value of the recent options
    void deleteRecent(int index, Context context) {
        Gson gson = new Gson();
        fetchRecentList(context);
        recentList.remove(index);
        adapter.notifyItemRemoved(index);
        String json = gson.toJson(recentList);
        sp.edit().putString("recent", json).apply();
    }

    // Pin the selected list, to change it's layout and avoid its automatic deletion
    void pinRecent(int position, Context context) {
        Gson gson = new Gson();
        fetchRecentList(context);

        // I know, it's awful, but it was the only way to avoid big changes and crashes
        if (!recentList.get(position).contains(Constants.PIN_WORKAROUND_ENTRY))
            recentList.get(position).add(Constants.PIN_WORKAROUND_ENTRY);
        else recentList.get(position).remove(Constants.PIN_WORKAROUND_ENTRY);

        adapter.notifyItemChanged(position);
        String json = gson.toJson(recentList);
        sp.edit().putString("recent", json).apply();
    }

    // Update the stored value of the recent options
    void updateRecent(List<String> options, Context context) {
        Gson gson = new Gson();
        fetchRecentList(context);
        insertInRecent(options);
        String json = gson.toJson(recentList);
        sp.edit().putString("recent", json).apply();
    }

    // Restore the latest option list
    void restoreLatest(Context context) {
        fetchRecentList(context);
        if (recentList.size() > 0) {
            List<String> lastOption = recentList.get(recentList.size() - 1);
            roulette.restoreOption(lastOption);
        }
    }

    // Insert a new list in the recent options list
    private void insertInRecent(List<String> newRecent) {
        // Check if there's a duplicate, create a copy to avoid overwriting
        List<String> values = new ArrayList<>(newRecent);
        for (List<String> recent : recentList) {
            if (newRecent.size() == recent.size() || newRecent.size() == recent.size() - 1) {
                newRecent = new ArrayList<>(newRecent);
                recent = new ArrayList<>(recent);
                Collections.sort(newRecent);

                // Don't consider the pin entry
                recent.remove(Constants.PIN_WORKAROUND_ENTRY);
                Collections.sort(recent);
                if (newRecent.equals(recent)) return;
            }
        }
        // Keep 10 recent only, avoid deleting pinned recent
        if (recentList.size() > 9) {
            // Remove the first non-pinned element
            for (List<String> recent : recentList) {
                if (!recent.contains(Constants.PIN_WORKAROUND_ENTRY)) {
                    recentList.remove(recent);
                    recentList.add(values);
                    break;
                }
            }
            // No element is removable, don't save
        }
        // Recent list not empty, save
        else recentList.add(values);
    }

    // Fetch the recent list from share preferences or initialize it
    private void fetchRecentList(Context context) {
        Gson gson = new Gson();
        if (recentList == null) {
            // Shared preferences may be null if the method is called without opening the dialog
            if (sp == null) sp = PreferenceManager.getDefaultSharedPreferences(context);
            String recent = sp.getString("recent", "");
            Type type = new TypeToken<List<List<String>>>() {
            }.getType();
            recentList = gson.fromJson(recent, type);
            if (recentList == null) recentList = new ArrayList<>();
        }
    }
}
