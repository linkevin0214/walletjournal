package com.example.walletjournal.view;

import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.walletjournal.R;
import com.example.walletjournal.model.Category;
import com.example.walletjournal.model.CategoryPalette;
import com.example.walletjournal.model.Record;
import com.example.walletjournal.model.RecordType;

/**
 * Binds a list of Record rows into the Records screen's RecyclerView,
 * inserting a date-group header ("今天" / "昨天" / "9月1日"...) whenever the
 * calendar day changes. Records must already be sorted newest first.
 *
 * Each row's icon/color is resolved from the real Category data (built-in
 * or user-created via Add Category) by matching on category name; TRANSFER
 * rows always use a fixed icon since they have no category.
 */
public class RecordsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ROW = 1;

    /** Either a date-header String or a Record. */
    private final List<Object> items = new ArrayList<>();
    private final Map<String, Category> categoriesByName = new HashMap<>();
    private OnRecordClickListener clickListener;

    public interface OnRecordClickListener {
        void onRecordClick(Record record);
    }

    public void setOnRecordClickListener(OnRecordClickListener listener) {
        this.clickListener = listener;
    }

    public void submitList(List<Record> records, List<Category> categories) {
        categoriesByName.clear();
        if (categories != null) {
            for (Category category : categories) {
                categoriesByName.put(category.getName(), category);
            }
        }

        items.clear();
        if (records != null) {
            String lastLabel = null;
            for (Record record : records) {
                String label = dateLabel(record.getCreatedAt());
                if (!label.equals(lastLabel)) {
                    items.add(label);
                    lastLabel = label;
                }
                items.add(record);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_ROW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_record_header, parent, false));
        }
        return new RowViewHolder(inflater.inflate(R.layout.item_record, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) item);
        } else {
            Record record = (Record) item;
            ((RowViewHolder) holder).bind(record, categoriesByName.get(record.getCategory()));
            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onRecordClick(record);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String dateLabel(long millis) {
        if (DateUtils.isToday(millis)) {
            return "今天";
        }
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        if (isSameDay(millis, yesterday.getTimeInMillis())) {
            return "昨天";
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return (cal.get(Calendar.MONTH) + 1) + "月" + cal.get(Calendar.DAY_OF_MONTH) + "日";
    }

    private boolean isSameDay(long a, long b) {
        Calendar calA = Calendar.getInstance();
        calA.setTimeInMillis(a);
        Calendar calB = Calendar.getInstance();
        calB.setTimeInMillis(b);
        return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR)
                && calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR);
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView label;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            label = (TextView) itemView;
        }

        void bind(String text) {
            label.setText(text);
        }
    }

    static class RowViewHolder extends RecyclerView.ViewHolder {

        private final FrameLayout iconCircle;
        private final TextView tvIconEmoji;
        private final TextView tvTitle;
        private final TextView tvSubtitle;
        private final TextView tvAmount;

        RowViewHolder(@NonNull View itemView) {
            super(itemView);
            iconCircle = itemView.findViewById(R.id.item_icon_circle);
            tvIconEmoji = itemView.findViewById(R.id.item_icon_emoji);
            tvTitle = itemView.findViewById(R.id.item_title);
            tvSubtitle = itemView.findViewById(R.id.item_subtitle);
            tvAmount = itemView.findViewById(R.id.item_amount);
        }

        void bind(Record record, Category category) {
            RecordType type = parseType(record.getType());
            String formattedAmount = String.format(Locale.TAIWAN, "%,d", record.getAmount());

            if (type == RecordType.TRANSFER) {
                tvTitle.setText("轉帳至" + safe(record.getToAccount()));
                tvSubtitle.setText(record.getAccount() + " → " + record.getToAccount());
                tvAmount.setText("$" + formattedAmount);
                tvAmount.setTextColor(color(R.color.accounts_text_primary));
                tvIconEmoji.setText("🔁");
                setIconBackground(R.color.icon_bg_green);
            } else {
                tvTitle.setText(record.getCategory());
                tvSubtitle.setText(buildSubtitle(record.getAccount(), record.getNote()));
                if (type == RecordType.INCOME) {
                    tvAmount.setText("+$" + formattedAmount);
                    tvAmount.setTextColor(color(R.color.income_green));
                } else {
                    tvAmount.setText("-$" + formattedAmount);
                    tvAmount.setTextColor(color(R.color.accounts_amount_negative));
                }

                if (category != null) {
                    tvIconEmoji.setText(CategoryPalette.emoji(category.getIcon()));
                    setIconBackgroundTint(CategoryPalette.pastelColor(category.getColorIndex()));
                } else {
                    // No matching category found (e.g. it was renamed/removed) — generic fallback.
                    tvIconEmoji.setText("📌");
                    setIconBackground(R.color.icon_bg_gray);
                }
            }
        }

        private void setIconBackground(int colorRes) {
            setIconBackgroundTint(color(colorRes));
        }

        private void setIconBackgroundTint(int argb) {
            Drawable background = iconCircle.getBackground();
            if (background == null) {
                iconCircle.setBackgroundResource(R.drawable.bg_dot);
                background = iconCircle.getBackground();
            }
            Drawable mutated = background.mutate();
            DrawableCompat.setTint(mutated, argb);
            iconCircle.setBackground(mutated);
        }

        private String buildSubtitle(String account, String note) {
            if (note == null || note.isEmpty()) {
                return account;
            }
            return account + " · " + note;
        }

        private String safe(String value) {
            return value == null ? "" : value;
        }

        private int color(int colorRes) {
            return ContextCompat.getColor(itemView.getContext(), colorRes);
        }

        private RecordType parseType(String raw) {
            if (raw != null) {
                for (RecordType type : RecordType.values()) {
                    if (type.name().equals(raw)) {
                        return type;
                    }
                }
            }
            return RecordType.EXPENSE;
        }
    }
}
