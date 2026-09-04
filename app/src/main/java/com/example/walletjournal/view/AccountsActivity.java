package com.example.walletjournal.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.window.SplashScreenView;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.walletjournal.R;
import com.example.walletjournal.contract.AccountsContract;
import com.example.walletjournal.model.Account;
import com.example.walletjournal.presenter.AccountsPresenter;

/**
 * Accounts overview screen: total assets + the full list of account rows,
 * fetched from Room and rendered in a RecyclerView. Also the app's launcher
 * activity (see the manifest), so this is where the cold-start splash screen
 * (Theme.App.Starting) is driven from.
 *
 * This talks to the real platform android.window.SplashScreen (API 31+) rather
 * than the androidx.core:core-splashscreen compat library — this project's
 * ancient Android Gradle Plugin (3.2.0, see the root build.gradle) can't dex
 * that library's classes at all (dexer crash), so there is deliberately no
 * splash on pre-12 devices; API 31+ still gets the full custom background/
 * icon/exit-animation experience straight from Theme.App.Starting.
 */
public class AccountsActivity extends BaseActivity implements AccountsContract.IAccounts_view {

    private AccountsPresenter presenter;

    private TextView tvTotalAssets;
    private RecyclerView rvAccounts;
    private AccountsAdapter adapter;

    /** Keeps the splash screen up until the first account load actually has data to
     *  show, so it cuts straight to a populated list instead of a blank-then-filled
     *  flash — see showAccounts() below. */
    private volatile boolean dataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // The platform dismisses its splash the moment this activity's first frame
            // draws — holding that first draw (via a pre-draw listener that returns
            // false until ready) is how you "keep it on screen" without the compat
            // library's setKeepOnScreenCondition() helper.
            View content = findViewById(android.R.id.content);
            content.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (!dataLoaded) {
                        return false;
                    }
                    content.getViewTreeObserver().removeOnPreDrawListener(this);
                    return true;
                }
            });
            getSplashScreen().setOnExitAnimationListener(this::animateSplashExit);
        }

        presenter = new AccountsPresenter(getApplicationContext());

        tvTotalAssets = findViewById(R.id.tv_total_assets);

        rvAccounts = findViewById(R.id.rv_accounts);
        rvAccounts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AccountsAdapter();
        rvAccounts.setAdapter(adapter);

        findViewById(R.id.btn_add_account).setOnClickListener(v ->
                startActivity(new Intent(this, AddAccountActivity.class)));

        findViewById(R.id.btn_fab_add).setOnClickListener(v ->
                startActivity(new Intent(this, AddRecordActivity.class)));

        findViewById(R.id.tab_list).setOnClickListener(v ->
                startActivity(new Intent(this, RecordsActivity.class)));

        findViewById(R.id.tab_stats).setOnClickListener(v ->
                startActivity(new Intent(this, StatsActivity.class)));

        presenter.attachView(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.loadAccounts();
    }

    @Override
    public void showTotalAssets(String formattedTotal) {
        tvTotalAssets.setText(formattedTotal);
    }

    @Override
    public void showAccounts(List<Account> accounts) {
        dataLoaded = true;
        adapter.submitList(accounts);
    }

    /** Icon pops slightly then the whole splash view fades away, instead of the
     *  system's plain default fade — a bit more "finished product" on the way in. */
    private void animateSplashExit(SplashScreenView splashView) {
        View icon = splashView.getIconView();

        ObjectAnimator popX = ObjectAnimator.ofFloat(icon, View.SCALE_X, 1f, 1.15f, 0f);
        ObjectAnimator popY = ObjectAnimator.ofFloat(icon, View.SCALE_Y, 1f, 1.15f, 0f);
        ObjectAnimator fade = ObjectAnimator.ofFloat(splashView, View.ALPHA, 1f, 1f, 0f);

        AnimatorSet exit = new AnimatorSet();
        exit.playTogether(popX, popY, fade);
        exit.setDuration(450);
        exit.setInterpolator(new AccelerateDecelerateInterpolator());
        exit.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                splashView.remove();
            }
        });
        exit.start();
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }
}
