package com.example.walletjournal.view;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.walletjournal.R;
import com.example.walletjournal.contract.StatsContract;
import com.example.walletjournal.model.BudgetProgress;
import com.example.walletjournal.model.CategoryShare;
import com.example.walletjournal.model.CategoryStat;
import com.example.walletjournal.model.MonthlyTotal;
import com.example.walletjournal.presenter.StatsPresenter;

/**
 * Stats screen. 排行、趨勢、分類 are all fully wired against Room.
 */
public class StatsActivity extends BaseActivity implements StatsContract.IStats_view {

    private StatsPresenter presenter;

    private TextView tabCategory;
    private TextView tabTrend;
    private TextView tabRanking;

    private View panelCategory;
    private View panelTrend;
    private View panelRanking;

    private TextView tvRankingTitle;
    private RecyclerView rvRanking;
    private RankingAdapter rankingAdapter;
    private TextView tvRankingEmpty;

    private RecyclerView rvTrend;
    private TrendAdapter trendAdapter;
    private TextView tvTrendEmpty;

    private TextView tvCategoryTitle;
    private DonutChartView donutChart;
    private TextView tvCategoryTotal;
    private RecyclerView rvCategoryShare;
    private CategoryShareAdapter categoryShareAdapter;
    private TextView tvCategoryEmpty;

    private View sectionBudget;
    private TextView tvBudgetLabel;
    private TextView tvBudgetAmount;
    private View budgetBarFill;

    /** Index of the currently shown tab: 0=分類, 1=趨勢, 2=排行. */
    private int currentTab = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        presenter = new StatsPresenter(getApplicationContext());

        tabCategory = findViewById(R.id.tab_category);
        tabTrend = findViewById(R.id.tab_trend);
        tabRanking = findViewById(R.id.tab_ranking);

        panelCategory = findViewById(R.id.panel_category);
        panelTrend = findViewById(R.id.panel_trend);
        panelRanking = findViewById(R.id.panel_ranking);

        tvRankingTitle = findViewById(R.id.tv_ranking_title);
        rvRanking = findViewById(R.id.rv_ranking);
        rvRanking.setLayoutManager(new LinearLayoutManager(this));
        rankingAdapter = new RankingAdapter();
        rvRanking.setAdapter(rankingAdapter);
        tvRankingEmpty = findViewById(R.id.tv_ranking_empty);

        rvTrend = findViewById(R.id.rv_trend);
        rvTrend.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        trendAdapter = new TrendAdapter();
        rvTrend.setAdapter(trendAdapter);
        tvTrendEmpty = findViewById(R.id.tv_trend_empty);

        tvCategoryTitle = findViewById(R.id.tv_category_title);
        donutChart = findViewById(R.id.donut_chart);
        tvCategoryTotal = findViewById(R.id.tv_category_total);
        rvCategoryShare = findViewById(R.id.rv_category_share);
        rvCategoryShare.setLayoutManager(new LinearLayoutManager(this));
        categoryShareAdapter = new CategoryShareAdapter();
        rvCategoryShare.setAdapter(categoryShareAdapter);
        tvCategoryEmpty = findViewById(R.id.tv_category_empty);

        sectionBudget = findViewById(R.id.section_budget);
        tvBudgetLabel = findViewById(R.id.tv_budget_label);
        tvBudgetAmount = findViewById(R.id.tv_budget_amount);
        budgetBarFill = findViewById(R.id.budget_bar_fill);
        findViewById(R.id.btn_edit_budget).setOnClickListener(v ->
                startActivity(new Intent(this, BudgetsActivity.class)));

        tabCategory.setOnClickListener(v -> selectTab(0));
        tabTrend.setOnClickListener(v -> selectTab(1));
        tabRanking.setOnClickListener(v -> selectTab(2));

        // Left/right swipe anywhere in the panel area switches tabs too, same
        // order as the segmented control (分類 → 趨勢 → 排行).
        SwipeableFrameLayout contentPanels = findViewById(R.id.content_stats_panels);
        // rv_trend scrolls horizontally itself — let it own drags that start on it.
        contentPanels.setHorizontalScrollExclusionView(rvTrend);
        contentPanels.setOnSwipeListener(new SwipeableFrameLayout.OnSwipeListener() {
            @Override
            public void onSwipeLeft() {
                selectTab(Math.max(currentTab - 1, 0));
            }

            @Override
            public void onSwipeRight() {
                selectTab(Math.min(currentTab + 1, 2));
            }
        });

