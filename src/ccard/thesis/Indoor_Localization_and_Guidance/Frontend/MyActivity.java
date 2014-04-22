package ccard.thesis.Indoor_Localization_and_Guidance.Frontend;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes.LogFile;
import ccard.thesis.Indoor_Localization_and_Guidance.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.*;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;

import java.io.*;

public class MyActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private static final String TAG = "Drive fail";
    private static final int REQUEST_CODE_UPLOAD = 1;
    private static final int REQUEST_CODE_RESOLUTION = 2;
    private GoogleApiClient apiClient;
    private BaseLoaderCallback mLoader = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);

        }
    };
    public static final String PREFERENCES = "SETTINGS";
    static{
        if(!OpenCVLoader.initDebug()){
        //TODO: error reporting
        }
    }
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5,this,mLoader);
        LogFile.createLog(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(apiClient == null){
            apiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        if (!apiClient.isConnected()){
            apiClient.connect();
        }
    }

    @Override
    protected void onPause(){
        if (apiClient != null && apiClient.isConnected()){
            apiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inf = getMenuInflater();
        inf.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.settings:
                Intent i = new Intent(this,SettingsDialog.class);
                startActivity(i);
                return true;
            case R.id.upload:
                LogFile.getInstance().l("test");
                LogFile.getInstance().flushLog();
                int t = 0;
                while (t <20) t++;

                uploadFile();
                return true;
        }
        return true;
    }

    public void guide(View view){
        Intent i = new Intent(this,Guidance.class);
        startActivity(i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
         Log.i(TAG, "API client failed: "+connectionResult.toString());
        if (!connectionResult.hasResolution()){
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(),this,0).show();
            return;
        }

        try {
            connectionResult.startResolutionForResult(this,REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while  starting resolution", e);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG,"API Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void showMessage(String message){
        Toast.makeText(this,message,5000).show();
    }
    private void uploadFile(){
        ProgressDialog pd = ProgressDialog.show(this,"Uploading","Please wait",false);

        Drive.DriveApi.newContents(apiClient)
                .setResultCallback(contentsResultResultCallback);

        pd.dismiss();
    }

    protected ProgressDialog progress(ProgressDialog pd, boolean start_stop){
        if (!start_stop){
            pd.dismiss();
            return null;
        } else {
            pd = ProgressDialog.show(this,"Uploading","PleaseWait");
            return pd;
        }
    }

    ResultCallback<DriveApi.ContentsResult> contentsResultResultCallback = new ResultCallback<DriveApi.ContentsResult>() {
        @Override
        public void onResult(DriveApi.ContentsResult contentsResult) {
            if (!contentsResult.getStatus().isSuccess()){
                showMessage("Error while trying to create new file contents");
                return;
            }

            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(LogFile.file_name)
                    .setMimeType("text/plain")
                    .setStarred(true).build();
            Drive.DriveApi.getRootFolder(apiClient)
                    .createFile(apiClient,changeSet,contentsResult.getContents())
                    .setResultCallback(driveIdResultResultCallback);

        }
    };

    ResultCallback<DriveFolder.DriveFileResult> driveIdResultResultCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(DriveFolder.DriveFileResult driveIdResult) {
            if (!driveIdResult.getStatus().isSuccess()){
                showMessage("Failed to create file");
                return;
            }
            new UploaderService().execute(driveIdResult);
        }
    };

    private class UploaderService extends AsyncTask<DriveFolder.DriveFileResult,String,Void>{
        private ProgressDialog pd;

        @Override
        protected Void doInBackground(DriveFolder.DriveFileResult... params) {
            DriveFolder.DriveFileResult fileResult = params[0];
            try {
                DriveApi.ContentsResult contentsResult = fileResult.getDriveFile()
                        .openContents(apiClient, DriveFile.MODE_WRITE_ONLY, null)
                        .await();
                if (!contentsResult.getStatus().isSuccess()){
                    publishProgress("Failed to open file");
                    return null;
                }
                OutputStream outputStream  = contentsResult.getContents().getOutputStream();
                BufferedReader appends = new BufferedReader(new FileReader(
                        new File(LogFile.getInstance().getLog())));
                String line = "";
                while((line = appends.readLine()) != null){
                    outputStream.write(line.getBytes());
                }
                appends.close();

                com.google.android.gms.common.api.Status status = fileResult.getDriveFile()
                        .commitAndCloseContents(apiClient, contentsResult.getContents())
                        .await();
                if (status.isSuccess()){
                    publishProgress("Uploaded");
                } else {
                    publishProgress("Failed to Upload");
                }
            } catch (FileNotFoundException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... vals){
            super.onProgressUpdate(vals);

            showMessage(vals[0]);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = progress(pd,true);
        }

        @Override
        protected void onPostExecute(Void result){
            super.onPostExecute(result);
            progress(pd,false);
        }
    }
}
