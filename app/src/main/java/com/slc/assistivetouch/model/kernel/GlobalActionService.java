package com.slc.assistivetouch.model.kernel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.slc.assistivetouch.model.SettingConstant.Ga;
import de.robv.android.xposed.XC_MethodHook;

import java.util.HashMap;
import java.util.Map;

public class GlobalActionService {
    private static final String CLASS_I_WINDOW_MANAGER = "android.view.IWindowManager";
    private static final String CLASS_PHONE_WINDOW_MANAGER = "com.android.server.policy.PhoneWindowManager";
    private static final String CLASS_WINDOW_MANAGER_FUNCS = "android.view.WindowManagerPolicy.WindowManagerFuncs";
    private static BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String key = intent.getStringExtra(Ga.EXTRA_KEY);
            Object value = null;
            switch (intent.getIntExtra("valueType", 0)) {
                case 1:
                    value = intent.getStringExtra(Ga.EXTRA_VALUE);
                    break;
                case 2:
                    value = Integer.valueOf(intent.getIntExtra(Ga.EXTRA_VALUE, 0));
                    break;
                case 3:
                    value = Long.valueOf(intent.getLongExtra(Ga.EXTRA_VALUE, 0));
                    break;
                case 4:
                    value = Float.valueOf(intent.getFloatExtra(Ga.EXTRA_VALUE, 0.0f));
                    break;
                case 5:
                    intent.getBooleanExtra(Ga.EXTRA_VALUE, false);
                    break;
                case 6:
                    value = intent.getStringArrayListExtra(Ga.EXTRA_VALUE);
                    break;
            }
            if (value != null) {
                if (GlobalActionService.preferenceMap == null) {
                    GlobalActionService.preferenceMap = new HashMap();
                }
                GlobalActionService.preferenceMap.put(key, value);
            }
        }

        private void submitData() {
        }
    };
    private static Class<?> mPhoneWindowManagerClass;
    private static final XC_MethodHook phoneWindowManagerInitHook = new XC_MethodHook() {
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        }
    };
    private static Map<String, Object> preferenceMap;

    public static void initAndroid(ClassLoader classLoader) {
    }

    public static Map<String, Object> getPreferenceMap() {
        return preferenceMap;
    }
}
