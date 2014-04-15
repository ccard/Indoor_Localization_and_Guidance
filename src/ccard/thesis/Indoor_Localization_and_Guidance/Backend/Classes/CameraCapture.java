package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageCapture;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * Created by Chris Card on 3/3/14.
 * This class implements imagecapture and uses the devices camera
 */
public class CameraCapture implements ImageCapture {

    private Context context;
    private VideoCapture camera;
    private Size cSize;
    private DisplayMetrics dis;

    public CameraCapture(Context cont,Size cSize){
        context = cont;
        this.cSize = cSize;
        dis = context.getResources().getDisplayMetrics();
    }


    @Override
    public boolean open(){
        if(camera == null || !camera.isOpened()){
           camera = new VideoCapture((context.getPackageManager()
                   .hasSystemFeature(PackageManager.FEATURE_CAMERA) ? 1 : 0));

           if (cSize.width < 0 && cSize.height < 0){
               List<Size> sizes = camera.getSupportedPreviewSizes();
               Size largest = sizes.get(sizes.size()-1);

               cSize = largest;
           }
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
        int loop_count = 0;
        do {
            grabbed = camera.grab();
            loop_count++;
        } while(!grabbed && loop_count < 20);

        if (!grabbed && loop_count >= 20){
            return null;
        }
        camera.retrieve(ret,Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGB);
        Core.transpose(ret,ret);
        Core.flip(ret,ret,0);
        return ret;
    }
}
