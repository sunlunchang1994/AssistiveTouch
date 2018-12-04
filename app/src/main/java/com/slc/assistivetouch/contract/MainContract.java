package com.slc.assistivetouch.contract;

import com.slc.code.contract.MvpContract.BaseModel;
import com.slc.code.contract.MvpContract.BaseMvpView;
import com.slc.code.contract.MvpContract.BasePresenter;

public interface MainContract {

    interface MainPresenter extends BasePresenter<MainView, BaseModel> {
    }

    interface MainView extends BaseMvpView<MainPresenter> {
        void loadFragment(boolean isOxygenOsRomOrH2OsRom, boolean isAllowOpen);
    }
}
