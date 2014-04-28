package ccard.thesis.Indoor_Localization_and_Guidance.Frontend;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.media.audiofx.AudioEffect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

    //TODO: fix memory leak problems and figure out why images isn't displaying
    private LocalSQLDb sqlDb;
    private LocalTestDB loader;
    private ProgressBar progressBar;
    private Descriptor orb;
    private ListIterator<String> files;
    private int num_files,progression;
    private ImageContainer currImage;
    protected FrameLayout frame;
    private EditText location;

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
        location = (EditText)findViewById(R.id.location_field);
        new Load_Save(this,sqlDb,loader,null,orb).execute(files.next());
    }

    @Override
    public void onPause(){
        try {
            sqlDb.closeConnection();
        } catch (DBError dbError) {
            LogFile.getInstance().e("Failed to close database");
            LogFile.getInstance().flushLog();
        }
        super.onPause();
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
        if (location.getText().length() == 0){
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("You must enter a location!")
                    .create();
            alertDialog.show();
        }
        else if (files.hasNext()){
            new Load_Save(this,sqlDb,loader,(MyMat)currImage,orb)
                    .execute(location.getText().toString(),files.next());
            progression++;
            progressBar.setProgress(progression);
            progressBar.invalidate();
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("All images are now loaded")
                    .create();
            alertDialog.show();
            sqlDb.close();
            finish();
        }
    }


    private class Load_Save extends AsyncTask<String,Void,ImageContainer>{

        private LocalSQLDb sqlDb;
        private LocalTestDB loader;
        private ProgressDialog pd;
        private MyMat toSave;
        private Descriptor des;
        private Context context;
        public Load_Save(Context context,LocalSQLDb db,LocalTestDB testDB,MyMat img,Descriptor des){
            sqlDb = db;
            this.context = context;
            loader = testDB;
            this.des = des;
            if (img != null){
                toSave = img;
            }
        }
        @Override
        protected ImageContainer doInBackground(String... strings) {
            ImageContainer img = new MyMat();
            if (strings.length == 1){
                String file = strings[0];
                try {
                    img = loader.loadImage(file,des);
                } catch (DBError dbError) {
                    LogFile.getInstance().e(dbError.getStackTrace().toString());
                    LogFile.getInstance().flushLog();
                }
            } else {
                try {
                    if (sqlDb.saveDescriptor_Keypoints(strings[0],toSave)){
                        LogFile.getInstance().l("Saved Image: "+strings[0]);
                    } else {
                        LogFile.getInstance().l("Failed to save file");
                    }
                    LogFile.getInstance().flushLog();
                    img = loader.loadImage(strings[1],des);
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
            pd = ProgressDialog.show(context,"Saving","Please Wait",false);
        }

        @Override
        protected void onPostExecute(ImageContainer imageContainer) {
            super.onPostExecute(imageContainer);
            currImage = imageContainer;
            pd.dismiss();
            frame.setBackground(new BitmapDrawable(getResources(), imageContainer.render(false)));
            ((MyMat) currImage).release();
        }
    }

}