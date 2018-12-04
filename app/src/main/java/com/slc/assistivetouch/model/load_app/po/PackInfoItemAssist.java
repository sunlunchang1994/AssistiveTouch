package com.slc.assistivetouch.model.load_app.po;

import android.content.pm.PackageManager;
import android.content.res.Resources;

public class PackInfoItemAssist {
    private int mAppIconSizePx;
    private PackageManager mPackageManager;
    private Resources mResources;

    public PackInfoItemAssist(PackageManager mPackageManager, int mAppIconSizePx, Resources mResources) {
        this.mPackageManager = mPackageManager;
        this.mAppIconSizePx = mAppIconSizePx;
        this.mResources = mResources;
    }

    public PackageManager getPackageManager() {
        return this.mPackageManager;
    }

    public int getAppIconSizePx() {
        return this.mAppIconSizePx;
    }

    public Resources getResources() {
        return this.mResources;
    }
}
