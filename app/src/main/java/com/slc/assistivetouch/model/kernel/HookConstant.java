package com.slc.assistivetouch.model.kernel;

public class HookConstant {

    private static final int FLAG_WAKE = 0x00000001;
    private static final int FLAG_WAKE_DROPPED = 0x00000002;
    public static final int PA_SOURCE_CUSTOM = 0x08000000 | 0x00000001;
    public static final String PACK_ANDROID = "android";
    public static final String PACK_DEF_LAUNCHER = "com.android.launcher";
    public static final String PACK_SYSTEM_UI = "com.android.systemui";
    public static final String PATH_PREFERENCES_N_START = "/data/user_de/0/com.slc.assistivetouch/shared_prefs/com.slc.assistivetouch_preferences.xml";
    public static final int TAKE_SCREENSHOT_FULLSCREEN = 1;

    public static class ClassString {
        public static final String CLASS_ACTIVITY_MANAGER_SERVICE = "com.android.server.am.ActivityManagerService";
        public static final String CLASS_I_APPLICATION_THREAD = "android.app.IApplicationThread";
        public static final String CLASS_I_WINDOW_MANAGER = "android.view.IWindowManager";
        public static final String CLASS_PHONE_WINDOW_MANAGER = "com.android.server.policy.PhoneWindowManager";
        public static final String CLASS_PHONE_WINDOW_MANAGER_5 = "com.android.internal.policy.impl.PhoneWindowManager";
        public static final String CLASS_PHONE_WINDOW_MANAGER_OEM = "com.android.server.policy.OemPhoneWindowManager";
        public static final String CLASS_WINDOW_MANAGER_FUNCS = "android.view.WindowManagerPolicy.WindowManagerFuncs";
        public static final String CLASS_WINDOW_STATE = "android.view.WindowManagerPolicy$WindowState";
    }

    public static class MethodString {
        public static final String METHOD_AM_FORCE_STOP_PACKAGE = "forceStopPackage";
        public static final String METHOD_AM_REMOVE_TASK = "removeTask";
        public static final String METHOD_PM_INIT = "init";
        public static final String METHOD_PM_INTERCEPT_KEY_BEFORE_DISPATCHING = "interceptKeyBeforeDispatching";
        public static final String METHOD_PM_INTERCEPT_KEY_BEFORE_QUEUEING = "interceptKeyBeforeQueueing";
        public static final String METHOD_PM_PERFORM_HAPTIC_FEEDBACK_LW = "performHapticFeedbackLw";
        public static final String METHOD_PUBLISH_CONTENT_PROVIDERS = "publishContentProviders";
    }
}
