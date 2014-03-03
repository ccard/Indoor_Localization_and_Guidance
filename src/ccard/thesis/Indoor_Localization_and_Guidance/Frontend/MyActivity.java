package ccard.thesis.Indoor_Localization_and_Guidance.Frontend;

import android.app.Activity;
import android.os.Bundle;
import ccard.thesis.Indoor_Localization_and_Guidance.R;
import org.opencv.*;
import org.opencv.android.OpenCVLoader;

public class MyActivity extends Activity {
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
}
