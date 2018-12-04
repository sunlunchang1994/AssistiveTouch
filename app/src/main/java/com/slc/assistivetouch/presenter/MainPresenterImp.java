package com.slc.assistivetouch.presenter;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.widget.Toast;

import com.slc.assistivetouch.R;
import com.slc.assistivetouch.contract.MainContract;
import com.slc.assistivetouch.model.SettingConstant;
import com.slc.code.contract.MvpContract.BaseModel;
import com.slc.code.presenter.MvpPresenterImp;

public class MainPresenterImp extends MvpPresenterImp<MainContract.MainView, BaseModel> implements MainContract.MainPresenter {
    private AlertDialog loadingSettingResponseDialog;
    private Runnable loadingSettingResponseTimeOutRunnable = new Runnable() {
        public void run() {
            if (MainPresenterImp.this.loadingSettingResponseDialog != null && MainPresenterImp.this.loadingSettingResponseDialog.isShowing()) {
                MainPresenterImp.this.loadingSettingResponseDialog.dismiss();
                MainPresenterImp.this.loadingSettingResponseDialog = null;
                MainPresenterImp.this.loadingSettingResponseTimeOutRunnable = null;
                MainPresenterImp.this.mHandler = null;
                getView().loadFragment(false, false);
                Toast.makeText(MainPresenterImp.this.getContext(), R.string.toast_pleas_select_model, Toast.LENGTH_LONG).show();
            }
        }
    };
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (SettingConstant.Ga.ACTION_RESULT_SYSTEM_INFO.equals(intent.getAction())) {
                if (SettingConstant.Ga.KEY_IS_OXYGEN_OS_ROM_OR_H2OS_ROM.equals(intent.getStringExtra(SettingConstant.Ga.EXTRA_KEY))) {
                    MainPresenterImp.this.mHandler.removeCallbacks(MainPresenterImp.this.loadingSettingResponseTimeOutRunnable);
                    MainPresenterImp.this.mHandler = null;
                    MainPresenterImp.this.loadingSettingResponseDialog.dismiss();
                    MainPresenterImp.this.loadingSettingResponseDialog = null;
                    getView().loadFragment(intent.getBooleanExtra(SettingConstant.Ga.EXTRA_VALUE, false), true);
                }
            }
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
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SettingConstant.Ga.ACTION_RESULT_SYSTEM_INFO);
        getContext().registerReceiver(this.mBroadcastReceiver, intentFilter);
        Intent intent = new Intent(SettingConstant.Ga.ACTION_REQUEST_SYSTEM_INFO);
        intent.putExtra(SettingConstant.Ga.EXTRA_KEY, SettingConstant.Ga.KEY_IS_OXYGEN_OS_ROM_OR_H2OS_ROM);
        getContext().sendBroadcast(intent);
        this.mHandler.postDelayed(this.loadingSettingResponseTimeOutRunnable, 3000);
    }

    @Override
    public void destroy() {
        super.destroy();
        getContext().unregisterReceiver(this.mBroadcastReceiver);
    }
}
