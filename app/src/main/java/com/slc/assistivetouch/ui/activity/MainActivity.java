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

    public void loadFragment(Bundle bundle) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        MainSettingFragment mainSettingFragment = new MainSettingFragment();
        mainSettingFragment.setArguments(bundle);
        fragmentTransaction.add(R.id.fl_content, mainSettingFragment);
        fragmentTransaction.commit();
    }

}
