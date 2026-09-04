package com.example.walletjournal.view;

import android.graphics.drawable.Drawable;
import android.graphics.Typeface;
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
import com.example.walletjournal.model.MonthlyTotal;

/**
 * Binds a list of MonthlyTotal (oldest first) into the Stats screen's 趨勢
 * horizontal bar chart. Bar height is relative to the largest month shown.
 */
public class TrendAdapter extends RecyclerView.Adapter<TrendAdapter.ViewHolder> {

    private final List<MonthlyTotal> months = new ArrayList<>();
    private long maxTotal = 1;

    public void submitList(List<MonthlyTotal> newMonths) {
        months.clear();
        if (newMonths != null) {
            months.addAll(newMonths);
        }
        maxTotal = 1;
        for (MonthlyTotal month : months) {
            maxTotal = Math.max(maxTotal, month.getTotal());
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trend, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        boolean isCurrentMonth = position == months.size() - 1;
        holder.bind(months.get(position), maxTotal, isCurrentMonth);
    }

    @Override
    public int getItemCount() {
        return months.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvAmount;
        private final View barTrack;
        private final View barFill;
        private final TextView tvLabel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.item_trend_amount);
            barTrack = itemView.findViewById(R.id.item_trend_bar_track);
            barFill = itemView.findViewById(R.id.item_trend_bar_fill);
            tvLabel = itemView.findViewById(R.id.item_trend_label);
        }

        void bind(MonthlyTotal month, long maxTotal, boolean isCurrentMonth) {
            tvAmount.setText(formatShort(month.getTotal()));
            tvLabel.setText(month.getMonth() + "月");
            tvLabel.setTypeface(null, isCurrentMonth ? Typeface.BOLD : Typeface.NORMAL);
            tvLabel.setTextColor(color(isCurrentMonth
                    ? R.color.accounts_text_primary : R.color.accounts_text_secondary));

            int barColorRes = isCurrentMonth ? R.color.trend_bar_current : R.color.trend_bar;
            Drawable fillDrawable = barFill.getBackground().mutate();
            DrawableCompat.setTint(fillDrawable, color(barColorRes));
            barFill.setBackground(fillDrawable);

            int fillWeight = (int) Math.max(2, Math.round(month.getTotal() * 100.0 / maxTotal));
            fillWeight = Math.min(fillWeight, 100);
            setWeight(barFill, fillWeight);
            setWeight(((LinearLayout) barTrack).getChildAt(0), 100 - fillWeight);
        }

        private void setWeight(View view, int weight) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
            params.weight = weight;
            view.setLayoutParams(params);
        }

        private int color(int colorRes) {
            return ContextCompat.getColor(itemView.getContext(), colorRes);
        }

        private String formatShort(long amount) {
            if (amount >= 1_000) {
                return String.format(Locale.TAIWAN, "%.1fk", amount / 1000.0);
            }
            return String.valueOf(amount);
        }
    }
}
