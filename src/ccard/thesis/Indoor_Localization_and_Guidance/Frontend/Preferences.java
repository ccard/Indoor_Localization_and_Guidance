package ccard.thesis.Indoor_Localization_and_Guidance.Frontend;

import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import ccard.thesis.Indoor_Localization_and_Guidance.R;

/**
 * Created by Ch on 3/18/14.
 */
public class Preferences extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume(){
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause(){
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("camera_input")){
            BluetoothAdapter ad = BluetoothAdapter.getDefaultAdapter();
            if (((CheckBoxPreference)(getPreferenceManager().findPreference("camera_input"))).isChecked()){
                sharedPreferences.edit().putBoolean("device_camera",true).commit();
            } else {
                sharedPreferences.edit().putBoolean("device_camera",false).commit();
                if (ad != null) {
                    if(!ad.isEnabled()){
                        CheckBoxPreference c = (CheckBoxPreference)(getPreferenceManager().findPreference("bluetooth_input"));
                        c.setEnabled(false);
                        ((CheckBoxPreference)(getPreferenceManager().findPreference("camera_input"))).setChecked(true);
                    }
                }
            }
            CheckBoxPreference c = (CheckBoxPreference)(getPreferenceManager().findPreference("bluetooth_input"));
            c.setChecked(false);
        }
    }
}