package com.slc.assistivetouch.provider;

import com.slc.assistivetouch.model.SettingConstant;
import com.slc.code.provider.RemotePreferenceProvider;

public class MainSettingProvider extends RemotePreferenceProvider {
    public MainSettingProvider() {
        super(SettingConstant.AUTHORITIES, SettingConstant.APP_PREFERENCES_NAME);
    }
}
