package com.slc.assistivetouch.ui.fragment;

import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.SwitchPreference;
import android.widget.Toast;

import com.slc.assistivetouch.R;
import com.slc.assistivetouch.contract.MainSettingContract.MainSettingPresenter;
import com.slc.assistivetouch.contract.MainSettingContract.MainSettingView;
import com.slc.assistivetouch.model.kernel.SamsungKeyEvent;
import com.slc.assistivetouch.model.SettingConstant;
import com.slc.assistivetouch.presenter.MainSettingPresenterImp;
import com.slc.code.ui.fragment.PreferenceBaseFragment;

public class MainSettingFragment extends PreferenceBaseFragment<MainSettingPresenter> implements OnPreferenceChangeListener, MainSettingView, OnPreferenceClickListener {
    private boolean isAllowOpen;
    private ListPreference preference_action_app_switch;
    private ListPreference preference_action_app_switch_double;
    private ListPreference preference_action_app_switch_long;
    private ListPreference preference_action_back;
    private ListPreference preference_action_back_double;
    private ListPreference preference_action_back_long;
    private ListPreference preference_action_bixby;
    private ListPreference preference_action_bixby_double;
    private ListPreference preference_action_bixby_long;
    private ListPreference preference_action_home_long;
    private ListPreference preference_action_menu;
    private ListPreference preference_action_menu_double;
    private ListPreference preference_action_menu_long;
    private SwitchPreference preference_action_switch_app_switch_and_back;
    private ListPreference preference_custom_actions_temp;
    private SwitchPreference preference_main_switch;

    @Override
    protected int getPreferenceFromResource() {
        return R.xml.key_system_preference;
    }

    @Override
    public void fromResourceBefore() {
        super.fromResourceBefore();
        Bundle bundle = getArguments();
        //this.isAllowOpen = bundle.getBoolean(SettingConstant.Ga.KEY_IS_ALLOW_OPEN); TODO
        this.isAllowOpen = true;
        MainSettingPresenterImp.initialize(this);
        getPresenter().init(bundle);
    }

    @Override
    public void fromResourceLater() {
        this.preference_main_switch = (SwitchPreference) findPreference(SettingConstant.PREF_KEY_HWKEY_MAIN_SWITCH);
        this.preference_main_switch.setOnPreferenceChangeListener(this);
        this.preference_action_switch_app_switch_and_back = (SwitchPreference) findPreference(SettingConstant.PREF_KEY_HWKEY_SWITCH_APP_SWITCH_AND_BACK);
        this.preference_action_switch_app_switch_and_back.setOnPreferenceChangeListener(this);
        this.preference_action_bixby = findListPreferenceAndInitBaseConfig(SettingConstant.PREF_KEY_HWKEY_BIXBY);
        this.preference_action_bixby_long = findListPreferenceAndInitBaseConfig(SettingConstant.PREF_KEY_HWKEY_BIXBY_LONG);
        this.preference_action_bixby_double = findListPreferenceAndInitBaseConfig(SettingConstant.PREF_KEY_HWKEY_BIXBY_DOUBLE);
        this.preference_action_home_long = findListPreferenceAndInitBaseConfig(SettingConstant.PREF_KEY_HWKEY_HOME_LONG);
        this.preference_action_back = findListPreferenceAndInitBaseConfig(SettingConstant.PREF_KEY_HWKEY_BACK);
        this.preference_action_back_long = findListPreferenceAndInitBaseConfig(SettingConstant.PREF_KEY_HWKEY_BACK_LONG);
        this.preference_action_back_double = findListPreferenceAndInitBaseConfig(SettingConstant.PREF_KEY_HWKEY_BACK_DOUBLE);
        this.preference_action_app_switch = findListPreferenceAndInitBaseConfig(SettingConstant.PREF_KEY_HWKEY_APP_SWITCH);
        this.preference_action_app_switch_long = findListPreferenceAndInitBaseConfig(SettingConstant.PREF_KEY_HWKEY_APP_SWITCH_LONG);
        this.preference_action_app_switch_double = findListPreferenceAndInitBaseConfig(SettingConstant.PREF_KEY_HWKEY_APP_SWITCH_DOUBLE);
        this.preference_action_menu = findListPreferenceAndInitBaseConfig(SettingConstant.PREF_KEY_HWKEY_MENU);
        this.preference_action_menu_long = findListPreferenceAndInitBaseConfig(SettingConstant.PREF_KEY_HWKEY_MENU_LONG);
        this.preference_action_menu_double = findListPreferenceAndInitBaseConfig(SettingConstant.PREF_KEY_HWKEY_MENU_DOUBLE);
        if (!SamsungKeyEvent.MANUFACTURER_SAMSUNG.equals(Build.MANUFACTURER)) {
            getPreferenceScreen().removePreference(findPreference(getString(R.string.pref_key_samsung_os_dedicated)));
        }
        findPreference(SettingConstant.PREF_KEY_DONATE).setOnPreferenceClickListener(this);
    }

    private void setListPreferenceSummary(ListPreference listPreference) {
        listPreference.setSummary(Integer.parseInt(listPreference.getValue()) == SettingConstant.HWKEY_ACTION_CUSTOM_ACTIONS ?
                getPresenter().getCustomSummary(listPreference.getKey()) : listPreference.getEntry().toString());
    }

    private ListPreference findListPreferenceAndInitBaseConfig(CharSequence key) {
        ListPreference listPreference = (ListPreference) findPreference(key);
        setListPreferenceSummary(listPreference);
        listPreference.setOnPreferenceChangeListener(this);
        return listPreference;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (SettingConstant.PREF_KEY_HWKEY_MAIN_SWITCH.equals(key)) {
            if (this.isAllowOpen) {
                String str = SettingConstant.Ga.ACTION_PREF_KEY_HWKEY_SWITCH;
                getPresenter().sendBroadcastFromPreferenceChange(str, key, this.preference_main_switch.isChecked());
            }else{
                Toast.makeText(getMvpContext(),R.string.label_main_switch_summary,Toast.LENGTH_LONG).show();
            }
            return this.isAllowOpen;
        }
        if (SettingConstant.PREF_KEY_HWKEY_SWITCH_APP_SWITCH_AND_BACK.equals(key)) {
            getPresenter().sendBroadcastFromPreferenceChange(SettingConstant.Ga.ACTION_PREF_KEY_HWKEY_SWITCH, key, this.preference_action_switch_app_switch_and_back.isChecked());
        } else if (Integer.parseInt(newValue.toString()) == SettingConstant.HWKEY_ACTION_CUSTOM_ACTIONS) {
            this.preference_custom_actions_temp = (ListPreference) preference;
            getPresenter().showLoadingAppDialog(preference.getKey());
            return false;
        } else if (preference instanceof ListPreference) {
            preference.setSummary(getPresenter().changeHwKeyOfActionName(key, newValue.toString()));
        }
        return true;
    }

    @Override
    public void refreshCustomItem(String summary, String value) {
        this.preference_custom_actions_temp.setSummary(summary);
        this.preference_custom_actions_temp.setValue(value);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(SettingConstant.PREF_KEY_DONATE)) {
            getPresenter().startDonateUi();
        }
        return false;
    }
}
