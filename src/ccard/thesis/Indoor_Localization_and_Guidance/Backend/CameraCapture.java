package ccard.thesis.Indoor_Localization_and_Guidance.Backend;

import android.content.Context;
import android.hardware.Camera;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.NativeCameraView;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

/**
 * Created by Chris Card on 3/3/14.
 * This class implements imagecapture and uses the devices camera
 */
public class CameraCapture implements ImageCapture{

    private Context context;
    private VideoCapture camera;
    private Size cSize;

    public CameraCapture(Context cont,Size cSize){
        context = cont;
        this.cSize = cSize;
    }


    @Override
    public boolean open(){
        if(camera == null || camera.isOpened()){
           camera = new VideoCapture(0);
           camera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH,cSize.width);
           camera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT,cSize.height);
        }
        return camera.isOpened();
    }

    @Override
    public boolean close() {
        camera.release();
        camera = null;
        return true;
    }

    @Override
    public MyMat capture() {
        if(!camera.isOpened()) return null;
        MyMat ret = new MyMat();
        boolean grabbed;
        do {
            grabbed = camera.grab();
        } while(!grabbed);

        camera.retrieve(ret,Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGB);
        return ret;
    }
}
