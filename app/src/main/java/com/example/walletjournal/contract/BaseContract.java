package com.example.walletjournal.contract;


/**
 * Base contract every screen's Contract (e.g. MainContract) extends,
 * so each screen only needs to add what's specific to it.
 */
public interface BaseContract {

    interface IBase_Model extends BaseContract {
    }

    interface IBase_View extends BaseContract {
        void showLoading();
        void hideLoading();
        void showError(String message);
    }

    interface IBase_Presenter<V extends IBase_View> extends BaseContract {
        void attachView(V view);
        void detachView();
    }
}
