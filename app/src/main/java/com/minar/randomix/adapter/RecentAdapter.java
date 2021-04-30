package com.minar.randomix.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.minar.randomix.R;
import com.minar.randomix.utilities.OnItemClickListener;
import com.minar.randomix.utilities.RecentUtils;

import java.util.List;

import static androidx.core.content.ContextCompat.getColor;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.RecentHolder> {
    private final List<List<String>> recentList;
    private final LayoutInflater inflater;
    private OnItemClickListener listener = null;

    class RecentHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        final RecentAdapter adapter;
        TextView optionList;

        public RecentHolder(View itemView, RecentAdapter adapter) {
            super(itemView);
            optionList = itemView.findViewById(R.id.recentText);

            this.adapter = adapter;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getLayoutPosition();
            List<String> element = recentList.get(position);
            listener.onItemClick(position, element, v);
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getLayoutPosition();
            listener.onItemLongClick(position, v);
            return true;
        }
    }

    public RecentAdapter(Context context, List<List<String>> recentList) {
        inflater = LayoutInflater.from(context);
        this.recentList = recentList;
    }

    @NonNull
    @Override
    public RecentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = inflater.inflate(R.layout.item_recent_list, parent, false);
        return new RecentHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentHolder holder, int position) {
        List<String> current = recentList.get(position);
        if (current.contains("HorribleWorkaroundToPin")) {
            holder.optionList.setTypeface(null, Typeface.BOLD);
            holder.optionList.setTextColor(getThemeAccentColor(holder.adapter.inflater.getContext()));
        } else {
            holder.optionList.setTypeface(null, Typeface.NORMAL);
            holder.optionList.setTextColor(getColor(holder.adapter.inflater.getContext(), R.color.goodGray));
        }
        holder.optionList.setText(RecentUtils.fromOptionList(current));
    }

    @Override
    public int getItemCount() {
        return recentList.size();
    }

    // Set the interface to manage the clicks outside the adapter
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Get the accent color
    public static int getThemeAccentColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        return value.data;
    }
}
