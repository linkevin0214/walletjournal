package com.example.walletjournal.contract;

import java.util.List;

import com.example.walletjournal.model.CategoryBudget;

/**
 * MVP contract for the Budgets screen: set a monthly budget per expense category.
 */
public interface BudgetsContract {

    interface IBudgets_view extends BaseContract.IBase_View {
        void showBudgets(List<CategoryBudget> budgets);
        void closeScreen();
    }

    interface IBudgets_model extends BaseContract.IBase_Model {
        /** One row per fixed expense category; amount 0 = not set yet. */
        List<CategoryBudget> getBudgets();

        void saveBudgets(List<CategoryBudget> budgets);
    }

    interface IBudgets_presenter extends BaseContract.IBase_Presenter<IBudgets_view> {
        void loadBudgets();
        void save(List<CategoryBudget> budgets);
    }
}
