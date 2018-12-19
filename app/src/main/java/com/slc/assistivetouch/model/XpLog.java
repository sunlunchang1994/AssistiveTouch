package com.slc.assistivetouch.model;

import android.util.Log;

import de.robv.android.xposed.XposedBridge;

public class XpLog {
    private static final boolean DEBUG = false;
    private static final String TAG = "AssistiveTouch";

    public static void log(String message) {
        log(TAG, message);
    }

    public static void log(String tag, String message) {
        log(tag, message, DEBUG);
    }

    public static void log(String message, boolean isShow) {
        log(TAG, message, isShow);
    }

    public static void log(String tag, String message, boolean isDebug) {
        if (isDebug) {
            XposedBridge.log(tag + ": " + message);
        }
    }

    public static void log(Throwable t) {
        log(TAG, t);
    }

    public static void log(String tag, Throwable t) {
        log(tag, t, DEBUG);
    }

    public static void log(Throwable t, boolean isShow) {
        log(TAG, t, isShow);
    }

    public static void log(String tag, Throwable t, boolean isShow) {
        if (isShow) {
            XposedBridge.log(tag + ": " + Log.getStackTraceString(t));
        }
    }
}
