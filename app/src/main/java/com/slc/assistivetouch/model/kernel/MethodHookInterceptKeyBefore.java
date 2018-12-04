package com.slc.assistivetouch.model.kernel;

import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.view.InputDeviceCompat;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import com.slc.assistivetouch.model.SettingConstant;
import com.slc.assistivetouch.model.XpLog;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedHelpers;

public class MethodHookInterceptKeyBefore {
    private static boolean mAppSwitchKeyPressed = false;
    private static boolean mBackKeyPressed;
    private static boolean mBixbyKeyPressed;
    private static boolean mHwKeysEnabled = true;
    private static boolean mIsAppSwitchDoubleTap = false;
    private static boolean mIsAppSwitchIterationPressed = false;
    private static boolean mIsAppSwitchLongPressed = false;
    private static boolean mIsBackDoubleTap = false;
    private static boolean mIsBackIterationPressed = false;
    private static boolean mIsBackLongPressed = false;
    private static boolean mIsBixbyDoubleTap = false;
    private static boolean mIsBixbyIterationPressed = false;
    private static boolean mIsBixbyLongPressed = false;
    private static boolean mIsHomeLongPressed;
    private static boolean mIsMenuDoubleTap = false;
    private static boolean mIsMenuIterationPressed = false;
    private static boolean mIsMenuLongPressed = false;
    private static boolean mMenuKeyPressed;
    private static boolean mSystemBooted = false;
    private static boolean mWasAppSwitchDoubleTap = false;
    private static boolean mWasBackDoubleTap = false;
    private static boolean mWasBixbyDoubleTap = false;
    private static boolean mWasMenuDoubleTap = false;
    private ActionManager actionManager = ActionManager.getInstance();

    private static class Holder {
        private static final MethodHookInterceptKeyBefore INSTANCE = new MethodHookInterceptKeyBefore();

        private Holder() {
        }
    }

    static MethodHookInterceptKeyBefore getInstance() {
        return Holder.INSTANCE;
    }

