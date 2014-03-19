package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.*;
import ccard.thesis.Indoor_Localization_and_Guidance.Frontend.MyActivity;
import ccard.thesis.Indoor_Localization_and_Guidance.R;
import org.json.JSONObject;
import org.opencv.core.Size;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chris on 3/3/14.
 * This class manages the management and computation so that the
 * GUI thread remains free for other tasks
 */
public class ComputationManager extends AsyncTask<Integer,View,Integer> {

    private Context context;
    private ImageCapture capture;
    private Descriptor descriptor;
    private Matcher matcher;
    private DataBase db;
    private ImageProvidor pv;
    private ResReceiver res;
    private boolean run;
    private SharedPreferences prefs;
    private ImageView view;

    public ComputationManager(Context cont){
        context = cont;
        run = true;

        view = (ImageView)((Activity)context).getWindow()
                .getDecorView().findViewById(R.id.ImageDisplay);

        prefs = context.getSharedPreferences(MyActivity.PREFERENCES,0);
        db = new LocalTestDB();
        Map<DataBase.ParamReturn, JSONObject> params = getParams();
        res = new ResReceiver(new Handler());
        if(prefs.getBoolean("device_camera",true)){
            capture = new CameraCapture(context,new Size(400,400));
        } else {
            //TODO: replace with bluetooth or other device
            capture = new CameraCapture(context,new Size(400,400));
        }
        descriptor = new ORBDescriptor();
        matcher = new LSHMatcher(context);
        descriptor.initDescriptor(params.get(DataBase.ParamReturn.Descriptor),context);
        matcher.setTrainingParams(params.get(DataBase.ParamReturn.Matcher));
        pv = new LocalImageProvider();
        pv.setDatabase(db);

    }

    private Map<DataBase.ParamReturn,JSONObject> getParams(){
        Map<DataBase.ParamReturn,JSONObject> params = new HashMap<DataBase.ParamReturn, JSONObject>();

        try {
            db.openConnection();
            params.put(DataBase.ParamReturn.Descriptor,
                    db.getParams(DataBase.ParamReturn.Descriptor));
            params.put(DataBase.ParamReturn.Matcher,
                    db.getParams(DataBase.ParamReturn.Matcher));
            db.closeConnection();
        } catch (DBError dbError) {
            dbError.printStackTrace();
        }
        return params;
    }


    private void postImage(ImageContainer img,boolean with_key){
        if(img.hasImageToDraw()){
            //TODO: create the interface that this will be used with
            img.render(view,with_key);
        }
    }

    @Override
    protected Integer doInBackground(Integer... params) {

        if (capture.open()) {
            while (run){
                MyMat query = capture.capture();
                query.calcDescriptor(descriptor);
                postImage(query,true);
            }
        } else {
            run = false;
        }

        return 1;
    }

    @Override
    protected void onPostExecute(Integer res){
        capture.close();
        run = false;
    }
}