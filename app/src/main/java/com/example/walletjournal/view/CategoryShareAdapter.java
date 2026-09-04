package com.example.walletjournal.view;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.walletjournal.R;
import com.example.walletjournal.model.CategoryShare;

/**
 * Binds a list of CategoryShare rows (this month's expense categories with
 * their % share of the total) into the Stats screen's 分類 legend RecyclerView.
 * COLORS is shared with StatsActivity so the donut chart segments and the
 * legend dots line up.
 */
public class CategoryShareAdapter extends RecyclerView.Adapter<CategoryShareAdapter.ViewHolder> {

    public static final int[] COLORS = {
            0xFFE2572B, // orange-red
            0xFF3B82D6, // blue
            0xFF1E7A5F, // dark green
            0xFFC08A3E, // tan/olive
            0xFF7B5EA7, // purple
            0xFF2E7D5B  // teal
    };

    private final List<CategoryShare> shares = new ArrayList<>();

    public void submitList(List<CategoryShare> newShares) {
        shares.clear();
        if (newShares != null) {
            shares.addAll(newShares);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_share, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(shares.get(position), position);
    }

    @Override
    public int getItemCount() {
        return shares.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final View dot;
        private final TextView tvCategory;
        private final TextView tvPercent;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dot = itemView.findViewById(R.id.item_dot);
            tvCategory = itemView.findViewById(R.id.item_category);
            tvPercent = itemView.findViewById(R.id.item_percent);
        }

        void bind(CategoryShare share, int position) {
            tvCategory.setText(share.getCategory());
            tvPercent.setText(Math.round(share.getPercent()) + "%");

            int color = COLORS[position % COLORS.length];
            Drawable dotDrawable = dot.getBackground().mutate();
            DrawableCompat.setTint(dotDrawable, color);
            dot.setBackground(dotDrawable);
        }
    }
}
