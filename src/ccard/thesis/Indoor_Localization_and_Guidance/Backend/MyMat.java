package ccard.thesis.Indoor_Localization_and_Guidance.Backend;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.KeyPoint;

import java.util.ArrayList;

/**
 * Created by Chris Card on 3/3/14.
 * This class allows contains info for images that can be drawn
 */
public class MyMat extends Mat implements ImageContainer {

    private Mat descriptor;
    private ArrayList<KeyPoint> keyPoints;

    public MyMat(){

    }
    public MyMat(Bitmap img){
        Utils.bitmapToMat(img,this);
    }

    @Override
    public KeyPoint getKeyPoint(int index) {
        return ((index >= 0) && (index < keyPoints.size()) ? keyPoints.get(index) :
        null);
    }

    @Override
    public ArrayList<KeyPoint> getKeyPoints() {
        return keyPoints;
    }

    @Override
    public Mat getDescriptor() {
        return descriptor;
    }

    @Override
    public boolean hasImageToDraw() {
        return !this.empty();
    }

    @Override
    public boolean render(final View view, boolean withKeyPoints) {

        Mat drawMat = new Mat();
        this.copyTo(drawMat);

        if(withKeyPoints){
            return false;
        }

        Bitmap img = Bitmap.createBitmap(drawMat.cols(),drawMat.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(drawMat,img);
        drawMat.release();
        final Bitmap img_draw = img;
        img.recycle();


        view.post(new Runnable() {
            @Override
            public void run() {
                ((ImageView) view).setImageBitmap(img_draw);
            }
        });

        return true;
    }

    @Override
    public boolean renderComparision(final View view, ImageContainer im2, ArrayList<DMatch> matches) {
        Mat drawMat = new Mat();
        this.copyTo(drawMat);

        Bitmap img = Bitmap.createBitmap(drawMat.cols(),drawMat.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(drawMat,img);
        drawMat.release();
        final Bitmap img_draw = img;
        img.recycle();

        view.post(new Runnable() {
            @Override
            public void run() {
                ((ImageView) view).setImageBitmap(img_draw);
            }
        });
        return false;
    }

    @Override
    public boolean calcDescriptor(Descriptor des) {
        return false;
    }
}
