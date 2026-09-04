package com.example.walletjournal.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.walletjournal.R;
import com.example.walletjournal.contract.RecordsContract;
import com.example.walletjournal.model.Account;
import com.example.walletjournal.model.Category;
import com.example.walletjournal.model.Record;
import com.example.walletjournal.model.RecordType;
import com.example.walletjournal.presenter.RecordsPresenter;

/**
 * Records list screen: this month's expense total (filterable by account,
 * type, and date range) + every expense/income/transfer entry, grouped by
 * day, newest first.
 */
public class RecordsActivity extends BaseActivity implements RecordsContract.IRecords_view {

    private static final long DAY_MILLIS = 24L * 60 * 60 * 1000;

    /** Room's own query is sub-millisecond even at 1000+ rows (see the index change),
     *  so without this floor the loading spinner's show→hide round trip routinely
     *  finishes before the very first frame is even drawn — the spinner is
     *  technically shown, but never actually appears on screen. Only the first load
     *  after onResume() (see firstLoadPending) is held to this floor; a filter/search
     *  change or a later reload just updates instantly, no spinner involved. */
    private static final long MIN_LOADING_MS = 300;

    private RecordsPresenter presenter;

    private TextView tvSummaryLabel;
    private TextView tvSummaryTotal;
    private TextView tvExpenseTotal;
    private TextView tvIncomeTotal;
    private LinearLayout containerFilters;
    private final List<TextView> filterChips = new ArrayList<>();
    private LinearLayout containerTypeFilters;
    private LinearLayout containerDateFilter;

    private EditText etSearch;
    private TextView btnClearSearch;

    private RecyclerView rvRecords;
    private RecordsAdapter adapter;
    private View layoutEmpty;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;
    private View layoutLoading;
    private long loadStartMillis;
    private boolean firstLoadPending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        presenter = new RecordsPresenter(getApplicationContext());

        tvSummaryLabel = findViewById(R.id.tv_summary_label);
        tvSummaryTotal = findViewById(R.id.tv_summary_total);
        tvExpenseTotal = findViewById(R.id.tv_expense_total);
        tvIncomeTotal = findViewById(R.id.tv_income_total);
        containerFilters = findViewById(R.id.container_filters);
        containerTypeFilters = findViewById(R.id.container_type_filters);
        containerDateFilter = findViewById(R.id.container_date_filter);

