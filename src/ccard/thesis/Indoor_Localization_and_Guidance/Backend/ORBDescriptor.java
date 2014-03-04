package ccard.thesis.Indoor_Localization_and_Guidance.Backend;

import org.json.JSONObject;
import org.opencv.core.*;
import org.opencv.features2d.DescriptorExtractor;
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
    DescriptorExtractor des;

    Mat descript,mask;
    ArrayList<KeyPoint> keyPoints;

    public ORBDescriptor(){
        des = DescriptorExtractor.create(DescriptorExtractor.ORB);
        keyPoints = new ArrayList<KeyPoint>();
    }

    @Override
    public ArrayList<KeyPoint> getKeyPoints() {
        return keyPoints;
    }

    @Override
    public Mat getDescriptor() {
        return descript;
    }

    @Override
    public boolean makeMask(MaskTypes type, int... radius_side) {
        if(radius_side.length > 2 || radius_side.length == 0) return false;

        int radius_s,side2 = 0;
        if (radius_side.length == 1){
            radius_s = radius_side[0];
        } else {
            radius_s = radius_side[0];
            side2 = radius_side[1];
        }

        switch (type){
            case Circle:
                mask = Mat.zeros(2*radius_s,2*radius_s, CvType.CV_8U);
                Core.circle(mask,new Point(radius_s,radius_s),radius_s,
                        new Scalar(1,1,1),-1);
                break;
            case Square:
                mask = Mat.zeros(radius_s,side2,CvType.CV_8U);
                Core.rectangle(mask,new Point(0,0),new Point(radius_s,side2),
                        new Scalar(1,1,1),-1);
                break;
        }
        return true;
    }

    @Override
    public boolean initDescriptor(JSONObject params) {
        detect = FeatureDetector.create(FeatureDetector.ORB);

        return false;
    }

    @Override
    public boolean calculateDescriptor() {
        return false;
    }
}
