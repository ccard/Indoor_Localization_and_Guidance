package ccard.thesis.Indoor_Localization_and_Guidance.Frontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import ccard.thesis.Indoor_Localization_and_Guidance.R;
import org.opencv.android.OpenCVLoader;

public class MyActivity extends Activity {

    public static final String PREFERENCES = "SETTINGS";
    static{
        if(!OpenCVLoader.initDebug()){
        //TODO: error reporting
        }
    }
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inf = getMenuInflater();
        inf.inflate(R.menu.settings,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.settings:
                Intent i = new Intent(this,SettingsAct.class);
                startActivity(i);
                return true;
        }
        return true;
    }

    public void guide(View view){
        //TODO: Finish implement
    }
}
