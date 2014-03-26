package ccard.thesis.Indoor_Localization_and_Guidance.Frontend;

import android.app.Activity;
import android.os.Bundle;
import ccard.thesis.Indoor_Localization_and_Guidance.R;

/**
 * Created by Ch on 3/26/14.
 */
public class SettingsDialog extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_dialog);
    } 
    @Override
    public void onResume(){
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new Preferences()).commit();
        super.onResume();
    }
}