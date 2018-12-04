package com.slc.assistivetouch.contract;

import android.os.Bundle;

import com.slc.assistivetouch.model.load_app.po.PackInfoItem;
import com.slc.code.contract.MvpContract.BaseModel;
import com.slc.code.contract.MvpContract.BaseMvpView;
import com.slc.code.contract.MvpContract.BasePresenter;

import java.util.List;

public interface MainSettingContract {

    interface OnLoadInstalledAppDataListener {
        void onLoadInstalledAppData(List<PackInfoItem> list);
    }

    interface MainSettingModel extends BaseModel {
        void cancelAsyncTask();

        void getInstalledAppData(OnLoadInstalledAppDataListener onLoadInstalledAppDataListener);
    }

    interface MainSettingPresenter extends BasePresenter<MainSettingView, MainSettingModel> {
        int changeHwKeyOfActionIndex(String str, String str2);

        String changeHwKeyOfActionName(String str, String str2);

        void clearCustomAction(String str);

        String getCustomSummary(String str);

        void init(Bundle bundle);

        void sendBroadcastFromPreferenceChange(String str, String str2, Object obj);

        void showLoadingAppDialog(String str);

        void startDonateUi();
    }

    interface MainSettingView extends BaseMvpView<MainSettingPresenter> {
        void refreshCustomItem(String str, String str2);
    }
}
