package com.slc.assistivetouch.model.kernel;

import com.slc.assistivetouch.model.SettingConstant;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by achang on 2019/3/1.
 */

public class ValidationHelper {
    public static void initAndroid(ClassLoader classLoader) {
        XposedHelpers.findAndHookMethod(SettingConstant.class.getName(), classLoader,"isModuleCheck", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                param.setResult(true);
            }
        });
        XposedHelpers.findAndHookMethod(SettingConstant.class.getName(), classLoader, "isOxygenOsRomOrH2osRom", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                param.setResult(PublicDataHelper.isOxygenOsRomOrH2OsRom());
            }
        });
    }
}
