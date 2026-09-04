package com.example.walletjournal.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.example.walletjournal.contract.BudgetsContract;

/**
 * Data layer for the Budgets screen. One row per fixed expense category
 * (Categories.EXPENSE); amount is 0 when no budget has been set yet.
 */
public class BudgetsModel implements BudgetsContract.IBudgets_model {

    private final BudgetDao budgetDao;

    public BudgetsModel(Context context) {
        budgetDao = AppDatabase.getInstance(context).budgetDao();
    }

    @Override
    public List<CategoryBudget> getBudgets() {
        List<Budget> saved = budgetDao.getAll();
        List<CategoryBudget> result = new ArrayList<>();
        for (String category : Categories.EXPENSE) {
            result.add(new CategoryBudget(category, findAmount(saved, category)));
        }
        return result;
    }

    @Override
    public void saveBudgets(List<CategoryBudget> budgets) {
        for (CategoryBudget budget : budgets) {
            budgetDao.upsert(new Budget(budget.getCategory(), budget.getAmount()));
        }
    }

    private long findAmount(List<Budget> saved, String category) {
        for (Budget budget : saved) {
            if (budget.getCategory().equals(category)) {
                return budget.getAmount();
            }
        }
        return 0;
    }
}
