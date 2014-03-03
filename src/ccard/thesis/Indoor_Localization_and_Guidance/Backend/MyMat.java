package ccard.thesis.Indoor_Localization_and_Guidance.Backend;

import android.view.View;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.KeyPoint;

import java.util.ArrayList;

/**
 * Created by Chris Card on 3/3/14.
 * This class allows contains info for images that can be drawn
 */
public class MyMat extends Mat implements ImageContainer {

    @Override
    public KeyPoint getKeyPoint(int index) {
        return null;
    }

    @Override
    public ArrayList<KeyPoint> getKeyPoints() {
        return null;
    }

    @Override
    public Mat getDescriptor() {
        return null;
    }

    @Override
    public boolean hasImageToDraw() {
        return false;
    }

    @Override
    public boolean render(View view, boolean withKeyPoints) {
        return false;
    }

    @Override
    public boolean renderComparision(View view, ImageContainer im2, ArrayList<DMatch> matches) {
        return false;
    }

    @Override
    public boolean calcDescriptor(Descriptor des) {
        return false;
    }
}
