package com.example.walletjournal.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    private RecordsPresenter presenter;

    private TextView tvSummaryLabel;
    private TextView tvSummaryTotal;
    private TextView tvExpenseTotal;
    private TextView tvIncomeTotal;
    private LinearLayout containerFilters;
    private final List<TextView> filterChips = new ArrayList<>();
    private LinearLayout containerTypeFilters;
    private LinearLayout containerDateFilter;

    private RecyclerView rvRecords;
    private RecordsAdapter adapter;
    private TextView tvEmpty;

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

        rvRecords = findViewById(R.id.rv_records);
        rvRecords.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecordsAdapter();
        rvRecords.setAdapter(adapter);

        tvEmpty = findViewById(R.id.tv_empty);

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
    public void showRecords(List<Record> records, List<Category> categories) {
        adapter.submitList(records, categories);
        tvEmpty.setVisibility(records == null || records.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }
}
