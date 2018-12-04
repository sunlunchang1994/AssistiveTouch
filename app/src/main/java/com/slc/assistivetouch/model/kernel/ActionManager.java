package com.slc.assistivetouch.model.kernel;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.Toast;

import com.slc.assistivetouch.model.SettingConstant;
import com.slc.assistivetouch.model.XpLog;
import com.slc.assistivetouch.model.kernel.HookConstant.ClassString;
import com.slc.assistivetouch.model.kernel.HookConstant.MethodString;
import com.slc.assistivetouch.model.SettingConstant.Ga;
import com.slc.code.provider.RemotePreferences;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.slc.assistivetouch.model.SettingConstant.ACTION_FAST_PAYMENT;

class ActionManager extends XC_MethodHook {
    private boolean isLoadPreferences;
    private ActivityManager mActivityManager;
    private int mDoubletapSpeed;
    private Map<HwKeyTrigger, HwKeyAction> mHwKeyActions;
    private boolean mIsOpenMainWitch;
    private boolean mIsSwitchAppSwitchAndBack;
    private int mKillDelay;
    private List<String> mKillIgnoreList;
    private PowerManager mPowerManager;
    private TorchModel mTorchModel;

    private static class Holder {
        private static final ActionManager INSTANCE = new ActionManager();
    }

    public static class HwKeyAction {
        int actionId;
        String customApp;

        public HwKeyAction(int id) {
            this.actionId = id;
        }

        HwKeyAction(int id, String cApp) {
            this.actionId = id;
            this.customApp = cApp;
        }

        public HwKeyAction clone() {
            return new HwKeyAction(this.actionId, this.customApp);
        }
    }
    @SuppressWarnings("unchecked")
    private ActionManager() {
        this.isLoadPreferences = false;
        this.mIsOpenMainWitch = false;
        this.mIsSwitchAppSwitchAndBack = false;
        this.mKillIgnoreList = new ArrayList();

        this.mKillIgnoreList.add(HookConstant.PACK_SYSTEM_UI);
        Map<HwKeyTrigger, HwKeyAction> map = new HashMap();
        map.put(HwKeyTrigger.BIXBY_SINGLETAP, new HwKeyAction(0, null));
        map.put(HwKeyTrigger.BIXBY_LONGPRESS, new HwKeyAction(0, null));
        map.put(HwKeyTrigger.BIXBY_DOUBLETAP, new HwKeyAction(0, null));
        map.put(HwKeyTrigger.HOME_LONGPRESS, new HwKeyAction(0, null));
        map.put(HwKeyTrigger.BACK_SINGLETAP, new HwKeyAction(0, null));
        map.put(HwKeyTrigger.BACK_LONGPRESS, new HwKeyAction(0, null));
        map.put(HwKeyTrigger.BACK_DOUBLETAP, new HwKeyAction(0, null));
        map.put(HwKeyTrigger.APP_SWITCH_SINGLETAP, new HwKeyAction(0, null));
        map.put(HwKeyTrigger.APP_SWITCH_LONGPRESS, new HwKeyAction(0, null));
        map.put(HwKeyTrigger.APP_SWITCH_DOUBLETAP, new HwKeyAction(0, null));
        map.put(HwKeyTrigger.MENU_SINGLETAP, new HwKeyAction(0, null));
        map.put(HwKeyTrigger.MENU_LONGPRESS, new HwKeyAction(0, null));
        map.put(HwKeyTrigger.MENU_DOUBLETAP, new HwKeyAction(0, null));
        map.put(HwKeyTrigger.CUSTOM_SINGLETAP, new HwKeyAction(0, null));
        map.put(HwKeyTrigger.CUSTOM_LONGPRESS, new HwKeyAction(0, null));
        map.put(HwKeyTrigger.CUSTOM_DOUBLETAP, new HwKeyAction(0, null));
        this.mHwKeyActions = Collections.unmodifiableMap(map);
        if (!AssistiveTouch.isOxygenOsRomOrH2OsRom()) {
            fillPreferences(AssistiveTouch.getXSharedPreferences().getAll());
        }
    }

