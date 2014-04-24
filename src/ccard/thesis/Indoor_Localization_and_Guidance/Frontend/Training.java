package ccard.thesis.Indoor_Localization_and_Guidance.Frontend;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.BitmapDrawable;
import android.media.audiofx.AudioEffect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes.*;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.DataBase;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.Descriptor;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageContainer;
import ccard.thesis.Indoor_Localization_and_Guidance.R;
import org.json.JSONObject;

import java.util.List;
import java.util.ListIterator;

/**
 * Created by Chris Card on 4/24/14.
 */
public class Training extends Activity {

    private LocalSQLDb sqlDb;
    private LocalTestDB loader;
    private ProgressBar progressBar;
    private Descriptor orb;
    private ListIterator<String> files;
    private int num_files,progression;
    private ImageContainer currImage;
    private FrameLayout frame;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.traing_fomr);

        sqlDb = new LocalSQLDb(this);
        loader = new LocalTestDB(this);

        orb = new ORBDescriptor();
        orb.initDescriptor(loadParams(),this);

        loadFiles();

        progressBar = (ProgressBar)findViewById(R.id.imageProgress);
        progressBar.setMax(num_files);
        progression = 0;

        frame = (FrameLayout)findViewById(R.id.TrainingView);
    }

    private JSONObject loadParams(){
        JSONObject params = null;
        try {
            params = loader.getParams(DataBase.ParamReturn.Descriptor);
        } catch (DBError dbError) {
            LogFile.getInstance().e(dbError.getStackTrace().toString());
            LogFile.getInstance().flushLog();
        }
        return params;
    }

    private void loadFiles(){
        try {
            List<String> temp = loader.getImageFiles();
            num_files = temp.size();
            files = temp.listIterator();
        } catch (DBError dbError) {
            LogFile.getInstance().e(dbError.getStackTrace().toString());
            LogFile.getInstance().flushLog();
        }
    }

    public void save_image(View view){
        //TODO: implement saving functionality
    }


    private class Load_Save extends AsyncTask<String,Void,ImageContainer>{

        private LocalSQLDb sqlDb;
        private LocalTestDB loader;
        private ProgressDialog pd;
        private ImageContainer toSave;
        private Descriptor des;
        public Load_Save(LocalSQLDb db,LocalTestDB testDB,MyMat img,Descriptor des){
            sqlDb = db;
            loader = testDB;
            if (img != null){
                toSave = img;
                this.des = des;
            }
        }
        @Override
        protected ImageContainer doInBackground(String... strings) {
            ImageContainer img = new MyMat();
            if (strings.length == 1){
                String file = strings[0];
                try {
                    img = loader.loadImage(file);
                } catch (DBError dbError) {
                    LogFile.getInstance().e(dbError.getStackTrace().toString());
                    LogFile.getInstance().flushLog();
                }
            } else {
                try {
                    toSave.calcDescriptor(des);
                    ((MyMat)toSave).release();
                    sqlDb.saveDescriptor_Keypoints(strings[0],toSave);
                    img = loader.loadImage(strings[1]);
                } catch (DBError dbError) {
                    LogFile.getInstance().e(dbError.getStackTrace().toString());
                    LogFile.getInstance().flushLog();
                }
            }

            return img;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = ProgressDialog.show(getParent(),"Saving","Please Wait",false);
        }

        @Override
        protected void onPostExecute(ImageContainer imageContainer) {
            super.onPostExecute(imageContainer);
            currImage = imageContainer;
            frame.setBackground(new BitmapDrawable(getResources(),currImage.render(false)));
            pd.dismiss();
        }
    }

}