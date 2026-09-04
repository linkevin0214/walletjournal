package com.example.walletjournal.presenter;

import java.util.List;

import android.content.Context;

import com.example.walletjournal.contract.BudgetsContract;
import com.example.walletjournal.model.AppExecutors;
import com.example.walletjournal.model.BudgetsModel;
import com.example.walletjournal.model.CategoryBudget;

/**
 * Presenter for the Budgets screen. Database read/write happens on a
 * background thread (AppExecutors.diskIO); results are posted back to the
 * main thread.
 */
public class BudgetsPresenter implements BudgetsContract.IBudgets_presenter {

    private BudgetsContract.IBudgets_view view;
    private final BudgetsContract.IBudgets_model model;

    public BudgetsPresenter(Context context) {
        model = new BudgetsModel(context);
    }

    @Override
    public void attachView(BudgetsContract.IBudgets_view view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void loadBudgets() {
        if (view == null) {
            return;
        }
        AppExecutors.diskIO(() -> {
            List<CategoryBudget> budgets = model.getBudgets();
            AppExecutors.mainThread(() -> {
                if (view != null) {
                    view.showBudgets(budgets);
                }
            });
        });
    }

    @Override
    public void save(List<CategoryBudget> budgets) {
        if (view == null) {
            return;
        }
        AppExecutors.diskIO(() -> {
            model.saveBudgets(budgets);
            AppExecutors.mainThread(() -> {
                if (view != null) {
                    view.closeScreen();
                }
            });
        });
    }
}