        findViewById(R.id.tab_list).setOnClickListener(v ->
                startActivity(new Intent(this, RecordsActivity.class)));
        findViewById(R.id.tab_accounts).setOnClickListener(v -> finish());
        findViewById(R.id.btn_fab_add).setOnClickListener(v ->
                startActivity(new Intent(this, AddRecordActivity.class)));

        selectTab(2);

        presenter.attachView(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.loadRanking();
        presenter.loadTrend();
        presenter.loadCategoryShares();
        presenter.loadBudgetProgress();
    }

    private void selectTab(int index) {
        currentTab = index;

        tabCategory.setBackgroundResource(index == 0 ? R.drawable.bg_segment_selected : 0);
        tabTrend.setBackgroundResource(index == 1 ? R.drawable.bg_segment_selected : 0);
        tabRanking.setBackgroundResource(index == 2 ? R.drawable.bg_segment_selected : 0);

        tabCategory.setTextColor(getColor(index == 0 ? R.color.accounts_text_primary : R.color.accounts_text_secondary));
        tabTrend.setTextColor(getColor(index == 1 ? R.color.accounts_text_primary : R.color.accounts_text_secondary));
        tabRanking.setTextColor(getColor(index == 2 ? R.color.accounts_text_primary : R.color.accounts_text_secondary));

        tabCategory.setTypeface(null, index == 0 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tabTrend.setTypeface(null, index == 1 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tabRanking.setTypeface(null, index == 2 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        panelCategory.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        panelTrend.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        panelRanking.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showRanking(List<CategoryStat> stats, int month) {
        tvRankingTitle.setText(month + "月支出排行");
        rankingAdapter.submitList(stats);
        tvRankingEmpty.setVisibility(stats == null || stats.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showTrend(List<MonthlyTotal> months) {
        trendAdapter.submitList(months);

        boolean allZero = true;
        if (months != null) {
            for (MonthlyTotal month : months) {
                if (month.getTotal() != 0) {
                    allZero = false;
                    break;
                }
            }
        }
        tvTrendEmpty.setVisibility(allZero ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showCategoryShares(List<CategoryShare> shares, int month) {
        tvCategoryTitle.setText(month + "月支出分佈");
        categoryShareAdapter.submitList(shares);
        tvCategoryEmpty.setVisibility(shares == null || shares.isEmpty() ? View.VISIBLE : View.GONE);

        long total = 0;
        List<DonutChartView.Segment> segments = new ArrayList<>();
        if (shares != null) {
            for (int i = 0; i < shares.size(); i++) {
                CategoryShare share = shares.get(i);
                total += share.getAmount();
                int color = CategoryShareAdapter.COLORS[i % CategoryShareAdapter.COLORS.length];
                segments.add(new DonutChartView.Segment((float) share.getPercent(), color));
            }
        }
        donutChart.setSegments(segments);
        tvCategoryTotal.setText("$" + String.format(Locale.TAIWAN, "%,d", total));
    }

    @Override
    public void showBudgetProgress(BudgetProgress progress) {
        if (progress == null || progress.getBudget() <= 0) {
            sectionBudget.setVisibility(View.GONE);
            return;
        }
        sectionBudget.setVisibility(View.VISIBLE);
        tvBudgetLabel.setText(progress.getCategory() + "預算");
        tvBudgetAmount.setText("$" + String.format(Locale.TAIWAN, "%,d", progress.getSpent())
                + " / $" + String.format(Locale.TAIWAN, "%,d", progress.getBudget()));

        int fillWeight = (int) Math.round(progress.getSpent() * 100.0 / progress.getBudget());
        fillWeight = Math.max(0, Math.min(fillWeight, 100));

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) budgetBarFill.getLayoutParams();
        params.weight = fillWeight;
        budgetBarFill.setLayoutParams(params);

        int barColorRes = progress.getSpent() > progress.getBudget()
                ? R.color.accounts_amount_negative : R.color.trend_bar_current;
        Drawable fillDrawable = budgetBarFill.getBackground().mutate();
        DrawableCompat.setTint(fillDrawable, getColor(barColorRes));
        budgetBarFill.setBackground(fillDrawable);
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }
}
