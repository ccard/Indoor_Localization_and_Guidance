package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.ResultReceiver;

/**
 * Created by Ch on 3/3/14.
 * This method extends ReslustReceiver so that more effective communication can
 * occur between the DataBaseConnection service and the calling application
 */
public class ResReceiver extends ResultReceiver {
    public ResReceiver(Handler handler) {
        super(handler);
    }

    @Override
    public void send(int resultCode, Bundle resultData) {
        super.send(resultCode, resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
    }
}
