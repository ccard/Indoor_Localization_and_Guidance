package ccard.thesis.Indoor_Localization_and_Guidance.Frontend;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes.LogFile;
import ccard.thesis.Indoor_Localization_and_Guidance.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;

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
        inf.inflate(R.menu.settings,menu);
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
}
