package com.slc.assistivetouch.model.kernel;

import android.content.Context;
import android.os.Build;
import android.view.KeyEvent;

import com.slc.assistivetouch.model.XpLog;
import com.slc.assistivetouch.model.kernel.HookConstant.ClassString;
import com.slc.assistivetouch.model.kernel.HookConstant.MethodString;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;

import java.util.List;

public class AssistiveTouch {
    private static final String TAG = "AssistiveTouch";

    private static XSharedPreferences mXSharedPreferences;

    public static void initAndroid(XSharedPreferences xSharedPreferences, ClassLoader classLoader) {
        try {
            xSharedPreferences.reload();
            mXSharedPreferences = xSharedPreferences;
            XpLog.log("初始化AssistiveTouch", true);
            if (PublicDataHelper.isOxygenOsRomOrH2OsRom()) {
                XposedHelpers.findAndHookMethod(XposedHelpers.findClass(ClassString.CLASS_ACTIVITY_MANAGER_SERVICE, classLoader),
                        MethodString.METHOD_PUBLISH_CONTENT_PROVIDERS, ClassString.CLASS_I_APPLICATION_THREAD, List.class, ActionManager.getInstance());
            }
            Class mPhoneWindowManagerClass = XposedHelpers.findClass(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? ClassString.CLASS_PHONE_WINDOW_MANAGER :
                    ClassString.CLASS_PHONE_WINDOW_MANAGER_5, classLoader);
            Class<?> mPhoneWindowManagerOemClass = null;
            if (PublicDataHelper.isOxygenOsRomOrH2OsRom()) {
                mPhoneWindowManagerOemClass = XposedHelpers.findClass(ClassString.CLASS_PHONE_WINDOW_MANAGER_OEM, classLoader);
            }
            XposedHelpers.findAndHookMethod(mPhoneWindowManagerClass, MethodString.METHOD_PM_INIT, Context.class,
                    ClassString.CLASS_I_WINDOW_MANAGER, ClassString.CLASS_WINDOW_MANAGER_FUNCS, MethodHookPm.getInstance());
            XposedHelpers.findAndHookMethod(mPhoneWindowManagerOemClass == null ? mPhoneWindowManagerClass : mPhoneWindowManagerOemClass,
                    MethodString.METHOD_PM_INTERCEPT_KEY_BEFORE_QUEUEING, KeyEvent.class, Integer.TYPE,
                    MethodHookInterceptKeyBefore.getInstance().getInterceptKeyBeforeQueueingMethodHook());
            XposedHelpers.findAndHookMethod(mPhoneWindowManagerOemClass == null ? mPhoneWindowManagerClass : mPhoneWindowManagerOemClass,
                    MethodString.METHOD_PM_INTERCEPT_KEY_BEFORE_DISPATCHING, ClassString.CLASS_WINDOW_STATE,
                    KeyEvent.class, Integer.TYPE, MethodHookInterceptKeyBefore.getInstance().getInterceptKeyBeforeDispatchingMethodHook());
        } catch (Throwable t) {
            XpLog.log(TAG, t);
        }
    }

    static XSharedPreferences getXSharedPreferences() {
        return mXSharedPreferences;
    }

}
