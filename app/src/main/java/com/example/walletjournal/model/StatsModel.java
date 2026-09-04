package com.example.walletjournal.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;

import com.example.walletjournal.contract.StatsContract;

/**
 * Data layer for the Stats screen's 排行 (ranking) tab. Aggregates this
 * month's EXPENSE records by category and compares each against last
 * month's total for the same category to compute a % change.
 */
public class StatsModel implements StatsContract.IStats_model {

    private static final int MAX_RANKED = 5;
    private static final int TREND_MONTHS = 6;

    private final RecordDao recordDao;
    private final BudgetDao budgetDao;

    public StatsModel(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        recordDao = db.recordDao();
        budgetDao = db.budgetDao();
    }

    @Override
    public List<CategoryStat> getExpenseRanking() {
        long[] currentRange = monthRange(0);
        long[] previousRange = monthRange(-1);

        List<CategoryTotal> current = recordDao.getExpenseCategoryTotals(currentRange[0], currentRange[1]);
        List<CategoryTotal> previous = recordDao.getExpenseCategoryTotals(previousRange[0], previousRange[1]);

        List<CategoryStat> stats = new ArrayList<>();
        int rank = 1;
        for (CategoryTotal row : current) {
            if (rank > MAX_RANKED) {
                break;
            }
            long previousTotal = findTotal(previous, row.category);
            Integer changePercent = previousTotal == 0
                    ? null
                    : (int) Math.round((row.total - previousTotal) * 100.0 / previousTotal);
            stats.add(new CategoryStat(rank, row.category, row.total, changePercent));
            rank++;
        }
        return stats;
    }

    @Override
    public int getCurrentMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    @Override
    public List<CategoryShare> getExpenseCategoryShares() {
        long[] currentRange = monthRange(0);
        List<CategoryTotal> current = recordDao.getExpenseCategoryTotals(currentRange[0], currentRange[1]);

        long grandTotal = 0;
        for (CategoryTotal row : current) {
            grandTotal += row.total;
        }

        List<CategoryShare> shares = new ArrayList<>();
        for (CategoryTotal row : current) {
            double percent = grandTotal == 0 ? 0 : row.total * 100.0 / grandTotal;
            shares.add(new CategoryShare(row.category, row.total, percent));
        }
        return shares;
    }

    @Override
    public List<MonthlyTotal> getMonthlyExpenseTrend() {
        List<MonthlyTotal> months = new ArrayList<>();
        // Oldest first (TREND_MONTHS-1 months ago) through newest (this month).
        for (int offset = -(TREND_MONTHS - 1); offset <= 0; offset++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, offset);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;

            long[] range = monthRange(offset);
            Long total = recordDao.getExpenseTotal(range[0], range[1]);
            months.add(new MonthlyTotal(year, month, total == null ? 0 : total));
        }
        return months;
    }

    @Override
    public BudgetProgress getTopCategoryBudgetProgress() {
        List<CategoryShare> shares = getExpenseCategoryShares();
        if (shares.isEmpty()) {
            return null;
        }
        CategoryShare top = shares.get(0);
        Budget budget = budgetDao.getByCategory(top.getCategory());
        long budgetAmount = budget == null ? 0 : budget.getAmount();
        return new BudgetProgress(top.getCategory(), top.getAmount(), budgetAmount);
    }

    private long findTotal(List<CategoryTotal> list, String category) {
        for (CategoryTotal item : list) {
            if (item.category.equals(category)) {
                return item.total;
            }
        }
        return 0;
    }

    /** monthOffset=0 -> current month, -1 -> previous month. Returns [startInclusive, endExclusive) millis. */
    private long[] monthRange(int monthOffset) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, monthOffset);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, 1);
        long end = cal.getTimeInMillis();
        return new long[]{start, end};
    }
}
