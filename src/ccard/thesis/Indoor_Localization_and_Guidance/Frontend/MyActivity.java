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
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
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
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
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
                if (!apiClient.isConnected()){
                    apiClient.connect();
                }
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
    private void uploadFile(){
        ProgressDialog pd = ProgressDialog.show(this,"Uploading","Please wait",false);
        final ResultCallback<DriveApi.DriveIdResult> driveIdResultResultCallback = new ResultCallback<DriveApi.DriveIdResult>() {
            @Override
            public void onResult(DriveApi.DriveIdResult driveIdResult) {
                if (!driveIdResult.getStatus().isSuccess()){
                    Toast.makeText(getParent(),"Failed to open file",5000);
                    return;
                }
                DriveFile file = Drive.DriveApi.getFile(apiClient,driveIdResult.getDriveId());
                try {
                    DriveApi.ContentsResult contentsResult = file.openContents(apiClient,DriveFile.MODE_WRITE_ONLY,null)
                            .await();
                    if (!contentsResult.getStatus().isSuccess()){
                        Toast.makeText(getParent(),"Failed to open file",5000);
                        return;
                    }
                    OutputStream outputStream  = contentsResult.getContents().getOutputStream();
                    BufferedReader appends = new BufferedReader(new FileReader(
                            new File(LogFile.getInstance().getLog())));
                    String line = "";
                    while((line = appends.readLine()) != null){
                        outputStream.write(line.getBytes());
                    }
                    appends.close();

                    Status status = file.commitAndCloseContents(apiClient,contentsResult.getContents()).await();
                    if (status.isSuccess()){
                        Toast.makeText(getParent(),"Uploaded",4000);
                    } else {
                        Toast.makeText(getParent(),"Failed to Upload",4000);
                    }
                } catch (FileNotFoundException e){
                    e.printStackTrace();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        };

        Drive.DriveApi.fetchDriveId(apiClient,Drive.DriveApi.getRootFolder(apiClient)
                .getDriveId().encodeToString())
                .setResultCallback(driveIdResultResultCallback);
        pd.dismiss();
    }


}
