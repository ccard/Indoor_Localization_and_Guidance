package ccard.thesis.Indoor_Localization_and_Guidance.Frontend;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes.ComputationManager;
import ccard.thesis.Indoor_Localization_and_Guidance.R;
import org.json.JSONObject;

/**
 * Created by Ch on 3/19/14.
 * This Class displays the guidance for the user
 */
public class Guidance extends Activity {


    private AsyncTask<Integer,JSONObject,Integer> comp;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guidance_view);


    }

    @Override
    public void onResume(){
        super.onResume();
        //TODO: move this to another method
        if(comp == null) {
            comp = new ComputationManager(this).execute(1);
        } else if (comp.isCancelled()) {
            comp = new ComputationManager(this).execute(1);
        }

    }

    @Override
    public void onPause(){
        comp.cancel(true);
        super.onPause();

    }


}