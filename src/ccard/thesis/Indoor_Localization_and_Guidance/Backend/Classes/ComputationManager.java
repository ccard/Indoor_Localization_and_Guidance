package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.*;
import ccard.thesis.Indoor_Localization_and_Guidance.R;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Size;
import org.opencv.features2d.DMatch;

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
    private FrameLayout view;
    private TextView textView;
    private JSONObject matchParams;

    private Bitmap disp;
    private ProgressDialog prog;

    public ComputationManager(Context cont){
        //TODO: gabage collect variables to avoid using too much memory
        context = cont;
        run = true;

        /*
        * The initialization sequence must happen in the following order:
        * 1. initGuiComp - initializes the values from the gui part of the program
        * 2. initDbInterface - initializes the image providers and the db
        * 3. initCapture - initialize the capture device
        * 4. initImageProcessing - initalizes the objects that will perform image processing
        */
        initGuiComp();

        initDbInterface();

        initCapture();

        initImageProcessing(getParams());
    }

    /**
     * Initializes gui components that are used
     */
    private void initGuiComp(){
        textView = ((TextView)((Activity)context).getWindow()
                .getDecorView().findViewById(R.id.ShowLocation));
        view = ((FrameLayout)((Activity)context).getWindow()
                .getDecorView().findViewById(R.id.ImageDisplay));

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * This initializes components that interact with the database
     */
    private void initDbInterface(){
        db = new LocalTestDB(context);
        pv = new LocalImageProvider();
        pv.setDatabase(db);
        res = new ResReceiver(new Handler());
    }

    /**
     * This initializes the capture devices
     */
    private void initCapture(){
        if(prefs.getBoolean("device_camera",false)){
            capture = new CameraCapture(context,new Size(-1,-1));
        } else {
            //TODO: replace with bluetooth or other device
            capture = new CameraCapture(context,new Size(400,400));
        }
    }

    /**
     * This initializes the image processing components
     * @param params
     */
    private void initImageProcessing(Map<DataBase.ParamReturn,JSONObject> params){
        descriptor = new ORBDescriptor();
        matcher = new BFMatcher(context);
        descriptor.initDescriptor(params.get(DataBase.ParamReturn.Descriptor),context);
        matcher.setTrainingParams(params.get(DataBase.ParamReturn.Matcher));

        matchParams = new JSONObject();
        try {
            matchParams.put("Type", Matcher.MatchingType.BruteForce);
            matchParams.put("k",5);
            matchParams.put("compactResults",true);
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

    private JSONObject formMessage(String message){
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
        if (!pv.hasImages()){
            publishProgress(formProgress(true));
            pv.requestImages(null,descriptor);
            matcher.train(pv);
            publishProgress(formProgress(false));
        }
        if (capture.open()) {
            while (run){
                if(isCancelled()) break;
                MyMat query = capture.capture();
                if (query == null) continue;

                if(query.calcDescriptor(descriptor)){
                    publishProgress(formRender(query.render(true)));
                    int choice = localize(query);
                    if (choice < 0) continue;
                    publishProgress(formMessage("" + choice));
                } else {
                    publishProgress(formRender(query.render(false)));
                }
                query.release();
            }
            capture.close();
            pv.release();
        } else {
            run = false;
        }

        return 1;
    }

    private int localize(MyMat query){
        ArrayList<ArrayList<DMatch>> matches = matcher.match(matchParams,query);
        if (null == matches) return -1;
        return matcher.verify(matches,pv,query,1.5,20,45);
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
                    if (null == disp) break;
                    view.setBackground(new BitmapDrawable(context.getResources(),disp));
                    disp.recycle();
                    break;
                case 2:
                    if (data.getBoolean("Data")){
                        prog = ProgressDialog.show(context,"Loading","Please wait",false);
                    } else {
                        prog.dismiss();
                    }
                    break;
                case 3:
                    textView.setText(data.getString("Data"));
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
