package com.minar.randomix.utilities;

import android.view.View;

import java.util.List;

public interface OnItemClickListener {
    void onItemClick(int position, List<String> recentList, View view);
    void onItemLongClick(int position, View view);
}