    private XC_MethodHook interceptKeyBeforeDispatchingMethodHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            if (MethodHookInterceptKeyBefore.this.actionManager.isIsOpenMainWitch()) {
                if ((Boolean) XposedHelpers.callMethod(MethodHookPm.getInstance().getPhoneWindowManager(), "keyguardOn"))
                    return;
                KeyEvent event = (KeyEvent) param.args[1];
                int keyCode = event.getKeyCode();
                boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
                boolean isFromSystem = (event.getFlags() & KeyEvent.FLAG_FROM_SYSTEM) != 0;
                XpLog.log("interceptKeyBeforeDispatching: keyCode=" + keyCode +
                        "; isInjected=" + (((Integer) param.args[2] & 0x01000000) != 0) +
                        "; repeatCount=" + event.getRepeatCount() +
                        "; fromSystem=" + isFromSystem +
                        "; flags=" + Integer.toHexString(event.getFlags()) +
                        "; source=" + event.getSource());

                if (event.getSource() == InputDevice.SOURCE_UNKNOWN || event.getSource() == HookConstant.PA_SOURCE_CUSTOM) {
                    // ignore unknown source events, e.g. events injected from GB itself
                    XpLog.log("interceptKeyBeforeDispatching: ignoring event from unknown source");
                    return;
                }

                Handler mHandler = (Handler) XposedHelpers.getObjectField(param.thisObject, "mHandler");

                if (keyCode == KeyEvent.KEYCODE_MENU && isFromSystem && !ActionManager.getInstance().isTaskLocked() && ActionManager.getInstance().hasAction(HwKey.MENU)) {
                    if (!down) {
                        mMenuKeyPressed = false;
                        mHandler.removeCallbacks(mMenuLongPress);
                        if (mIsMenuLongPressed) {
                            mIsMenuLongPressed = false;
                            mIsMenuIterationPressed = false;
                            param.setResult(-1);
                            return;
                        } else if (event.getRepeatCount() == 0) {
                            if (mIsMenuDoubleTap) {
                                // we are still waiting for double-tap
                                XpLog.log("MENU doubletap pending. Ignoring.");
                            } else if (mIsMenuIterationPressed) {
                                mIsMenuIterationPressed = false;
                            } else if (!mWasMenuDoubleTap && !event.isCanceled()) {
                                if (ActionManager.getInstance().getActionFor(HwKeyTrigger.MENU_SINGLETAP).actionId != SettingConstant.HWKEY_ACTION_DEFAULT) {
                                    ActionManager.getInstance().performAction(HwKeyTrigger.MENU_SINGLETAP);
                                } else {
                                    XpLog.log("Triggering original DOWN/UP events for MENU key");
                                    actionManager.injectKey(KeyEvent.KEYCODE_MENU);
                                }
                            }
                            param.setResult(-1);
                            return;
                        }
                    } else if (event.getRepeatCount() == 0) {
                        mMenuKeyPressed = true;
                        mIsMenuIterationPressed = false;
                        mWasMenuDoubleTap = mIsMenuDoubleTap;
                        if (mIsMenuDoubleTap) {
                            ActionManager.getInstance().performAction(HwKeyTrigger.MENU_DOUBLETAP);
                            mHandler.removeCallbacks(mMenuDoubleTapReset);
                            mIsMenuDoubleTap = false;
                        } else {
                            mIsMenuLongPressed = false;
                            mIsMenuDoubleTap = false;
                            if (ActionManager.getInstance().getActionFor(HwKeyTrigger.MENU_DOUBLETAP).actionId != SettingConstant.HWKEY_ACTION_DEFAULT) {
                                mIsMenuDoubleTap = true;
                                mHandler.postDelayed(mMenuDoubleTapReset, ActionManager.getInstance().getDoubletapSpeed());
                            }
                            if (ActionManager.getInstance().getActionFor(HwKeyTrigger.MENU_LONGPRESS).actionId != SettingConstant.HWKEY_ACTION_DEFAULT) {
                                mHandler.postDelayed(mMenuLongPress, getLongpressTimeoutForAction(ActionManager.getInstance().getActionFor(HwKeyTrigger.MENU_LONGPRESS).actionId));
                            }
                        }
                        param.setResult(-1);
                        return;
                    } else {
                        mIsMenuIterationPressed = true;
                        XpLog.log("此处有长按事件");
                    }
                }

                if (keyCode == KeyEvent.KEYCODE_BACK && isFromSystem && !ActionManager.getInstance().isTaskLocked() && ActionManager.getInstance().hasAction(HwKey.BACK)) {
                    if (!down) {
                        mBackKeyPressed = false;
                        mHandler.removeCallbacks(mBackLongPress);
                        if (mIsBackLongPressed) {
                            mIsBackLongPressed = false;
                            mIsBackIterationPressed = false;
                            param.setResult(-1);
                            return;
                        } else if (event.getRepeatCount() == 0) {
                            if (mIsBackDoubleTap) {
                                // we are still waiting for double-tap
                                XpLog.log("BACK doubletap pending. Ignoring.");
                            } else if (mIsBackIterationPressed) {
                                mIsBackIterationPressed = false;
                            } else if (!mWasBackDoubleTap && !event.isCanceled()) {
                                if (ActionManager.getInstance().getActionFor(HwKeyTrigger.BACK_SINGLETAP).actionId != SettingConstant.HWKEY_ACTION_DEFAULT) {
                                    ActionManager.getInstance().performAction(HwKeyTrigger.BACK_SINGLETAP);
                                } else {
                                    XpLog.log("Triggering original DOWN/UP events for BACK key");
                                    actionManager.injectKey(KeyEvent.KEYCODE_BACK);
                                }
                            }
                            param.setResult(-1);
                            return;
                        }
                    } else if (event.getRepeatCount() == 0) {
                        mBackKeyPressed = true;
                        mIsBackIterationPressed = false;
                        mWasBackDoubleTap = mIsBackDoubleTap;
                        if (mIsBackDoubleTap) {
                            ActionManager.getInstance().performAction(HwKeyTrigger.BACK_DOUBLETAP);
                            mHandler.removeCallbacks(mBackDoubleTapReset);
                            mIsBackDoubleTap = false;
                        } else {
                            mIsBackLongPressed = false;
                            mIsBackDoubleTap = false;
                            if (ActionManager.getInstance().getActionFor(HwKeyTrigger.BACK_DOUBLETAP).actionId != SettingConstant.HWKEY_ACTION_DEFAULT) {
                                mIsBackDoubleTap = true;
                                mHandler.postDelayed(mBackDoubleTapReset, ActionManager.getInstance().getDoubletapSpeed());
                            }
                            if (ActionManager.getInstance().getActionFor(HwKeyTrigger.BACK_LONGPRESS).actionId != SettingConstant.HWKEY_ACTION_DEFAULT) {
                                mHandler.postDelayed(mBackLongPress,
                                        getLongpressTimeoutForAction(
                                                ActionManager.getInstance().getActionFor(HwKeyTrigger.BACK_LONGPRESS).actionId));
                            }
                        }
                        param.setResult(-1);
                        return;
                    } else {
                        XpLog.log("此处有长按事件");
                        mIsBackIterationPressed = true;
                    }
                }

                if (keyCode == KeyEvent.KEYCODE_APP_SWITCH && isFromSystem && !ActionManager.getInstance().isTaskLocked() && ActionManager.getInstance().hasAction(HwKey.APP_SWITCH)) {
                    if (!down) {
                        mAppSwitchKeyPressed = false;
                        mHandler.removeCallbacks(mAppSwitchLongPress);
                        if (mIsAppSwitchLongPressed) {
                            mIsAppSwitchLongPressed = false;
                            mIsAppSwitchIterationPressed = false;
                            param.setResult(-1);
                            return;
                        } else if (event.getRepeatCount() == 0) {
                            if (mIsAppSwitchDoubleTap) {
                                // we are still waiting for double-tap
                                XpLog.log("APP_SWITCH doubletap pending. Ignoring.");
                            } else if (mIsAppSwitchIterationPressed) {
                                mIsAppSwitchIterationPressed = false;
                            } else if (!mWasAppSwitchDoubleTap && !event.isCanceled()) {
                                //log("走到bug");
                                if (ActionManager.getInstance().getActionFor(HwKeyTrigger.APP_SWITCH_SINGLETAP).actionId != SettingConstant.HWKEY_ACTION_DEFAULT) {
                                    ActionManager.getInstance().performAction(HwKeyTrigger.APP_SWITCH_SINGLETAP);
                                } else {
                                    XpLog.log("Triggering original DOWN/UP events for APP_SWITCH key");
                                    actionManager.injectKey(KeyEvent.KEYCODE_APP_SWITCH);
                                }
                            }
                            param.setResult(-1);
                            return;
                        } else {
                            XpLog.log("此处双击松开");
                        }
                    } else if (event.getRepeatCount() == 0) {
                        mAppSwitchKeyPressed = true;
                        mIsAppSwitchIterationPressed = false;
                        mWasAppSwitchDoubleTap = mIsAppSwitchDoubleTap;
                        if (mIsAppSwitchDoubleTap) {
                            ActionManager.getInstance().performAction(HwKeyTrigger.APP_SWITCH_DOUBLETAP);
                            mHandler.removeCallbacks(mAppSwitchDoubleTapReset);
                            mIsAppSwitchDoubleTap = false;
                        } else {
                            mIsAppSwitchLongPressed = false;
                            mIsAppSwitchDoubleTap = false;
                            if (ActionManager.getInstance().getActionFor(HwKeyTrigger.APP_SWITCH_DOUBLETAP).actionId !=
                                    SettingConstant.HWKEY_ACTION_DEFAULT) {
                                mIsAppSwitchDoubleTap = true;
                                mHandler.postDelayed(mAppSwitchDoubleTapReset, ActionManager.getInstance().getDoubletapSpeed());
                            }
                            if (ActionManager.getInstance().getActionFor(HwKeyTrigger.APP_SWITCH_LONGPRESS).actionId !=
                                    SettingConstant.HWKEY_ACTION_DEFAULT) {
                                mHandler.postDelayed(mAppSwitchLongPress,
                                        getLongpressTimeoutForAction(
                                                ActionManager.getInstance().getActionFor(HwKeyTrigger.APP_SWITCH_LONGPRESS).actionId));
                            }
                        }
                        param.setResult(-1);
                        return;
                    } else {
                        mIsAppSwitchIterationPressed = true;
                        XpLog.log("此处有长按事件");
                    }
                }
                if (keyCode == SamsungKeyEvent.KEYCODE_BIXBY && isFromSystem && !ActionManager.getInstance().isTaskLocked() && ActionManager.getInstance().hasAction(HwKey.BIXBY)) {
                    if (down) {
                        if (event.getRepeatCount() == 0) {
                            mIsBixbyIterationPressed = false;
                        } else {
                            XpLog.log("此处有长按事件");
                            mIsBixbyIterationPressed = true;
                        }
                    }
                }
            }
        }
    };
    private XC_MethodHook interceptKeyBeforeQueueingMethodHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            if (!MethodHookInterceptKeyBefore.mSystemBooted) {
                if (XposedHelpers.getBooleanField(param.thisObject, "mSystemBooted")) {
                    MethodHookInterceptKeyBefore.mSystemBooted = true;
                } else {
                    return;
                }
            }
            if (AssistiveTouch.isOxygenOsRomOrH2OsRom()) {
                MethodHookInterceptKeyBefore.this.actionManager.initPreferences();
            }
            if (MethodHookInterceptKeyBefore.this.actionManager.isIsOpenMainWitch()) {
                KeyEvent event = (KeyEvent) param.args[0];
                XpLog.log("有主要按钮点击");
                int keyCode = event.getKeyCode();
                boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
                boolean keyguardOn = (Boolean) XposedHelpers.callMethod(MethodHookPm.getInstance().getPhoneWindowManager(), "keyguardOn");
                boolean isFromSystem = (event.getFlags() & KeyEvent.FLAG_FROM_SYSTEM) != 0;
                XpLog.log("interceptKeyBeforeQueueing: keyCode=" + keyCode +
                        "; action=" + event.getAction() + "; repeatCount=" + event.getRepeatCount() +
                        "; flags=0x" + Integer.toHexString(event.getFlags()) +
                        "; source=" + event.getSource());
                Handler mHandler = ActionManager.getInstance().getSystemHandler();
                if (event.getSource() == InputDevice.SOURCE_UNKNOWN ||
                        event.getSource() == HookConstant.PA_SOURCE_CUSTOM) {
                    // ignore unknown source events, e.g. synthetic events injected from GB itself
                    XpLog.log("interceptKeyBeforeQueueing: ignoring event from unknown source");
                    if (Utils.isOxygenOsRom() || Utils.isH2OsRom()) {
                        // mangle OOS3.5 key event to allow pass-through and to avoid double-vibrations
                        event = KeyEvent.changeFlags(event, event.getFlags() | KeyEvent.FLAG_VIRTUAL_HARD_KEY);
                        event.setSource(InputDevice.SOURCE_KEYBOARD);
                        param.args[0] = event;
                    } else if (Utils.isParanoidRom() && event.getSource() == InputDevice.SOURCE_UNKNOWN) {
                        // mangle PA key event to allow pass-through
                        event = KeyEvent.changeFlags(event, event.getFlags() | KeyEvent.FLAG_VIRTUAL_HARD_KEY);
                        event.setSource(HookConstant.PA_SOURCE_CUSTOM);
                        param.args[0] = event;
                    }
                    return;
                }
                if (keyCode == KeyEvent.KEYCODE_HOME && !ActionManager.getInstance().isTaskLocked()) {
                    if (!down) {
                        mHandler.removeCallbacks(mHomeLongPress);
                        if (mIsHomeLongPressed) {
                            mIsHomeLongPressed = false;
                            param.setResult(0);
                            return;
                        }
                        /*if (event.getRepeatCount() == 0 && (event.getFlags() & KeyEvent.FLAG_FROM_SYSTEM) != 0) {
                            XpLog.log("HOME KeyEvent coming from HW key and keys disabled. Ignoring.");
                            param.setResult(0);
                            return;
                        }*/
                    } else {
                        if (event.getRepeatCount() == 0) {
                            mIsHomeLongPressed = false;
                            if (ActionManager.getInstance().getActionFor(HwKeyTrigger.HOME_LONGPRESS).actionId != SettingConstant.HWKEY_ACTION_DEFAULT) {
                                mHandler.postDelayed(mHomeLongPress, ViewConfiguration.getLongPressTimeout());
                            }
                        } else {
                            if (ActionManager.getInstance().getActionFor(HwKeyTrigger.HOME_LONGPRESS).actionId != SettingConstant.HWKEY_ACTION_DEFAULT) {
                                param.setResult(0);
                            }
                            XpLog.log("RepeatCount：" + event.getRepeatCount() + "");
                        }
                        return;
                    }
                }
                if (keyCode == SamsungKeyEvent.KEYCODE_BIXBY && isFromSystem && !ActionManager.getInstance().isTaskLocked() && ActionManager.getInstance().hasAction(HwKey.BIXBY)) {
                    if (!down) {
                        mBixbyKeyPressed = false;
                        mHandler.removeCallbacks(mBixbyLongPress);
                        if (mIsBixbyLongPressed) {
                            mIsBixbyLongPressed = false;
                            mIsBixbyIterationPressed = false;
                            param.setResult(-1);
                            return;
                        } else if (event.getRepeatCount() == 0) {
                            if (mIsBixbyDoubleTap) {
                                // we are still waiting for double-tap
                                XpLog.log("BIXBY doubletap pending. Ignoring.");
                            } else if (mIsBixbyIterationPressed) {
                                mIsBixbyIterationPressed = false;
                            } else if (!mWasBixbyDoubleTap && !event.isCanceled()) {
                                if (ActionManager.getInstance().getActionFor(HwKeyTrigger.BIXBY_SINGLETAP).actionId != SettingConstant.HWKEY_ACTION_DEFAULT) {
                                    ActionManager.getInstance().performAction(HwKeyTrigger.BIXBY_SINGLETAP);
                                    XpLog.log("此处走到bug");
                                } else {
                                    XpLog.log("Triggering original DOWN/UP events for BIXBY key");
                                    actionManager.injectKey(SamsungKeyEvent.KEYCODE_BIXBY);
                                }
                            }
                            param.setResult(-1);
                            return;
                        }
                    } else if (event.getRepeatCount() == 0) {
                        mBixbyKeyPressed = true;
                        mWasBixbyDoubleTap = mIsBixbyDoubleTap;
                        if (mIsBixbyDoubleTap) {
                            ActionManager.getInstance().performAction(HwKeyTrigger.BIXBY_DOUBLETAP);
                            mHandler.removeCallbacks(mBixbyDoubleTapReset);
                            mIsBixbyDoubleTap = false;
                        } else {
                            mIsBixbyLongPressed = false;
                            mIsBixbyDoubleTap = false;
                            if (ActionManager.getInstance().getActionFor(HwKeyTrigger.BIXBY_DOUBLETAP).actionId !=
                                    SettingConstant.HWKEY_ACTION_DEFAULT) {
                                mIsBixbyDoubleTap = true;
                                mHandler.postDelayed(mBixbyDoubleTapReset, ActionManager.getInstance().getDoubletapSpeed());
                            }
                            if (ActionManager.getInstance().getActionFor(HwKeyTrigger.BIXBY_LONGPRESS).actionId !=
                                    SettingConstant.HWKEY_ACTION_DEFAULT) {
                                mHandler.postDelayed(mBixbyLongPress,
                                        getLongpressTimeoutForAction(
                                                ActionManager.getInstance().getActionFor(HwKeyTrigger.BIXBY_LONGPRESS).actionId));
                            }
                        }
                        param.setResult(-1);
                        return;
                    }
                }
            }
        }
    };
    private Runnable mAppSwitchDoubleTapReset = new Runnable() {
        public void run() {
            MethodHookInterceptKeyBefore.mIsAppSwitchDoubleTap = false;
            if (!MethodHookInterceptKeyBefore.mAppSwitchKeyPressed) {
                if (MethodHookInterceptKeyBefore.this.actionManager.getActionFor(HwKeyTrigger.APP_SWITCH_SINGLETAP).actionId != 0) {
                    XpLog.log("APP_SWITCH key double tap timed out and key not pressed; performing singletap action");
                    MethodHookInterceptKeyBefore.this.actionManager.performAction(HwKeyTrigger.APP_SWITCH_SINGLETAP);
                    return;
                }
                XpLog.log("APP_SWITCH key double tap timed out and key not pressed; injecting APP_SWITCH key");
                MethodHookInterceptKeyBefore.this.actionManager.injectKey(KeyEvent.KEYCODE_APP_SWITCH);
            }
        }
    };
    private Runnable mAppSwitchLongPress = new Runnable() {
        public void run() {
            XpLog.log("mAppSwitchLongPress runnable launched");
            MethodHookInterceptKeyBefore.mIsAppSwitchLongPressed = true;
            MethodHookInterceptKeyBefore.this.actionManager.performAction(HwKeyTrigger.APP_SWITCH_LONGPRESS);
        }
    };
    private Runnable mBackDoubleTapReset = new Runnable() {
        public void run() {
            MethodHookInterceptKeyBefore.mIsBackDoubleTap = false;
            if (!MethodHookInterceptKeyBefore.mBackKeyPressed) {
                if (MethodHookInterceptKeyBefore.this.actionManager.getActionFor(HwKeyTrigger.BACK_SINGLETAP).actionId != 0) {
                    XpLog.log("BACK key double tap timed out and key not pressed; performing singletap action");
                    MethodHookInterceptKeyBefore.this.actionManager.performAction(HwKeyTrigger.BACK_SINGLETAP);
                    return;
                }
                XpLog.log("BACK key double tap timed out and key not pressed; injecting BACK key");
                MethodHookInterceptKeyBefore.this.actionManager.injectKey(KeyEvent.KEYCODE_BACK);
            }
        }
    };
    private Runnable mBackLongPress = new Runnable() {
        public void run() {
            XpLog.log("mBackLongPress runnable launched");
            MethodHookInterceptKeyBefore.mIsBackLongPressed = true;
            MethodHookInterceptKeyBefore.this.actionManager.performAction(HwKeyTrigger.BACK_LONGPRESS);
        }
    };
    private Runnable mBixbyDoubleTapReset = new Runnable() {
        public void run() {
            MethodHookInterceptKeyBefore.mIsBixbyDoubleTap = false;
            if (!MethodHookInterceptKeyBefore.mBixbyKeyPressed) {
                if (MethodHookInterceptKeyBefore.this.actionManager.getActionFor(HwKeyTrigger.BIXBY_SINGLETAP).actionId != 0) {
                    XpLog.log("BIXBY key double tap timed out and key not pressed; performing singletap action");
                    MethodHookInterceptKeyBefore.this.actionManager.performAction(HwKeyTrigger.BIXBY_SINGLETAP);
                    return;
                }
                XpLog.log("BIXBY key double tap timed out and key not pressed; injecting BIXBY key");
                MethodHookInterceptKeyBefore.this.actionManager.injectKey(SamsungKeyEvent.KEYCODE_BIXBY);
            }
        }
    };
    private Runnable mBixbyLongPress = new Runnable() {
        public void run() {
            XpLog.log("mAppSwitchLongPress runnable launched");
            MethodHookInterceptKeyBefore.mIsBixbyLongPressed = true;
            MethodHookInterceptKeyBefore.this.actionManager.performAction(HwKeyTrigger.BIXBY_LONGPRESS);
        }
    };
    private Runnable mHomeLongPress = new Runnable() {
        public void run() {
            MethodHookInterceptKeyBefore.mIsHomeLongPressed = true;
            MethodHookInterceptKeyBefore.this.actionManager.performAction(HwKeyTrigger.HOME_LONGPRESS);
        }
    };
    private Runnable mMenuDoubleTapReset = new Runnable() {
        public void run() {
            MethodHookInterceptKeyBefore.mIsMenuDoubleTap = false;
            if (!MethodHookInterceptKeyBefore.mMenuKeyPressed) {
                if (MethodHookInterceptKeyBefore.this.actionManager.getActionFor(HwKeyTrigger.MENU_SINGLETAP).actionId != 0) {
                    XpLog.log("MENU key double tap timed out and key not pressed; performing singletap action");
                    MethodHookInterceptKeyBefore.this.actionManager.performAction(HwKeyTrigger.MENU_SINGLETAP);
                    return;
                }
                XpLog.log("MENU key double tap timed out and key not pressed; injecting MENU key");
                MethodHookInterceptKeyBefore.this.actionManager.injectKey(KeyEvent.KEYCODE_MENU);
            }
        }
    };
    private Runnable mMenuLongPress = new Runnable() {
        public void run() {
            XpLog.log("mMenuLongPress runnable launched");
            MethodHookInterceptKeyBefore.mIsMenuLongPressed = true;
            MethodHookInterceptKeyBefore.this.actionManager.performAction(HwKeyTrigger.MENU_LONGPRESS);
        }
    };

    /*private int getKeyCode(KeyEvent event) {
        if (this.actionManager.isIsSwitchAppSwitchAndBack()) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_APP_SWITCH) {
                XposedHelpers.setIntField(event, "mKeyCode", KeyEvent.KEYCODE_APP_SWITCH);
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                XposedHelpers.setIntField(event, "mKeyCode", KeyEvent.KEYCODE_BACK);
            }
        }
        return event.getKeyCode();
    }*/

    XC_MethodHook getInterceptKeyBeforeQueueingMethodHook() {
        return this.interceptKeyBeforeQueueingMethodHook;
    }

    XC_MethodHook getInterceptKeyBeforeDispatchingMethodHook() {
        return this.interceptKeyBeforeDispatchingMethodHook;
    }

    private int getLongpressTimeoutForAction(int action) {
        return action == -1 ? this.actionManager.getKillDelay() : ViewConfiguration.getLongPressTimeout();
    }

    /*private boolean areHwKeysEnabled() {
        return mHwKeysEnabled;
    }*/

}
