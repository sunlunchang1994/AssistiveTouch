package com.slc.assistivetouch.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.slc.assistivetouch.R;
import com.slc.assistivetouch.contract.MainSettingContract;
import com.slc.assistivetouch.contract.MainSettingContract;
import com.slc.assistivetouch.contract.MainSettingContract;
import com.slc.assistivetouch.contract.MainSettingContract;
import com.slc.assistivetouch.model.SettingConstant;
import com.slc.assistivetouch.model.load_app.MainSettingModelImp;
import com.slc.assistivetouch.model.load_app.WorldReadablePreferences;
import com.slc.assistivetouch.model.load_app.po.PackInfoItem;
import com.slc.assistivetouch.ui.adapter.PackInfoAdapter;
import com.slc.code.presenter.MvpPresenterImp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainSettingPresenterImp extends MvpPresenterImp<MainSettingContract.MainSettingView, MainSettingContract.MainSettingModel> implements MainSettingContract.MainSettingPresenter {
    private boolean isOxygenOsRomOrH2OsRom;
    private String[] preference_key_activities;
    private SharedPreferences sharedPreferences;

    public static void initialize(MainSettingContract.MainSettingView view) {
        new MainSettingPresenterImp(view).start();
    }

    private MainSettingPresenterImp(MainSettingContract.MainSettingView view) {
        super(view);
        setModel(new MainSettingModelImp());
    }

    @Override
    public void init(Bundle bundle) {
        this.isOxygenOsRomOrH2OsRom = bundle.getBoolean(SettingConstant.Ga.KEY_IS_OXYGEN_OS_ROM_OR_H2OS_ROM);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M || this.isOxygenOsRomOrH2OsRom) {
            this.sharedPreferences = getContext().getSharedPreferences(SettingConstant.APP_PREFERENCES_NAME, Context.MODE_PRIVATE);
        } else {
            this.sharedPreferences = new WorldReadablePreferences(!getContext().isDeviceProtectedStorage() ?
                    getContext().createDeviceProtectedStorageContext() : getContext(), SettingConstant.APP_PREFERENCES_NAME);
        }
        this.preference_key_activities = getContext().getResources().getStringArray(R.array.preference_key_activities);
    }

    @Override
    public String getCustomSummary(String key) {
        return this.sharedPreferences.getString(key + SettingConstant.KEY_HWKEY_CUSTOM_NAME, getContext().getString(R.string.title_custom_actions));
    }

    @Override
    public void clearCustomAction(String key) {
        this.sharedPreferences.edit().remove(key + SettingConstant.KEY_HWKEY_CUSTOM).remove(key + SettingConstant.KEY_HWKEY_CUSTOM_NAME).commit();
    }

    @Override
    public int changeHwKeyOfActionIndex(String key, String value) {
        int valueToInt = Integer.parseInt(value);
        sendBroadcastFromPreferenceChange(SettingConstant.Ga.ACTION_PREF_HWKEY_CHANGED, key, valueToInt);
        clearCustomAction(key);
        return valueToInt;
    }

    @Override
    public String changeHwKeyOfActionName(String key, String value) {
        return this.preference_key_activities[changeHwKeyOfActionIndex(key, value)];
    }

    @Override
    public void showLoadingAppDialog(final String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.title_custom_actions);
        View contentView = getContext().getLayoutInflater().inflate(R.layout.dialog_content_installed_app, null);
        builder.setView(contentView);
        final ListView lv_app_list = contentView.findViewById(R.id.lv_app_list);
        final ProgressBar pb_loading = contentView.findViewById(R.id.pb_loading);
        builder.setNegativeButton(R.string.action_cancel, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                getModel().cancelAsyncTask();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        getModel().getInstalledAppData(new MainSettingContract.OnLoadInstalledAppDataListener() {
            @Override
            public void onLoadInstalledAppData(List<PackInfoItem> packInfoItems) {
                lv_app_list.setVisibility(View.VISIBLE);
                pb_loading.setVisibility(View.GONE);
                lv_app_list.setAdapter(new PackInfoAdapter(getContext(), packInfoItems));
            }
        });
        lv_app_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PackInfoItem packInfoItem = (PackInfoItem) parent.getItemAtPosition(position);
                Log.i("packInfoItem", packInfoItem.getValue());
                sharedPreferences.edit().putString(key, String.valueOf(SettingConstant.HWKEY_ACTION_CUSTOM_ACTIONS))
                        .putString(key + SettingConstant.KEY_HWKEY_CUSTOM, packInfoItem.getValue())
                        .putString(key + SettingConstant.KEY_HWKEY_CUSTOM_NAME, packInfoItem.getAppName()).commit();
                getView().refreshCustomItem(packInfoItem.getAppName(), String.valueOf(SettingConstant.HWKEY_ACTION_CUSTOM_ACTIONS));
                sendBroadcastFromPreferenceChange(SettingConstant.Ga.ACTION_PREF_HWKEY_CHANGED_CUSTOM, key + SettingConstant.KEY_HWKEY_CUSTOM, packInfoItem.getValue());
                alertDialog.dismiss();
            }
        });
    }

    @Override
    public void startDonateUi() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        String payUrl = "https://qr.alipay.com/a6x00741uv1j6tjq3m2dm27";
        intent.setData(Uri.parse("alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=" + payUrl));
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            getContext().startActivity(intent);
        } else {
            intent.setData(Uri.parse(payUrl));
        }
        getContext().startActivity(intent);
    }

    @Override
    public void sendBroadcastFromPreferenceChange(String action, String key, Object value) {
        Intent intent = new Intent(action);
        intent.putExtra(SettingConstant.Ga.EXTRA_KEY, key);
        if (value instanceof String) {
            intent.putExtra(SettingConstant.Ga.EXTRA_VALUE, value.toString());
            intent.putExtra(SettingConstant.Ga.EXTRA_VALUE_TYPE, SettingConstant.Ga.ACTION_VALUE_TYPE_STRING);
        } else if (value instanceof Integer) {
            intent.putExtra(SettingConstant.Ga.EXTRA_VALUE, ((Integer) value).intValue());
            intent.putExtra(SettingConstant.Ga.EXTRA_VALUE_TYPE, SettingConstant.Ga.ACTION_VALUE_TYPE_INT);
        } else if (value instanceof Boolean) {
            intent.putExtra(SettingConstant.Ga.EXTRA_VALUE, ((Boolean) value).booleanValue());
            intent.putExtra(SettingConstant.Ga.EXTRA_VALUE_TYPE, SettingConstant.Ga.ACTION_VALUE_TYPE_BOOLEAN);
        } else {
            throw new ClassCastException("请对value的类型做判断处理");
        }
        getContext().sendBroadcast(intent);
    }
}
