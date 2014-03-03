package ccard.thesis.Indoor_Localization_and_Guidance.Backend;

import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;

import java.util.ArrayList;

/**
 * Created by Chris Card on 3/3/14.
 * This Class overrides the descriptor interface for the orb
 * descriptor
 */
public class ORBDescriptor implements Descriptor {

    FeatureDetector detect;

    public ORBDescriptor(){
        detect = FeatureDetector.create(FeatureDetector.ORB);
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
    public boolean makeMask(MaskTypes type, int... radius_side) {
        return false;
    }

    @Override
    public boolean initDescriptor(JSONObject params) {
        return false;
    }

    @Override
    public boolean calculateDescriptor() {
        return false;
    }
}
