package ccard.thesis.Indoor_Localization_and_Guidance.Frontend;

import android.app.Activity;
import android.os.Bundle;import ccard.thesis.Indoor_Localization_and_Guidance.R;

/**
 * Created by Ch on 3/26/14.
 * This displays the setting fragment
 */
public class SettingsAct extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_menu);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new Preferences()).commit();
    }
}