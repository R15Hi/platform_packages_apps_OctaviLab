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
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.ServiceManager;
import android.os.UserHandle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import android.content.om.IOverlayManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.content.BroadcastReceiver;
import android.content.Context;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.CustomOverlayPreferenceController;
import com.android.settings.display.OverlayCategoryPreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.List;

import com.android.settings.SettingsPreferenceFragment;
import com.octavi.support.colorpicker.ColorPickerPreference;

public class OctaviThemes extends DashboardFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "OctaviThemes";

    private Context mContext;
    private IOverlayManager mOverlayManager;

    private static final String SWITCH_STYLE = "switch_style";
    private static final String PREF_RGB_ACCENT_PICKER = "rgb_accent_picker";
    private static final int MENU_RESET = Menu.FIRST;

    static final int DEFAULT = 0xff1a73e8;

    private ColorPickerPreference rgbAccentPicker;
    private ListPreference mSwitchStyle;
    private IOverlayManager mOverlayService;

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.octavi_themes;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        //addPreferencesFromResource(R.xml.octavi_themes);

	mContext =  getActivity();

        mSwitchStyle = (ListPreference) findPreference(SWITCH_STYLE);
        int switchStyle = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.SWITCH_STYLE, 1);
        int valueIndex = mSwitchStyle.findIndexOfValue(String.valueOf(switchStyle));
        mSwitchStyle.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mSwitchStyle.setSummary(mSwitchStyle.getEntry());
        mSwitchStyle.setOnPreferenceChangeListener(this);

        rgbAccentPicker = (ColorPickerPreference) findPreference(PREF_RGB_ACCENT_PICKER);
        String colorVal = Settings.Secure.getStringForUser(mContext.getContentResolver(),
                Settings.Secure.ACCENT_COLOR, UserHandle.USER_CURRENT);
        int color = (colorVal == null)
                ? DEFAULT
                : Color.parseColor("#" + colorVal);
        rgbAccentPicker.setNewPreviewColor(color);
        rgbAccentPicker.setOnPreferenceChangeListener(this);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mSwitchStyle) {
                String value = (String) objValue;
                Settings.System.putInt(getContext().getContentResolver(), Settings.System.SWITCH_STYLE, Integer.valueOf(value));
                int valueIndex = mSwitchStyle.findIndexOfValue(value);
                mSwitchStyle.setSummary(mSwitchStyle.getEntries()[valueIndex]);
	} else if (preference == rgbAccentPicker) {
            int color = (Integer) objValue;
            String hexColor = String.format("%08X", (0xFFFFFFFF & color));
            Settings.Secure.putStringForUser(mContext.getContentResolver(),
                        Settings.Secure.ACCENT_COLOR,
                        hexColor, UserHandle.USER_CURRENT);
            try {
                 mOverlayManager.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
                 mOverlayManager.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
             } catch (RemoteException ignored) {
             }
            return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_reset)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.theme_option_reset_title);
        alertDialog.setMessage(R.string.theme_option_reset_message);
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetValues();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void resetValues() {
        final Context context = getContext();
        rgbAccentPicker = (ColorPickerPreference) findPreference(PREF_RGB_ACCENT_PICKER);
        rgbAccentPicker.setNewPreviewColor(DEFAULT);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.OCTAVI;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle(), this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle, Fragment fragment) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.font"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.adaptive_icon_shape"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.icon_pack.android"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.signal_icon"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.wifi_icon"));        
        controllers.add(new CustomOverlayPreferenceController(context,
		"android.theme.customization.custom_overlays"));
        return controllers;
    }
}