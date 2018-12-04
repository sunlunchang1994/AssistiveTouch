package com.slc.assistivetouch.presenter;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.slc.assistivetouch.R;
import com.slc.assistivetouch.contract.MainContract;
import com.slc.assistivetouch.model.SettingConstant;
import com.slc.code.contract.MvpContract.BaseModel;
import com.slc.code.presenter.MvpPresenterImp;
import com.slc.code.receiver.ImmediatelyBroadcastReceiver;

import java.util.Set;

public class MainPresenterImp extends MvpPresenterImp<MainContract.MainView, BaseModel> implements MainContract.MainPresenter {
    private AlertDialog loadingSettingResponseDialog;
    private MainContract.LoadingSettingResponseTimeOutRunnable loadingSettingResponseTimeOutRunnable = new MainContract.LoadingSettingResponseTimeOutRunnable() {
        @Override
        public void run() {
            super.run();
            if (MainPresenterImp.this.loadingSettingResponseDialog != null && MainPresenterImp.this.loadingSettingResponseDialog.isShowing()) {
                getContext().unregisterReceiver(MainPresenterImp.this.mBroadcastReceiver);
                MainPresenterImp.this.loadingSettingResponseDialog.dismiss();
                MainPresenterImp.this.loadingSettingResponseDialog = null;
                MainPresenterImp.this.loadingSettingResponseTimeOutRunnable = null;
                MainPresenterImp.this.mHandler = null;
                getView().loadFragment(bundle);
            }
        }
    };

    private ImmediatelyBroadcastReceiver mBroadcastReceiver =
            new ImmediatelyBroadcastReceiver(SettingConstant.Ga.ACTION_REQUEST_SYSTEM_INFO, SettingConstant.Ga.ACTION_RESULT_SYSTEM_INFO) {

                @Override
                public boolean handlerMsg(Set<String> extraKeySet, Bundle extrasBundle) {
                    for (String key : extraKeySet) {
                        if (SettingConstant.Ga.KEY_IS_OXYGEN_OS_ROM_OR_H2OS_ROM.equals(key)) {
                            Bundle bundle = new Bundle();
                            bundle.putBoolean(SettingConstant.Ga.KEY_IS_OXYGEN_OS_ROM_OR_H2OS_ROM, extrasBundle.getBoolean(SettingConstant.Ga.KEY_IS_OXYGEN_OS_ROM_OR_H2OS_ROM));
                            bundle.putBoolean(SettingConstant.Ga.KEY_IS_ALLOW_OPEN, true);
                            MainPresenterImp.this.getContext().runOnUiThread(MainPresenterImp.this.loadingSettingResponseTimeOutRunnable.setBundle(bundle)
                                    .setOnRunBeforeListener(new MainContract.LoadingSettingResponseTimeOutRunnable.OnRunBeforeListener() {
                                        @Override
                                        public void onRunBefore() {
                                            MainPresenterImp.this.mHandler.removeCallbacks(MainPresenterImp.this.loadingSettingResponseTimeOutRunnable);
                                        }
                                    }));
                        }
                    }
                    return false;
                }

                @Override
                protected void onOtherReceive(Context context, Intent intent) {

                }
            };
    private Handler mHandler = new Handler();

    public static void initialize(MainContract.MainView view) {
        new MainPresenterImp(view).start();
    }

    private MainPresenterImp(MainContract.MainView view) {
        super(view);
    }

    @Override
    public void start() {
        super.start();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.label_loading_setting_response);
        this.loadingSettingResponseDialog = builder.create();
        this.loadingSettingResponseDialog.show();
        //显示dialog
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SettingConstant.Ga.ACTION_RESULT_SYSTEM_INFO);
        getContext().registerReceiver(this.mBroadcastReceiver, intentFilter);
        //注册接收广播
        mBroadcastReceiver.addSendInfo(SettingConstant.Ga.KEY_IS_OXYGEN_OS_ROM_OR_H2OS_ROM);
        mBroadcastReceiver.sendMsg(getContext());
        Intent intent = new Intent(SettingConstant.Ga.ACTION_REQUEST_SYSTEM_INFO);
        intent.putExtra(SettingConstant.Ga.EXTRA_KEY, SettingConstant.Ga.KEY_IS_OXYGEN_OS_ROM_OR_H2OS_ROM);
        getContext().sendBroadcast(intent);
        //发送广播
        Bundle bundle = new Bundle();
        bundle.putBoolean(SettingConstant.Ga.KEY_IS_OXYGEN_OS_ROM_OR_H2OS_ROM, false);
        bundle.putBoolean(SettingConstant.Ga.KEY_IS_ALLOW_OPEN, false);
        this.mHandler.postDelayed(this.loadingSettingResponseTimeOutRunnable.setBundle(bundle)
                .setOnRunBeforeListener(new MainContract.LoadingSettingResponseTimeOutRunnable.OnRunBeforeListener() {
                    @Override
                    public void onRunBefore() {
                        Toast.makeText(MainPresenterImp.this.getContext(), R.string.toast_pleas_select_model, Toast.LENGTH_LONG).show();
                    }
                }), 3000);
        //发送接收广播超时监听
    }
}
