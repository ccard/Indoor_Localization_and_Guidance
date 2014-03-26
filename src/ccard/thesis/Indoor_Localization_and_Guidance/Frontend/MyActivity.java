package ccard.thesis.Indoor_Localization_and_Guidance.Frontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import ccard.thesis.Indoor_Localization_and_Guidance.R;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;

public class MyActivity extends Activity {

    private BaseLoaderCallback mLoader = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);

        }
    };
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

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5,this,mLoader);
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
                getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new Preferences()).commit();
                return true;
        }
        return true;
    }

    public void guide(View view){
        Intent i = new Intent(this,Guidance.class);
        startActivity(i);
    }
}
