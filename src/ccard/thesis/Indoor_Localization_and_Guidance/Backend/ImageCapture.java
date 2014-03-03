package ccard.thesis.Indoor_Localization_and_Guidance.Backend;

import android.graphics.Bitmap;

/**
 * Created by Chris Card on 3/3/14.
 * This class is an interface to capture images from a device
 */
public interface ImageCapture {

    /**
     * This method opens the image capture device
     * @return true if succeeded false other wise
     */
    public boolean open();

    /**
     * closes the connection to the image capture device
     * @return true if succeded
     */
    public boolean close();

    /**
     * This method captures a frame from the camera
     * @return a bitmap image
     */
    public MyMat capture();
}
