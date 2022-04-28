package com.octavi.lab.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.internal.util.octavi.OctaviUtils;
import com.octavi.lab.preferences.SystemSettingSwitchPreference;

import java.util.Arrays;
import java.util.HashSet;

import com.android.settings.SettingsPreferenceFragment;

public class BatteryMode extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "XBatteryMode";
    private static final String BATTERY_LEVEL_COLORS = "battery_level_colors";
    private SystemSettingSwitchPreference mBatteryLevelColors;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.octavi_battery_mode);

        ContentResolver resolver = getActivity().getContentResolver();
        mBatteryLevelColors = (SystemSettingSwitchPreference) findPreference(BATTERY_LEVEL_COLORS);
        mBatteryLevelColors.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (preference == mBatteryLevelColors) {
            OctaviUtils.showSystemUiRestartDialog(getActivity());
            return true;
            }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.OCTAVI;
    }
}
