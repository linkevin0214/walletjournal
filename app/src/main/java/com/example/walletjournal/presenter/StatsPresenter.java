package com.example.walletjournal.presenter;

import java.util.List;

import android.content.Context;

import com.example.walletjournal.contract.StatsContract;
import com.example.walletjournal.model.AppExecutors;
import com.example.walletjournal.model.BudgetProgress;
import com.example.walletjournal.model.CategoryShare;
import com.example.walletjournal.model.CategoryStat;
import com.example.walletjournal.model.MonthlyTotal;
import com.example.walletjournal.model.StatsModel;

/**
 * Presenter for the Stats screen's 排行 tab. The aggregation query runs on a
 * background thread (AppExecutors.diskIO) and results are posted back to
 * the main thread.
 */
public class StatsPresenter implements StatsContract.IStats_presenter {

    private StatsContract.IStats_view view;
    private final StatsContract.IStats_model model;

    public StatsPresenter(Context context) {
        model = new StatsModel(context);
    }

    @Override
    public void attachView(StatsContract.IStats_view view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void loadRanking() {
        if (view == null) {
            return;
        }
        AppExecutors.diskIO(() -> {
            List<CategoryStat> stats = model.getExpenseRanking();
            int month = model.getCurrentMonth();
            AppExecutors.mainThread(() -> {
                if (view != null) {
                    view.showRanking(stats, month);
                }
            });
        });
    }

    @Override
    public void loadTrend() {
        if (view == null) {
            return;
        }
        AppExecutors.diskIO(() -> {
            List<MonthlyTotal> months = model.getMonthlyExpenseTrend();
            AppExecutors.mainThread(() -> {
                if (view != null) {
                    view.showTrend(months);
                }
            });
        });
    }

    @Override
    public void loadCategoryShares() {
        if (view == null) {
            return;
        }
        AppExecutors.diskIO(() -> {
            List<CategoryShare> shares = model.getExpenseCategoryShares();
            int month = model.getCurrentMonth();
            AppExecutors.mainThread(() -> {
                if (view != null) {
                    view.showCategoryShares(shares, month);
                }
            });
        });
    }

    @Override
    public void loadBudgetProgress() {
        if (view == null) {
            return;
        }
        AppExecutors.diskIO(() -> {
            BudgetProgress progress = model.getTopCategoryBudgetProgress();
            AppExecutors.mainThread(() -> {
                if (view != null) {
                    view.showBudgetProgress(progress);
                }
            });
        });
    }
}
