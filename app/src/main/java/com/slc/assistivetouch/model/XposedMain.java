package com.slc.assistivetouch.model;

import android.os.Build;

import com.slc.assistivetouch.model.kernel.AssistiveTouch;
import com.slc.assistivetouch.model.kernel.HookConstant;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.io.File;

public class XposedMain implements IXposedHookZygoteInit, IXposedHookLoadPackage {
    private static final File prefsFileProt = new File("/data/user_de/0/com.slc.assistivetouch/shared_prefs/com.slc.assistivetouch_preferences.xml");
    private XSharedPreferences xSharedPreferences;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        XpLog.log("assistiveTouch-initZygote" + Build.VERSION.SDK_INT, true);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            this.xSharedPreferences = new XSharedPreferences(SettingConstant.PACKAGE_NAME);
            this.xSharedPreferences.makeWorldReadable();
            return;
        }
        this.xSharedPreferences = new XSharedPreferences(prefsFileProt);
        this.xSharedPreferences.makeWorldReadable();
    }

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(HookConstant.PACK_ANDROID) && lpparam.processName.equals(HookConstant.PACK_ANDROID)) {
            AssistiveTouch.initAndroid(this.xSharedPreferences, lpparam.classLoader);
        }
    }
}
