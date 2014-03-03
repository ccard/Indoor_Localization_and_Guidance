package ccard.thesis.Indoor_Localization_and_Guidance.Backend;

import android.content.Context;

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
}
