package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.*;
import ccard.thesis.Indoor_Localization_and_Guidance.Frontend.MyActivity;
import ccard.thesis.Indoor_Localization_and_Guidance.R;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.features2d.DMatch;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chris on 3/3/14.
 * This class manages the management and computation so that the
 * GUI thread remains free for other tasks
 */
public class ComputationManager extends AsyncTask<Integer,JSONObject,Integer> {

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
    private JSONObject matchParams;

    private Bitmap disp;

    public ComputationManager(Context cont){
        //TODO: Put progress loader in so user doesn't see blank screen
        //TODO: put loading into thread so does hog the gui
        //TODO: gabage collect variables to avoid using too much memory
        context = cont;
        run = true;

        view = new ImageView(context);
        ((FrameLayout)((Activity)context).getWindow()
                .getDecorView().findViewById(R.id.ImageDisplay)).addView(view);

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        db = new LocalTestDB(context);
        Map<DataBase.ParamReturn, JSONObject> params = getParams();
        res = new ResReceiver(new Handler());

        if(prefs.getBoolean("device_camera",false)){
            capture = new CameraCapture(context,new Size(-1,-1));
        } else {
            //TODO: replace with bluetooth or other device
            capture = new CameraCapture(context,new Size(400,400));
        }
        descriptor = new ORBDescriptor();
        matcher = new BFMatcher(context);
        descriptor.initDescriptor(params.get(DataBase.ParamReturn.Descriptor),context);
        matcher.setTrainingParams(params.get(DataBase.ParamReturn.Matcher));
        pv = new LocalImageProvider();
        pv.setDatabase(db);

        matchParams = new JSONObject();
        try {
            matchParams.put("Type", Matcher.MatchingType.BruteForce);
            matchParams.put("k",5);
            matchParams.put("compactResults",false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    private JSONObject formRender(Bitmap img){
        JSONObject j = new JSONObject();
        try {
            j.put("Type",1);
            disp = img;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return j;
    }

    private JSONObject formProgress(boolean startStop){
        JSONObject j = new JSONObject();
        try {
            j.put("Type",2);
            j.put("Data",startStop);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return j;
    }

    private JSONObject formToast(String message){
        JSONObject j = new JSONObject();
        try {
            j.put("Type",3);
            j.put("Data",message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return j;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        //publishProgress(formProgress(true));
        //pv.requestImages(null,descriptor);
        //matcher.train(pv);
        //publishProgress(formProgress(false));
        if (capture.open()) {
            while (run){
                if(isCancelled()) break;
                MyMat query = capture.capture();
                if (query == null) continue;
                Mat rot = Imgproc.getRotationMatrix2D(new Point(query.rows()/2,query.cols()/2),90,1);
                Imgproc.warpAffine(query,query,rot,query.size());
                if(query.calcDescriptor(descriptor)){
                    publishProgress(formRender(query.render(true)));
                    /*ArrayList<ArrayList<DMatch>> matches = matcher.match(matchParams,query);
                    if (null == matches) continue;
                    int choice = matcher.verify(matches,pv,query,1.5,17);
                    publishProgress(formToast("Chosen: "+choice));*/
                } else {
                    publishProgress(formRender(query.render(false)));
                }
            }
            capture.close();
        } else {
            run = false;
        }

        return 1;
    }

    @Override
    protected void onPostExecute(Integer res){
        run = false;
    }

    @Override
    protected void onProgressUpdate(JSONObject... values) {
        super.onProgressUpdate(values);
        JSONObject data = values[0];
        try {
            switch (data.getInt("Type")){
                case 1:
                    //Bitmap img = (Bitmap)data.get("Data");
                    if (null == disp) break;
                    //view.setImageBitmap(disp);
                    view.setBackground(new BitmapDrawable(context.getResources(),disp));
                    break;
                case 2:
                    if (data.getBoolean("Data")){
                        //TODO: do something
                    } else {
                        //TODO: do something
                    }
                    break;
                case 3:
                    Toast.makeText(context,data.getString("Data"),5000).show();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
