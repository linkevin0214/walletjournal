package com.example.walletjournal.contract;

/**
 * MVP contract for the Main screen: defines what the View exposes to the
 * Presenter, and what the Presenter exposes to the View.
 */
public interface MainContract {

    interface IMain_view extends BaseContract.IBase_View {
        void showGreeting(String greeting);
        void showTextOne(String textOne);
        void showTextSec(String textSec);
        void showTextThr(String textThr);
        void showTextFour(String textFour);
    }

    interface IMain_model extends BaseContract.IBase_Model {
    }

    interface IMain_presenter extends BaseContract.IBase_Presenter<IMain_view> {
        void loadGreeting();
        void loadTextOne();
        void loadTextSec();
        void loadTextThr();
        void loadTextFour();
    }
}
