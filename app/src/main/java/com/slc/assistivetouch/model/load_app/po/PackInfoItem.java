package com.slc.assistivetouch.model.load_app.po;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.slc.assistivetouch.model.load_app.Utils;

public class PackInfoItem {
    private BitmapDrawable mAppIcon;
    private String mAppName;
    private PackInfoItemAssist mPackInfoItemAssist;
    private ResolveInfo mResolveInfo;
    private String mValue;

    public PackInfoItem(String mAppName, ResolveInfo mResolveInfo, PackInfoItemAssist packInfoItemAssist) {
        this.mAppName = mAppName;
        this.mResolveInfo = mResolveInfo;
        this.mPackInfoItemAssist = packInfoItemAssist;
    }

    public String getAppName() {
        return this.mAppName;
    }

    public void setAppName(String mAppName) {
        this.mAppName = mAppName;
    }

    public BitmapDrawable getAppIcon() {
        if (this.mResolveInfo == null) {
            return null;
        }
        if (this.mAppIcon == null) {
            this.mAppIcon = new BitmapDrawable(this.mPackInfoItemAssist.getResources(), Bitmap.createScaledBitmap(Utils.drawableToBitmap(this.mResolveInfo.loadIcon(this.mPackInfoItemAssist.getPackageManager())), this.mPackInfoItemAssist.getAppIconSizePx(), this.mPackInfoItemAssist.getAppIconSizePx(), false));
        }
        return this.mAppIcon;
    }

    public void setAppIcon(BitmapDrawable mAppIcon) {
        this.mAppIcon = mAppIcon;
    }

    public ResolveInfo getResolveInfo() {
        return this.mResolveInfo;
    }

    public void setResolveInfo(ResolveInfo mResolveInfo) {
        this.mResolveInfo = mResolveInfo;
    }

    public String getValue() {
        if (this.mValue == null) {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.setComponent(new ComponentName(this.mResolveInfo.activityInfo.packageName, this.mResolveInfo.activityInfo.name));
            intent.putExtra("mode", 0);
            this.mValue = intent.toUri(0);
        }
        return this.mValue;
    }
}
