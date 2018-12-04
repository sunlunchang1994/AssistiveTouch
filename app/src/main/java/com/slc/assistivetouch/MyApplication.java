package com.slc.assistivetouch;

import android.app.Application;

import com.slc.code.app.AppData;
import com.slc.code.exception.DefCrashHandler;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppData.init(this).configure();
        DefCrashHandler.getInstance().init(this);
    }
}
