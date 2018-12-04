package com.slc.assistivetouch.model.load_app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ResolveInfo.DisplayNameComparator;
import android.os.AsyncTask;

import com.slc.assistivetouch.contract.MainSettingContract;
import com.slc.assistivetouch.contract.MainSettingContract.MainSettingModel;
import com.slc.assistivetouch.contract.MainSettingContract.OnLoadInstalledAppDataListener;
import com.slc.assistivetouch.model.load_app.po.PackInfoItem;
import com.slc.assistivetouch.model.load_app.po.PackInfoItemAssist;
import com.slc.code.app.AppData;
import com.slc.code.model.BaseModelImp;
import com.slc.code.ui.utils.DimenUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainSettingModelImp extends BaseModelImp implements MainSettingModel {
    private MyAsyncTask mAsyncTask;

    private static class MyAsyncTask extends AsyncTask<Void, Void, List<PackInfoItem>> {
        private OnLoadInstalledAppDataListener listener;

        public MyAsyncTask(OnLoadInstalledAppDataListener listener) {
            this.listener = listener;
        }

        @Override
        protected List<PackInfoItem> doInBackground(Void... voids) {
            List<PackInfoItem> packInfoItemList = new ArrayList<>();
            List<ResolveInfo> resolveInfoList = new ArrayList<>();
            Context context = AppData.getApplicationContext();
            PackageManager mPackageManager = context.getPackageManager();
            PackInfoItemAssist packInfoItemAssist = new PackInfoItemAssist(mPackageManager, DimenUtil.dip2px(context, 40), context.getResources());
            List<PackageInfo> packages = mPackageManager.getInstalledPackages(0);
            Intent mainIntent = new Intent();
            mainIntent.setAction(Intent.ACTION_MAIN);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            for (PackageInfo pi : packages) {
                if (this.isCancelled()) break;
                mainIntent.setPackage(pi.packageName);
                resolveInfoList.addAll(mPackageManager.queryIntentActivities(mainIntent, 0));
            }

            Collections.sort(resolveInfoList, new ResolveInfo.DisplayNameComparator(mPackageManager));
            for (ResolveInfo ri : resolveInfoList) {
                if (this.isCancelled()) break;
                String appName = ri.loadLabel(mPackageManager).toString();
                PackInfoItem packInfoItem = new PackInfoItem(appName, ri, packInfoItemAssist);
                packInfoItemList.add(packInfoItem);
            }
            return packInfoItemList;
        }

        @Override
        protected void onPostExecute(List<PackInfoItem> packInfoItems) {
            if (listener != null) {
                listener.onLoadInstalledAppData(packInfoItems);
                listener = null;
            }
        }
    }

    /**
     * 取消
     */
    public void cancelAsyncTask() {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
            mAsyncTask = null;
        }
    }

    @Override
    public void getInstalledAppData(MainSettingContract.OnLoadInstalledAppDataListener onLoadInstalledAppDataListener) {
        mAsyncTask = new MyAsyncTask(onLoadInstalledAppDataListener);
        mAsyncTask.execute();
    }

}