        etSearch = findViewById(R.id.et_search);
        btnClearSearch = findViewById(R.id.btn_clear_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                presenter.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (isEnterPressed(actionId, event, EditorInfo.IME_ACTION_SEARCH)) {
                hideKeyboard(v);
                v.clearFocus();
                return true;
            }
            return false;
        });
        btnClearSearch.setOnClickListener(v -> etSearch.setText(""));

        rvRecords = findViewById(R.id.rv_records);
        rvRecords.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecordsAdapter();
        adapter.setOnRecordClickListener(record -> {
            Intent intent = new Intent(this, AddRecordActivity.class);
            intent.putExtra(AddRecordActivity.EXTRA_RECORD_ID, record.getId());
            startActivity(intent);
        });
        rvRecords.setAdapter(adapter);
        new ItemTouchHelper(new RecordSwipeCallback()).attachToRecyclerView(rvRecords);

        layoutEmpty = findViewById(R.id.layout_empty);
        tvEmptyTitle = findViewById(R.id.tv_empty_title);
        tvEmptySubtitle = findViewById(R.id.tv_empty_subtitle);
        layoutLoading = findViewById(R.id.layout_loading);

        // Already on "清單" — tapping it again is a no-op.
        findViewById(R.id.tab_accounts).setOnClickListener(v -> finish());

        findViewById(R.id.btn_fab_add).setOnClickListener(v ->
                startActivity(new Intent(this, AddRecordActivity.class)));

        findViewById(R.id.tab_stats).setOnClickListener(v ->
                startActivity(new Intent(this, StatsActivity.class)));

        presenter.attachView(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firstLoadPending) {
            loadStartMillis = System.currentTimeMillis();
        }
        presenter.loadData();
    }

    @Override
    public void showFilters(List<Account> accounts, int selectedIndex) {
        containerFilters.removeAllViews();
        filterChips.clear();

        LayoutInflater inflater = LayoutInflater.from(this);

        TextView allChip = (TextView) inflater.inflate(R.layout.item_filter_chip, containerFilters, false);
        allChip.setText("全部帳戶");
        int allIndex = 0;
        allChip.setOnClickListener(v -> presenter.selectFilter(allIndex));
        containerFilters.addView(allChip);
        filterChips.add(allChip);

        for (int i = 0; i < accounts.size(); i++) {
            TextView chip = (TextView) inflater.inflate(R.layout.item_filter_chip, containerFilters, false);
            chip.setText(accounts.get(i).getTitle());
            int index = i + 1;
            chip.setOnClickListener(v -> presenter.selectFilter(index));
            containerFilters.addView(chip);
            filterChips.add(chip);
        }

        for (int i = 0; i < filterChips.size(); i++) {
            boolean selected = i == selectedIndex;
            TextView chip = filterChips.get(i);
            chip.setBackgroundResource(selected ? R.drawable.bg_filter_pill : 0);
            chip.setTextColor(getColor(selected ? R.color.white : R.color.header_text_muted));
        }
    }

    @Override
    public void showTypeFilters(RecordType selectedType) {
        containerTypeFilters.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        addTypeChip(inflater, "全部類型", null, selectedType);
        addTypeChip(inflater, "支出", RecordType.EXPENSE, selectedType);
        addTypeChip(inflater, "收入", RecordType.INCOME, selectedType);
    }

    private void addTypeChip(LayoutInflater inflater, String label, RecordType type, RecordType selectedType) {
        TextView chip = (TextView) inflater.inflate(R.layout.item_filter_chip, containerTypeFilters, false);
        chip.setText(label);
        boolean selected = type == selectedType;
        chip.setBackgroundResource(selected ? R.drawable.bg_filter_pill : 0);
        chip.setTextColor(getColor(selected ? R.color.white : R.color.header_text_muted));
        chip.setOnClickListener(v -> presenter.selectTypeFilter(type));
        containerTypeFilters.addView(chip);
    }

    @Override
    public void showDateFilter(long startMillis, long endMillis) {
        containerDateFilter.removeAllViews();

        boolean active = startMillis >= 0;
        LayoutInflater inflater = LayoutInflater.from(this);

        TextView dateChip = (TextView) inflater.inflate(R.layout.item_filter_chip, containerDateFilter, false);
        dateChip.setText(active ? formatRange(startMillis, endMillis) : "全部日期");
        dateChip.setBackgroundResource(active ? R.drawable.bg_filter_pill : 0);
        dateChip.setTextColor(getColor(active ? R.color.white : R.color.header_text_muted));
        dateChip.setOnClickListener(v -> openDateRangePicker(startMillis));
        containerDateFilter.addView(dateChip);

        if (active) {
            TextView clearChip = (TextView) inflater.inflate(R.layout.item_filter_chip, containerDateFilter, false);
            clearChip.setText("✕");
            clearChip.setOnClickListener(v -> presenter.clearDateRange());
            containerDateFilter.addView(clearChip);
        }
    }

    private String formatRange(long startMillis, long endMillis) {
        SimpleDateFormat format = new SimpleDateFormat("M/d", Locale.TAIWAN);
        return format.format(new Date(startMillis)) + " - " + format.format(new Date(endMillis));
    }

    /** Picks a start day, then an end day, then applies the range (either pick order works). */
    private void openDateRangePicker(long currentStart) {
        Calendar initial = Calendar.getInstance();
        if (currentStart >= 0) {
            initial.setTimeInMillis(currentStart);
        }
        new DatePickerDialog(this, (picker, year1, month1, day1) -> {
            long dayOneStart = startOfDay(year1, month1, day1);
            new DatePickerDialog(this, (picker2, year2, month2, day2) -> {
                long dayTwoStart = startOfDay(year2, month2, day2);
                long rangeStart = Math.min(dayOneStart, dayTwoStart);
                long laterDayStart = Math.max(dayOneStart, dayTwoStart);
                presenter.selectDateRange(rangeStart, laterDayStart + DAY_MILLIS - 1);
            }, year1, month1, day1).show();
        }, initial.get(Calendar.YEAR), initial.get(Calendar.MONTH), initial.get(Calendar.DAY_OF_MONTH)).show();
    }

    private long startOfDay(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    @Override
    public void showSummary(String periodLabel, long totalExpense, long totalIncome) {
        long balance = totalIncome - totalExpense;
        String balanceSign = balance < 0 ? "-NT$ " : "NT$ ";

        tvSummaryLabel.setText(periodLabel);
        tvSummaryTotal.setText(balanceSign + String.format(Locale.TAIWAN, "%,d", Math.abs(balance)));
        tvExpenseTotal.setText("-$" + String.format(Locale.TAIWAN, "%,d", totalExpense));
        tvIncomeTotal.setText("+$" + String.format(Locale.TAIWAN, "%,d", totalIncome));
    }

    @Override
    public void showRecords(List<Record> records, List<Category> categories, boolean hasAnyRecords) {
        if (!firstLoadPending) {
            applyRecords(records, categories, hasAnyRecords);
            return;
        }
        firstLoadPending = false;
        long elapsed = System.currentTimeMillis() - loadStartMillis;
        long remaining = Math.max(0, MIN_LOADING_MS - elapsed);
        layoutLoading.postDelayed(() -> applyRecords(records, categories, hasAnyRecords), remaining);
    }

    private void applyRecords(List<Record> records, List<Category> categories, boolean hasAnyRecords) {
        layoutLoading.setVisibility(View.GONE);
        adapter.submitList(records, categories);

        boolean empty = records == null || records.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (empty) {
            if (hasAnyRecords) {
                // A filter/search matched nothing — tapping the FAB wouldn't fix that.
                tvEmptyTitle.setText("找不到符合的紀錄");
                tvEmptySubtitle.setText("試試清除搜尋或篩選條件");
            } else {
                // True cold start: nothing has ever been entered yet.
                tvEmptyTitle.setText("還沒有任何紀錄");
                tvEmptySubtitle.setText("點擊中間的「+」開始記帳");
            }
        }
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    /**
     * Swipe left or right on a row to delete it — header rows ("今天" etc.) opt out
     * via getSwipeDirs(). A confirmation dialog gates the actual delete since this is
     * destructive; declining (or dismissing) snaps the row back with notifyItemChanged
     * rather than actually removing anything from the adapter's data.
     */
    private class RecordSwipeCallback extends ItemTouchHelper.SimpleCallback {

        RecordSwipeCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                               @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int position = viewHolder.getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION || adapter.getRecordAt(position) == null) {
                return 0;
            }
            return super.getSwipeDirs(recyclerView, viewHolder);
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getBindingAdapterPosition();
            Record record = position == RecyclerView.NO_POSITION ? null : adapter.getRecordAt(position);
            if (record == null) {
                // Shouldn't happen — getSwipeDirs() opts headers out — but reset the
                // visual swipe rather than leaving the row stuck off-screen if it does.
                if (position != RecyclerView.NO_POSITION) {
                    adapter.notifyItemChanged(position);
                }
                return;
            }
            new AlertDialog.Builder(RecordsActivity.this)
                    .setTitle("刪除紀錄")
                    .setMessage("確定要刪除這筆紀錄嗎？此動作無法復原。")
                    .setNegativeButton("取消", (dialog, which) -> adapter.notifyItemChanged(position))
                    .setOnCancelListener(dialog -> adapter.notifyItemChanged(position))
                    .setPositiveButton("刪除", (dialog, which) -> presenter.deleteRecord(record))
                    .show();
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                 @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState,
                                 boolean isCurrentlyActive) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX != 0) {
                View itemView = viewHolder.itemView;
                int verticalInset = dp(6);
                float cornerRadius = dp(14);
                float centerY = itemView.getTop() + itemView.getHeight() / 2f;
                // A fixed anchor near the edge being dragged from — clipped to the
                // currently-revealed strip so the icon/label never draw past it early
                // in the gesture, and settle into place as the swipe continues.
                float anchorX = dX > 0 ? itemView.getLeft() + dp(30) : itemView.getRight() - dp(30);

                RectF rect = dX > 0
                        ? new RectF(itemView.getLeft(), itemView.getTop() + verticalInset,
                                dX, itemView.getBottom() - verticalInset)
                        : new RectF(itemView.getRight() + dX, itemView.getTop() + verticalInset,
                                itemView.getRight(), itemView.getBottom() - verticalInset);

                Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
                background.setColor(ContextCompat.getColor(RecordsActivity.this, R.color.accounts_amount_negative));
                c.drawRoundRect(rect, cornerRadius, cornerRadius, background);

                c.save();
                c.clipRect(rect);

                Paint icon = new Paint(Paint.ANTI_ALIAS_FLAG);
                icon.setTextAlign(Paint.Align.CENTER);
                icon.setTextSize(itemView.getHeight() * 0.36f);
                float iconY = centerY - dp(7) - (icon.descent() + icon.ascent()) / 2f;
                c.drawText("🗑", anchorX, iconY, icon);

                Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
                label.setColor(Color.WHITE);
                label.setFakeBoldText(true);
                label.setTextAlign(Paint.Align.CENTER);
                label.setTextSize(dp(11));
                float labelY = centerY + dp(15) - (label.descent() + label.ascent()) / 2f;
                c.drawText("刪除", anchorX, labelY, label);

                c.restore();
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }
}
