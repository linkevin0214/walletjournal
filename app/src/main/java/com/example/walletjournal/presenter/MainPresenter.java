package com.example.walletjournal.presenter;

import com.example.walletjournal.contract.MainContract;
import com.example.walletjournal.model.MainModel;

/**
 * Presenter for the Main screen. Mediates between MainModel and the View
 * defined in MainContract.
 */
public class MainPresenter implements MainContract.IMain_presenter {

    private MainContract.IMain_view view;
    private final MainModel model = new MainModel();

    @Override
    public void attachView(MainContract.IMain_view view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void loadGreeting() {
        if (view == null) {
            return;
        }
        view.showGreeting(model.getGreeting());
    }
    @Override
    public void loadTextOne(){
        if(view == null){
            return;
        }
        view.showTextOne(model.getTextOne());
    }
    @Override
    public void loadTextSec(){
        if(view == null){
            return;
        }
        view.showTextSec(model.getTextSec());
    }
    @Override
    public void loadTextThr(){
        if(view == null){
            return;
        }
        view.showTextThr(model.getTextThr());
    }
    @Override
    public void loadTextFour(){
        if(view == null){
            return;
        }
        view.showTextFour(model.getTextFour());
    }
}