    static ActionManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 初始化广播接收者
     * @param context
     */
    final void initActionReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Ga.ACTION_REQUEST_SYSTEM_INFO);
        intentFilter.addAction(Ga.ACTION_PREF_HWKEY_CHANGED);
        intentFilter.addAction(Ga.ACTION_PREF_HWKEY_CHANGED_CUSTOM);
        intentFilter.addAction(Ga.ACTION_PREF_HWKEY_DOUBLETAP_SPEED_CHANGED);
        intentFilter.addAction(Ga.ACTION_PREF_HWKEY_KILL_DELAY_CHANGED);
        intentFilter.addAction(Ga.ACTION_PREF_KEY_HWKEY_SWITCH);
        context.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }
    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
        if (!this.isLoadPreferences && param.args[1] != null) {
            initPreferences();
        }
    }

    final boolean hasAction(HwKey key) {
        boolean retVal = false;
        if (key == HwKey.MENU) {
            retVal |= getActionFor(HwKeyTrigger.MENU_SINGLETAP).actionId != SettingConstant.HWKEY_ACTION_DEFAULT;
            retVal |= getActionFor(HwKeyTrigger.MENU_LONGPRESS).actionId != SettingConstant.HWKEY_ACTION_DEFAULT;
            retVal |= getActionFor(HwKeyTrigger.MENU_DOUBLETAP).actionId != SettingConstant.HWKEY_ACTION_DEFAULT;
        } else if (key == HwKey.HOME) {
            retVal |= getActionFor(HwKeyTrigger.HOME_LONGPRESS).actionId != SettingConstant.HWKEY_ACTION_DEFAULT;
        } else if (key == HwKey.BACK) {
            retVal |= getActionFor(HwKeyTrigger.BACK_SINGLETAP).actionId != SettingConstant.HWKEY_ACTION_DEFAULT;
            retVal |= getActionFor(HwKeyTrigger.BACK_LONGPRESS).actionId != SettingConstant.HWKEY_ACTION_DEFAULT;
            retVal |= getActionFor(HwKeyTrigger.BACK_DOUBLETAP).actionId != SettingConstant.HWKEY_ACTION_DEFAULT;
        } else if (key == HwKey.APP_SWITCH) {
            retVal |= getActionFor(HwKeyTrigger.APP_SWITCH_SINGLETAP).actionId != SettingConstant.HWKEY_ACTION_DEFAULT;
            retVal |= getActionFor(HwKeyTrigger.APP_SWITCH_LONGPRESS).actionId != SettingConstant.HWKEY_ACTION_DEFAULT;
            retVal |= getActionFor(HwKeyTrigger.APP_SWITCH_DOUBLETAP).actionId != SettingConstant.HWKEY_ACTION_DEFAULT;
        } else if (key == HwKey.BIXBY) {
            retVal |= getActionFor(HwKeyTrigger.BIXBY_SINGLETAP).actionId != SettingConstant.HWKEY_ACTION_DEFAULT;
            retVal |= getActionFor(HwKeyTrigger.BIXBY_LONGPRESS).actionId != SettingConstant.HWKEY_ACTION_DEFAULT;
        } else if (key == HwKey.CUSTOM) {
            retVal = false;//TODO 此处目前设置为false
        }

        XpLog.log("HWKEY " + key + " has action = " + retVal);
        return retVal;
    }

    final HwKeyAction getActionFor(HwKeyTrigger keyTrigger) {
        return this.mHwKeyActions.get(keyTrigger);
    }

    final void setActionFor(HwKeyTrigger keyTrigger, int value) {
        setActionFor(keyTrigger, value, null);
    }

    final void setActionFor(HwKeyTrigger keyTrigger, String value) {
        setActionFor(keyTrigger, 9, value);
    }

    final void setActionFor(HwKeyTrigger keyTrigger, int value, String customApp) {
        this.mHwKeyActions.get(keyTrigger).actionId = value;
        this.mHwKeyActions.get(keyTrigger).customApp = customApp;
    }

    void performAction(HwKeyTrigger keyTrigger) {
        HwKeyAction action = getActionFor(keyTrigger);
        XpLog.log("Performing action " + action + " for HWKEY trigger " + keyTrigger.toString());
        if (action.actionId != SettingConstant.HWKEY_ACTION_DEFAULT && action.actionId != SettingConstant.HWKEY_ACTION_NULL) {
            if (action.actionId == SettingConstant.HWKEY_ACTION_GO_TO_SLEEP) {
                goToSleep();
            } else if (action.actionId == SettingConstant.HWKEY_ACTION_TURN_OFF_OR_ON_SCREEN) {
                turnOffOrOnScreen();
            } else if (action.actionId == SettingConstant.HWKEY_ACTION_TURN_OFF_OR_ON_TORCH) {
                toggleTorch();
            } else if (action.actionId == SettingConstant.HWKEY_ACTION_FAST_PAYMENT) {
                MethodHookPm.getInstance().getContext().startActivity(new Intent(ACTION_FAST_PAYMENT));
            } else if (action.actionId == SettingConstant.HWKEY_ACTION_SCREENSHOT) {
                screenshot();
            } else if (action.actionId == SettingConstant.HWKEY_ACTION_KILL_CURRENT_APPLICATION) {
                killCurrentApplication();
            } else if (action.actionId == SettingConstant.HWKEY_ACTION_SWITCH_TO_LAST_APP) {
                switchToLastApp();
            } else if (action.actionId == SettingConstant.HWKEY_ACTION_CUSTOM_ACTIONS) {
                launchCustomApp(action.customApp);
            }
        }
    }

    private void goToSleep() {
        try {
            XpLog.log("开始息屏");
            XposedHelpers.callMethod(getPowerManager(), "goToSleep", new Object[]{Long.valueOf(SystemClock.uptimeMillis())});
        } catch (Exception e) {
            XposedBridge.log(e);
        }
    }

    private void wakeUp() {
        try {
            XpLog.log("开始唤醒");
            XposedHelpers.callMethod(getPowerManager(), "wakeUp", new Object[]{Long.valueOf(SystemClock.uptimeMillis())});
        } catch (Exception e) {
            XposedBridge.log(e);
        }
    }

    private void turnOffOrOnScreen() {
        if (getPowerManager().isInteractive()) {
            goToSleep();
        } else {
            wakeUp();
        }
    }

    private void toggleTorch() {
        Handler handler = getSystemHandler();
        if (handler != null) {
            if (this.mTorchModel == null) {
                this.mTorchModel = new TorchModel(MethodHookPm.getInstance().getContext(), handler);
            }
            this.mTorchModel.toggleTorch();
        }
    }

    private void screenshot() {
        Handler handler = getSystemHandler();
        if (handler != null) {
            Runnable mScreenshotRunnable = (Runnable) XposedHelpers.getObjectField(MethodHookPm.getInstance().getPhoneWindowManager(), "mScreenshotRunnable");
            XposedHelpers.callMethod(mScreenshotRunnable, "setScreenshotType", new Object[]{Integer.valueOf(1)});
            handler.postDelayed(mScreenshotRunnable, 260);
        }
    }

    private void killCurrentApplication() {
        Handler handler = getSystemHandler();
        if (handler != null) {
            handler.post(this.killCurrentApplicationRunnable);
        }
    }

    private void switchToLastApp() {
        Handler handler = getSystemHandler();
        if (handler != null) {
            handler.post(this.switchToLastAppRunnable);
        }
    }

    private void launchCustomApp(String uri) {
        Handler handler = getSystemHandler();
        if (handler != null) {
            if (uri == null) {
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(MethodHookPm.getInstance().getContext(), "Activity not found", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            try {
                launchCustomApp(Intent.parseUri(uri, Intent.URI_INTENT_SCHEME), handler);
            } catch (Throwable e) {
                XpLog.log("launchCustomApp: error parsing uri: ", e);
            }
        }
    }

    private void launchCustomApp(final Intent intent, Handler handler) {
        handler.post(new Runnable() {
            public void run() {
                try {
                    ActionManager.this.dismissKeyguard();
                    MethodHookPm.getInstance().getContext().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(MethodHookPm.getInstance().getContext(), "Activity not found", Toast.LENGTH_SHORT).show();
                } catch (Throwable t) {
                    XpLog.log("launchCustomApp*intent", t);
                }
            }
        });
    }

    boolean isTaskLocked() {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getActivityManager().getLockTaskModeState() != 0;
        } else {
            return getActivityManager().isInLockTaskMode();
        }
    }

    private PowerManager getPowerManager() {
        if (this.mPowerManager == null) {
            this.mPowerManager = (PowerManager) MethodHookPm.getInstance().getContext().getSystemService(Context.POWER_SERVICE);
        }
        return this.mPowerManager;
    }

    private ActivityManager getActivityManager() {
        if (this.mActivityManager == null) {
            this.mActivityManager = (ActivityManager) MethodHookPm.getInstance().getContext().getSystemService(Context.ACTIVITY_SERVICE);
        }
        return this.mActivityManager;
    }

    Handler getSystemHandler() {
        return (Handler) XposedHelpers.getObjectField(MethodHookPm.getInstance().getPhoneWindowManager(), "mHandler");
    }

    private void dismissKeyguard() {
        try {
            XposedHelpers.callMethod(MethodHookPm.getInstance().getPhoneWindowManager(), "dismissKeyguardLw");
        } catch (Throwable t) {
            XpLog.log("dismissKeyguard", t);
        }
    }

    /**
     * 初始化Preferences
     * 此处要做双重判断 因为系统发送的消息过于频繁 初始化RemotePreferences过于耗时
     */
    void initPreferences() {
        if (!this.isLoadPreferences) {
            Map<String, ?> allData = new RemotePreferences(MethodHookPm.getInstance().getContext(), SettingConstant.AUTHORITIES, SettingConstant.APP_PREFERENCES_NAME).getAll();
            XpLog.log("initPreferences", allData.size() + "*", true);
            if (!this.isLoadPreferences ) {
                fillPreferences(allData);
            }
        }
    }

    private void fillPreferences(Map<String, ?> allData) {
        XpLog.log("fillPreferences", allData.size() + "*", true);
        if (allData.size() != 0) {
            this.isLoadPreferences = true;
            setActionFor(HwKeyTrigger.BIXBY_SINGLETAP, Integer.parseInt(getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_BIXBY, "0")), getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_BIXBY_CUSTOM));
            setActionFor(HwKeyTrigger.BIXBY_LONGPRESS, Integer.parseInt(getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_BIXBY_LONG, "0")), getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_BIXBY_LONG_CUSTOM));
            setActionFor(HwKeyTrigger.BIXBY_DOUBLETAP, Integer.parseInt(getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_BIXBY_DOUBLE, "0")), getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_BIXBY_DOUBLE_CUSTOM));
            setActionFor(HwKeyTrigger.HOME_LONGPRESS, Integer.parseInt(getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_HOME_LONG, "0")), getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_HOME_LONG_CUSTOM));
            setActionFor(HwKeyTrigger.BACK_SINGLETAP, Integer.parseInt(getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_BACK, "0")), getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_BACK_CUSTOM));
            setActionFor(HwKeyTrigger.BACK_LONGPRESS, Integer.parseInt(getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_BACK_LONG, "0")), getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_BACK_LONG_CUSTOM));
            setActionFor(HwKeyTrigger.BACK_DOUBLETAP, Integer.parseInt(getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_BACK_DOUBLE, "0")), getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_BACK_DOUBLE_CUSTOM));
            setActionFor(HwKeyTrigger.APP_SWITCH_SINGLETAP, Integer.parseInt(getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_APP_SWITCH, "0")), getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_APP_SWITCH_CUSTOM));
            setActionFor(HwKeyTrigger.APP_SWITCH_LONGPRESS, Integer.parseInt(getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_APP_SWITCH_LONG, "0")), getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_APP_SWITCH_LONG_CUSTOM));
            setActionFor(HwKeyTrigger.APP_SWITCH_DOUBLETAP, Integer.parseInt(getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_APP_SWITCH_DOUBLE, "0")), getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_APP_SWITCH_DOUBLE_CUSTOM));
            setActionFor(HwKeyTrigger.MENU_SINGLETAP, Integer.parseInt(getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_MENU, "0")), getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_MENU_CUSTOM));
            setActionFor(HwKeyTrigger.MENU_LONGPRESS, Integer.parseInt(getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_MENU_LONG, "0")), getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_MENU_LONG_CUSTOM));
            setActionFor(HwKeyTrigger.MENU_DOUBLETAP, Integer.parseInt(getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_MENU_DOUBLE, "0")), getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_MENU_DOUBLE_CUSTOM));
            setActionFor(HwKeyTrigger.CUSTOM_SINGLETAP, Integer.parseInt(getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_CUSTOM, "0")), getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_CUSTOM_CUSTOM));
            setActionFor(HwKeyTrigger.CUSTOM_LONGPRESS, Integer.parseInt(getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_CUSTOM_LONG, "0")), getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_CUSTOM_LONG_CUSTOM));
            setActionFor(HwKeyTrigger.CUSTOM_DOUBLETAP, Integer.parseInt(getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_CUSTOM_DOUBLE, "0")), getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_CUSTOM_DOUBLE_CUSTOM));
            this.mDoubletapSpeed = Integer.parseInt(getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_DOUBLETAP_SPEED, "400"));
            this.mKillDelay = Integer.parseInt(getStringByMap(allData, SettingConstant.PREF_KEY_HWKEY_KILL_DELAY, "1000"));
            Boolean isOpenMainWitchTemp = (Boolean) allData.get(SettingConstant.PREF_KEY_HWKEY_MAIN_SWITCH);
            this.mIsOpenMainWitch = isOpenMainWitchTemp == null ? false : isOpenMainWitchTemp;
            Boolean isSwitchAppSwitchAndBack = (Boolean) allData.get(SettingConstant.PREF_KEY_HWKEY_SWITCH_APP_SWITCH_AND_BACK);
            this.mIsSwitchAppSwitchAndBack = isSwitchAppSwitchAndBack == null ? false : isSwitchAppSwitchAndBack;
        }
    }

    private String getStringByMap(Map<String, ?> allData, String key, String defValue) {
        String value = (String) allData.get(key);
        return (TextUtils.isEmpty(value) || value.equals("null")) ? defValue : value;
    }

    private String getStringByMap(Map<String, ?> allData, String key) {
        return (String) allData.get(key);
    }

    private void sendOsOxygenOsRomOrH2OsRom() {
        Intent intent = new Intent(Ga.ACTION_RESULT_SYSTEM_INFO);
        intent.putExtra(SettingConstant.Ga.EXTRA_KEY, SettingConstant.Ga.KEY_IS_OXYGEN_OS_ROM_OR_H2OS_ROM);
        intent.putExtra(SettingConstant.Ga.EXTRA_VALUE, AssistiveTouch.isOxygenOsRomOrH2OsRom());
        MethodHookPm.getInstance().getContext().sendBroadcast(intent);
    }

    int getDoubletapSpeed() {
        return this.mDoubletapSpeed;
    }

    int getKillDelay() {
        return this.mKillDelay;
    }

    boolean isLoadPreferences() {
        return this.isLoadPreferences;
    }

    boolean isIsOpenMainWitch() {
        return this.mIsOpenMainWitch;
    }

    boolean isIsSwitchAppSwitchAndBack() {
        return this.mIsSwitchAppSwitchAndBack;
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String key = intent.getStringExtra(Ga.EXTRA_KEY);
            if (Ga.ACTION_REQUEST_SYSTEM_INFO.equals(action)) {
                if (Ga.KEY_IS_OXYGEN_OS_ROM_OR_H2OS_ROM.equals(key)) {
                    ActionManager.this.sendOsOxygenOsRomOrH2OsRom();
                }
            } else if (SettingConstant.Ga.ACTION_PREF_HWKEY_CHANGED.equals(action)) {
                int value = intent.getIntExtra(Ga.EXTRA_VALUE, 0);
                if (SettingConstant.PREF_KEY_HWKEY_BIXBY.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.BIXBY_SINGLETAP, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_BIXBY_LONG.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.BIXBY_LONGPRESS, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_BIXBY_DOUBLE.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.BIXBY_DOUBLETAP, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_HOME_LONG.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.HOME_LONGPRESS, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_BACK.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.BACK_SINGLETAP, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_BACK_LONG.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.BACK_LONGPRESS, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_BACK_DOUBLE.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.BACK_DOUBLETAP, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_MENU.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.MENU_SINGLETAP, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_MENU_LONG.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.MENU_LONGPRESS, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_MENU_DOUBLE.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.MENU_DOUBLETAP, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_APP_SWITCH.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.APP_SWITCH_SINGLETAP, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_APP_SWITCH_LONG.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.APP_SWITCH_LONGPRESS, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_APP_SWITCH_DOUBLE.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.APP_SWITCH_DOUBLETAP, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_CUSTOM.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.CUSTOM_SINGLETAP, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_CUSTOM_LONG.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.CUSTOM_LONGPRESS, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_CUSTOM_DOUBLE.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.CUSTOM_DOUBLETAP, value);
                }
            } else if (SettingConstant.Ga.ACTION_PREF_HWKEY_CHANGED_CUSTOM.equals(action)) {
                String value = intent.getStringExtra(Ga.EXTRA_VALUE);
                if (SettingConstant.PREF_KEY_HWKEY_BIXBY_CUSTOM.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.BIXBY_SINGLETAP, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_BIXBY_LONG_CUSTOM.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.BIXBY_LONGPRESS, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_BIXBY_DOUBLE_CUSTOM.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.BIXBY_DOUBLETAP, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_HOME_LONG_CUSTOM.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.HOME_LONGPRESS, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_BACK_CUSTOM.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.BACK_SINGLETAP, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_BACK_LONG_CUSTOM.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.BACK_LONGPRESS, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_BACK_DOUBLE_CUSTOM.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.BACK_DOUBLETAP, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_MENU_CUSTOM.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.MENU_SINGLETAP, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_MENU_LONG_CUSTOM.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.MENU_LONGPRESS, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_MENU_DOUBLE_CUSTOM.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.MENU_DOUBLETAP, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_APP_SWITCH_CUSTOM.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.APP_SWITCH_SINGLETAP, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_APP_SWITCH_LONG_CUSTOM.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.APP_SWITCH_LONGPRESS, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_APP_SWITCH_DOUBLE_CUSTOM.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.APP_SWITCH_DOUBLETAP, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_CUSTOM_CUSTOM.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.CUSTOM_SINGLETAP, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_CUSTOM_LONG_CUSTOM.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.CUSTOM_LONGPRESS, value);
                } else if (SettingConstant.PREF_KEY_HWKEY_CUSTOM_DOUBLE_CUSTOM.equals(key)) {
                    ActionManager.this.setActionFor(HwKeyTrigger.CUSTOM_DOUBLETAP, value);
                }
            } else if (SettingConstant.Ga.ACTION_PREF_HWKEY_DOUBLETAP_SPEED_CHANGED.equals(action)) {
                ActionManager.this.mDoubletapSpeed = intent.getIntExtra(Ga.EXTRA_VALUE, ActionManager.this.mDoubletapSpeed);
            } else if (SettingConstant.Ga.ACTION_PREF_HWKEY_KILL_DELAY_CHANGED.equals(action)) {
                ActionManager.this.mKillDelay = intent.getIntExtra(Ga.EXTRA_VALUE, ActionManager.this.mKillDelay);
            } else if (SettingConstant.Ga.ACTION_PREF_KEY_HWKEY_SWITCH.equals(action)) {
                if (SettingConstant.PREF_KEY_HWKEY_MAIN_SWITCH.equals(key)) {
                    ActionManager.this.mIsOpenMainWitch = intent.getBooleanExtra(Ga.EXTRA_VALUE, false);
                } else if (SettingConstant.PREF_KEY_HWKEY_SWITCH_APP_SWITCH_AND_BACK.equals(key)) {
                    ActionManager.this.mIsSwitchAppSwitchAndBack = intent.getBooleanExtra(Ga.EXTRA_VALUE, false);
                }
            }
        }
    };
    private Runnable killCurrentApplicationRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Intent intent = new Intent("android.intent.action.MAIN");
                PackageManager pm = MethodHookPm.getInstance().getContext().getPackageManager();
                String defaultHomePackage = HookConstant.PACK_DEF_LAUNCHER;
                intent.addCategory("android.intent.category.HOME");
                ResolveInfo res = pm.resolveActivity(intent, 0);
                if (!(res.activityInfo == null || res.activityInfo.packageName.equals(HookConstant.PACK_ANDROID))) {
                    defaultHomePackage = res.activityInfo.packageName;
                }
                ActivityManager am = ActionManager.this.getActivityManager();
                List<RunningTaskInfo> apps = am.getRunningTasks(1);
                String targetKilled = null;
                if (apps.size() > 0 && apps.get(0).numRunning > 0) {
                    ComponentName cn = apps.get(0).topActivity;
                    if (!(ActionManager.this.mKillIgnoreList.contains(cn.getPackageName()) || cn.getPackageName().startsWith(defaultHomePackage))) {
                        XpLog.log("Force stopping: " + cn.getPackageName());
                        if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            XposedHelpers.callMethod(am, MethodString.METHOD_AM_REMOVE_TASK, apps.get(0).id);
                            XposedHelpers.callMethod(am, MethodString.METHOD_AM_FORCE_STOP_PACKAGE, cn.getPackageName());
                        } else {
                            XposedHelpers.callMethod(am, MethodString.METHOD_AM_REMOVE_TASK, apps.get(0).id, 1);
                        }
                        targetKilled = cn.getPackageName();
                    }
                }
                if (targetKilled != null) {
                    Class<?>[] paramArgs = new Class[]{XposedHelpers.findClass(ClassString.CLASS_WINDOW_STATE, null), Integer.TYPE, Boolean.TYPE};
                    XposedHelpers.callMethod(MethodHookPm.getInstance().getPhoneWindowManager(), MethodString.METHOD_PM_PERFORM_HAPTIC_FEEDBACK_LW, paramArgs,
                            null, 2, true);
                }
            } catch (Throwable e) {
                XpLog.log(e, true);
            }
        }
    };
    private Runnable switchToLastAppRunnable = new Runnable() {
        @SuppressLint({"MissingPermission"})
        @Override
        public void run() {
            int lastAppId = 0;
            int looper = 1;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            ActivityManager am = ActionManager.this.getActivityManager();
            String defaultHomePackage = HookConstant.PACK_DEF_LAUNCHER;
            intent.addCategory(Intent.CATEGORY_HOME);
            ResolveInfo res = MethodHookPm.getInstance().getContext().getPackageManager().resolveActivity(intent, 0);
            if (!(res.activityInfo == null || res.activityInfo.packageName.equals(HookConstant.PACK_ANDROID))) {
                defaultHomePackage = res.activityInfo.packageName;
            }
            List<RunningTaskInfo> tasks = am.getRunningTasks(5);
            while (lastAppId == 0 && looper < tasks.size()) {
                String packageName = tasks.get(looper).topActivity.getPackageName();
                if (!(packageName.equals(defaultHomePackage) || packageName.equals(HookConstant.PACK_SYSTEM_UI))) {
                    lastAppId = tasks.get(looper).id;
                }
                looper++;
            }
            if (lastAppId != 0) {
                am.moveTaskToFront(lastAppId, ActivityManager.MOVE_TASK_NO_USER_ACTION);
            }
        }
    };
}
