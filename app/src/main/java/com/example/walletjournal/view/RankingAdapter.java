package com.example.walletjournal.view;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.walletjournal.R;
import com.example.walletjournal.model.CategoryStat;

/**
 * Binds a list of CategoryStat rows into the Stats screen's 排行 RecyclerView.
 * Bar width is relative to the top-ranked (largest) amount in the list.
 */
public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {

    private static final int[] RANK_COLORS = {
            0xFFE2572B, // orange-red
            0xFF1E7A5F, // dark green
            0xFF3B82D6, // blue
            0xFFC08A3E, // tan/olive
            0xFF7B5EA7  // purple
    };

    private final List<CategoryStat> stats = new ArrayList<>();
    private long maxAmount = 1;

    public void submitList(List<CategoryStat> newStats) {
        stats.clear();
        if (newStats != null) {
            stats.addAll(newStats);
        }
        maxAmount = 1;
        for (CategoryStat stat : stats) {
            maxAmount = Math.max(maxAmount, stat.getAmount());
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranking, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(stats.get(position), maxAmount);
    }

    @Override
    public int getItemCount() {
        return stats.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvRank;
        private final TextView tvCategory;
        private final TextView tvAmount;
        private final TextView tvChange;
        private final View barFill;
        private final View barRemainder;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.item_rank);
            tvCategory = itemView.findViewById(R.id.item_category);
            tvAmount = itemView.findViewById(R.id.item_amount);
            tvChange = itemView.findViewById(R.id.item_change);
            barFill = itemView.findViewById(R.id.item_bar_fill);
            barRemainder = ((LinearLayout) itemView.findViewById(R.id.item_bar_track)).getChildAt(1);
        }

        void bind(CategoryStat stat, long maxAmount) {
            tvRank.setText(String.valueOf(stat.getRank()));
            tvCategory.setText(stat.getCategory());
            tvAmount.setText("$" + String.format(Locale.TAIWAN, "%,d", stat.getAmount()));

            Integer change = stat.getChangePercent();
            if (change == null) {
                tvChange.setText("--");
                tvChange.setTextColor(color(R.color.accounts_text_secondary));
            } else if (change > 0) {
                tvChange.setText("+" + change + "%");
                tvChange.setTextColor(color(R.color.accounts_amount_negative));
            } else if (change < 0) {
                tvChange.setText(change + "%");
                tvChange.setTextColor(color(R.color.income_green));
            } else {
                tvChange.setText("0%");
                tvChange.setTextColor(color(R.color.accounts_text_secondary));
            }

            int rankColor = RANK_COLORS[(stat.getRank() - 1) % RANK_COLORS.length];
            Drawable fillDrawable = barFill.getBackground().mutate();
            DrawableCompat.setTint(fillDrawable, rankColor);
            barFill.setBackground(fillDrawable);

            int fillWeight = (int) Math.max(4, Math.round(stat.getAmount() * 100.0 / maxAmount));
            fillWeight = Math.min(fillWeight, 100);
            setWeight(barFill, fillWeight);
            setWeight(barRemainder, 100 - fillWeight);
        }

        private void setWeight(View view, int weight) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
            params.weight = weight;
            view.setLayoutParams(params);
        }

        private int color(int colorRes) {
            return ContextCompat.getColor(itemView.getContext(), colorRes);
        }
    }
}
