package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.Descriptor;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageContainer;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

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

        if (!hasImageToDraw()) return false;
        Mat drawMat = new Mat();
        this.copyTo(drawMat);

        if(withKeyPoints){
            Features2d.drawKeypoints(this, new MatOfKeyPoint(keyPoints.toArray(new KeyPoint[0])), drawMat,
                    Scalar.all(-1), Features2d.DRAW_RICH_KEYPOINTS);
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
    public boolean renderComparision(final View view, ImageContainer im2, ArrayList<MyDMatch> matches) {
        if (!hasImageToDraw() || !im2.hasImageToDraw()) return false;
        Mat drawMat = new Mat();

        List<DMatch> dMatches = new ArrayList<DMatch>();
        for(MyDMatch dm : matches){
            dMatches.add(dm.toDMatch());
        }

        MatOfKeyPoint thisImg = new MatOfKeyPoint(keyPoints.toArray(new KeyPoint[0]));
        MatOfKeyPoint trainImg = new MatOfKeyPoint(im2.getKeyPoints().toArray(new KeyPoint[0]));
        Features2d.drawMatches(this, thisImg, (MyMat) im2, trainImg, new MatOfDMatch(dMatches.toArray(new DMatch[0])), drawMat,
                Scalar.all(-1), Scalar.all(-1), new MatOfByte(),Features2d.NOT_DRAW_SINGLE_POINTS);



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
    public boolean calcDescriptor(Descriptor des) {
        boolean worked = des.calculateDescriptor(this);

        if (!worked) return false;

        descriptor = des.getDescriptor();
        keyPoints = des.getKeyPoints();

        return true;
    }
}
