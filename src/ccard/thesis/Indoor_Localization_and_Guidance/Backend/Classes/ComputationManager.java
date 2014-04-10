package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
public class ComputationManager extends AsyncTask<Integer,Bitmap,Integer> {

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

    public ComputationManager(Context cont){
        //TODO: Put progress loader in so user doesn't see blank screen
        //TODO: put loading into thread so does hog the gui
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
            capture = new CameraCapture(context,new Size(400,400));
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
        pv.requestImages(null,descriptor);

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

    @Override
    protected Integer doInBackground(Integer... params) {
        matcher.train(pv);
        if (capture.open()) {
            while (run){
                if(isCancelled()) break;
                MyMat query = capture.capture();
                Mat rot = Imgproc.getRotationMatrix2D(new Point(query.rows()/2,query.cols()/2),90,1);
                Imgproc.warpAffine(query,query,rot,query.size());
                if(query.calcDescriptor(descriptor)){
                    publishProgress(query.render(true));
                    ArrayList<ArrayList<DMatch>> matches = matcher.match(matchParams,query);
                    if (null == matches) continue;
                    int choice = matcher.verify(matches,pv,query,1.5,17);
                    Toast.makeText(context,"found: "+choice,5000).show();
                } else {
                    publishProgress(query.render(false));
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
    protected void onProgressUpdate(Bitmap... values) {
        super.onProgressUpdate(values);
        Bitmap drawMat = values[0];
        view.setImageBitmap(drawMat);
    }
}
