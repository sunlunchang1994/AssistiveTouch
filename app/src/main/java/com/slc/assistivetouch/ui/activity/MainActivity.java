package com.slc.assistivetouch.ui.activity;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.slc.assistivetouch.R;
import com.slc.assistivetouch.contract.MainContract;
import com.slc.assistivetouch.model.SettingConstant;
import com.slc.assistivetouch.presenter.MainPresenterImp;
import com.slc.assistivetouch.ui.fragment.MainSettingFragment;
import com.slc.code.ui.activity.NativeToolBarActivity;

public class MainActivity extends NativeToolBarActivity<MainContract.MainPresenter> implements MainContract.MainView {
    public void onBindView(@Nullable Bundle savedInstanceState) {
    }

    @Override
    public Object setDeveloperView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initPresenter() {
        super.initPresenter();
        MainPresenterImp.initialize(this);
    }

    @Override
    public void loadFragment(boolean isOxygenOsRomOrH2OsRom, boolean isAllowOpen) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        MainSettingFragment mainSettingFragment = new MainSettingFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(SettingConstant.Ga.KEY_IS_OXYGEN_OS_ROM_OR_H2OS_ROM, isOxygenOsRomOrH2OsRom);
        bundle.putBoolean(SettingConstant.Ga.KEY_IS_ALLOW_OPEN, isAllowOpen);
        mainSettingFragment.setArguments(bundle);
        fragmentTransaction.add(R.id.fl_content, mainSettingFragment);
        fragmentTransaction.commit();
    }
}
