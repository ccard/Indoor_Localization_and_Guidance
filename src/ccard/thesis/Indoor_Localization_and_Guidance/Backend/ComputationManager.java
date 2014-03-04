package ccard.thesis.Indoor_Localization_and_Guidance.Backend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.ImageView;
import org.json.JSONObject;
import org.opencv.core.Size;

import java.util.ArrayList;
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
        //TODO: init db here
        res = new ResReceiver(new Handler());
        capture = new CameraCapture(context,new Size(400,400));
        descriptor = new ORBDescriptor();

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

        while (run){
            //TODO: image proccessing computations here
        }

    }

    private void postImage(ImageContainer img){
        if(img.hasImageToDraw()){
            //TODO: create the interface that this will be used with
            ImageView view = (ImageView)((Activity)context).getWindow().getDecorView().findViewById(1);
            img.render(view,false);
        }
    }
}
