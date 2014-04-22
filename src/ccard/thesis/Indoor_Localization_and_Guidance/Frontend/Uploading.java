package ccard.thesis.Indoor_Localization_and_Guidance.Frontend;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes.LogFile;
import ccard.thesis.Indoor_Localization_and_Guidance.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.*;

import java.io.*;

/**
 * Created by Ch on 4/22/14.
 */
public class Uploading extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "Drive fail";
    private static final int REQUEST_CODE_UPLOAD = 1;
    private static final int REQUEST_CODE_RESOLUTION = 2;
    private GoogleApiClient apiClient;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_dialog);
    } 
    @Override
    public void onResume(){
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
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "API client failed: " + connectionResult.toString());
        if (!connectionResult.hasResolution()){
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
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
        uploadFile();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void showMessage(String message){
        Toast.makeText(this, message, 5000).show();
    }
    private void uploadFile(){

        Drive.DriveApi.newContents(apiClient)
                .setResultCallback(contentsResultResultCallback);

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

    private class UploaderService extends AsyncTask<DriveFolder.DriveFileResult,String,Void> {
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
        protected void onPostExecute(Void result){
            super.onPostExecute(result);
            finish();
        }
    }


}