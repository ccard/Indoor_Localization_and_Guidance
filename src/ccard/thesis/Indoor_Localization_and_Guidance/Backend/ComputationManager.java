package ccard.thesis.Indoor_Localization_and_Guidance.Backend;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;

/**
 * Created by Chris on 3/3/14.
 * This class manages the management and computation so that the
 * GUI thread remains free for other tasks
 */
public class ComputationManager implements Runnable{

    private Context context;
    private boolean run;

    public ComputationManager(Context cont){
        context = cont;
        run = true;
    }

    @Override
    public void run() {

    }

    private void postImage(ImageContainer img){
        if(img.hasImageToDraw()){
            //TODO: create the interface that this will be used with
            ImageView view = (ImageView)((Activity)context).getWindow().getDecorView().findViewById(1);
            img.render(view,false);
        }
    }
}
