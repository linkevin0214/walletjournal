package com.example.walletjournal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.walletjournal.contract.MainContract;
import com.example.walletjournal.presenter.MainPresenter;
import com.example.walletjournal.view.AccountsActivity;
import com.example.walletjournal.view.BaseActivity;

public class MainActivity extends BaseActivity implements MainContract.IMain_view {
    public TextView tv1;
    public TextView tv2;
    public TextView tv3;
    public TextView tv4;
    private final MainPresenter presenter = new MainPresenter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv1 = findViewById(R.id.textview);
        tv2 = findViewById(R.id.textview2);
        tv3 = findViewById(R.id.textview3);
        tv4 = findViewById(R.id.textview4);
        presenter.attachView(this);
        presenter.loadGreeting();
        presenter.loadTextOne();
        presenter.loadTextSec();
        presenter.loadTextThr();
        presenter.loadTextFour();

        findViewById(R.id.btn_go_accounts).setOnClickListener(v ->
                startActivity(new Intent(this, AccountsActivity.class)));
    }

    @Override
    public void showGreeting(String greeting) {
        showError(greeting);
    }
    @Override
    public void showTextOne(String text1){
        tv1.setText(text1);
        showError(text1);
    }
    @Override
    public void showTextSec(String text2){
        tv2.setText(text2);
        showError(text2);
    }
    @Override
    public void showTextThr(String text3){
        tv3.setText(text3);
        showError(text3);
    }
    @Override
    public void showTextFour(String text4){
        tv4.setText(text4);
        showError(text4);
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }
}
