package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.widget.ImageView;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.*;
import org.json.JSONObject;
import org.opencv.core.Size;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chris on 3/3/14.
 * This class manages the management and computation so that the
 * GUI thread remains free for other tasks
 */
public class ComputationManager implements Runnable{

    private Context context;
    private ImageCapture capture;
    private Descriptor descriptor;
    private Matcher matcher;
    private DataBase db;
    private ImageProvidor pv;
    private ResReceiver res;
    private boolean run;

    public ComputationManager(Context cont){
        context = cont;
        run = true;

        db = new LocalTestDB();
        Map<DataBase.ParamReturn, JSONObject> params = getParams();
        res = new ResReceiver(new Handler());
        capture = new CameraCapture(context,new Size(400,400));
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

    @Override
    public void run() {

        capture.open();
        while (run){
            MyMat query = capture.capture();
        }

    }

    @Override
    public void finalize(){

    }



    private void postImage(ImageContainer img){
        if(img.hasImageToDraw()){
            //TODO: create the interface that this will be used with
            ImageView view = (ImageView)((Activity)context).getWindow().getDecorView().findViewById(1);
            img.render(view,false);
        }
    }
}
