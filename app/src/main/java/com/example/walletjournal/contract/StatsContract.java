package com.example.walletjournal.contract;

import java.util.List;

import com.example.walletjournal.model.BudgetProgress;
import com.example.walletjournal.model.CategoryShare;
import com.example.walletjournal.model.CategoryStat;
import com.example.walletjournal.model.MonthlyTotal;

/**
 * MVP contract for the Stats screen (排行 + 趨勢 + 分類 tabs).
 */
public interface StatsContract {

    interface IStats_view extends BaseContract.IBase_View {
        void showRanking(List<CategoryStat> stats, int month);
        void showTrend(List<MonthlyTotal> months);
        void showCategoryShares(List<CategoryShare> shares, int month);

        /** null (or budget <= 0) means hide the budget bar — no budget set for the top category. */
        void showBudgetProgress(BudgetProgress progress);
    }

    interface IStats_model extends BaseContract.IBase_Model {
        /** Top 5 expense categories this month, sorted descending. */
        List<CategoryStat> getExpenseRanking();

        /** 1-12. */
        int getCurrentMonth();

        /** Last 6 months' total expense, oldest first. */
        List<MonthlyTotal> getMonthlyExpenseTrend();

        /** Every expense category this month with its % share of the total, sorted descending. */
        List<CategoryShare> getExpenseCategoryShares();

        /** null when there's no expense data this month. */
        BudgetProgress getTopCategoryBudgetProgress();
    }

    interface IStats_presenter extends BaseContract.IBase_Presenter<IStats_view> {
        void loadRanking();
        void loadTrend();
        void loadCategoryShares();
        void loadBudgetProgress();
    }
}
