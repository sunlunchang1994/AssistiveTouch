package com.slc.assistivetouch.presenter;

import android.os.Bundle;
import android.util.Log;

import com.slc.assistivetouch.contract.MainContract;
import com.slc.assistivetouch.model.SettingConstant;
import com.slc.code.contract.MvpContract.BaseModel;
import com.slc.code.presenter.MvpPresenterImp;

public class MainPresenterImp extends MvpPresenterImp<MainContract.MainView, BaseModel> implements MainContract.MainPresenter {

    public static void initialize(MainContract.MainView view) {
        new MainPresenterImp(view).start();
    }

    private MainPresenterImp(MainContract.MainView view) {
        super(view);
    }

    @Override
    public void start() {
        super.start();
        Bundle bundle = new Bundle();
        bundle.putBoolean(SettingConstant.Ga.KEY_IS_ALLOW_OPEN, SettingConstant.isModuleCheck());
        Log.i("MainPresenterImp",SettingConstant.isModuleCheck()+"");
        bundle.putBoolean(SettingConstant.Ga.KEY_IS_OXYGEN_OS_ROM_OR_H2OS_ROM, SettingConstant.isOxygenOsRomOrH2osRom());
        getView().loadFragment(bundle);
    }
}
