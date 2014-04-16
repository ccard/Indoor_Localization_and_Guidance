package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import android.content.Context;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.Descriptor;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageContainer;
import org.json.JSONObject;
import org.opencv.core.*;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    public boolean initDescriptor(JSONObject params,Context context) {
        detect = FeatureDetector.create(FeatureDetector.ORB);
        String xml = XMLparser.build_XML(params, XMLparser.DESCRIPTORS.ORB);
        File outFile = XMLparser.createXMLFile("ORBParams",xml,context);
        detect.read(outFile.getPath());
        return true;
    }

    @Override
    public boolean calculateDescriptor(ImageContainer image) {
        if (!image.hasImageToDraw() && !image.getClass().isInstance(Mat.class)) return false;
        List<MatOfKeyPoint> keyPoint = new ArrayList<MatOfKeyPoint>();
        List<Mat> images = new ArrayList<Mat>();
        Mat gray = new Mat();
        Imgproc.cvtColor((MyMat)image, gray, Imgproc.COLOR_BGR2GRAY);
        images.add(gray);
        List<Mat> desc = new ArrayList<Mat>();
        List<Mat> masks = new ArrayList<Mat>();

        //masks.add(mask);
        try{
            detect.detect(images,keyPoint,masks);
            des.compute(images,keyPoint,desc);
            keyPoints = new ArrayList<KeyPoint>(keyPoint.get(0).toList());
            descript = desc.get(0);
        } catch (Exception e) {
            gray.release();
            images.clear();
            keyPoint.clear();
            desc.clear();
            return false;
        }

        gray.release();
        images.clear();
        keyPoint.clear();
        desc.clear();
        return true;
    }
}
