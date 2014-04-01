package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import android.content.Context;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageContainer;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageProvidor;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.Matcher;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorMatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ch on 4/1/14.
 * This class represents a brute force matcher
 */
public class BFMatcher implements Matcher {

    private Context context;
    private DescriptorMatcher matcher;
    Map<Integer,Integer> trainingKeyToDbKey;

    public BFMatcher(Context context){
        this.context = context;
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
    }

    @Override
    public boolean train(ImageProvidor db) {
        matcher.add(getDescriptors(db));
        return true;
    }

    @Override
    public ArrayList<ArrayList<DMatch>> match(JSONObject params, ImageContainer query) {

        List<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();

        try {
            if((MatchingType)params.get("Type") == MatchingType.BruteForce){
                int k = 5;
                ArrayList<Mat> masks = null;
                boolean compactres = false;

                k = params.getInt("k");
                compactres = params.getBoolean("compactResults");

                matcher.knnMatch(query.getDescriptor(), matches, k);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<ArrayList<DMatch>> match = new ArrayList<ArrayList<DMatch>>();

        for(MatOfDMatch dMatch : matches){
            match.add(new ArrayList<DMatch>(dMatch.toList()));
        }

        return (!match.isEmpty() ? match : null);
    }

    @Override
    public int verify(ArrayList<ArrayList<DMatch>> matches, ImageProvidor db, ImageContainer query, double distanceThreshold, double inlierThreshold) {

        Map<Integer,ArrayList<MyDMatch>> image_matches = new HashMap<Integer, ArrayList<MyDMatch>>();

        for(ArrayList<DMatch> dMatches : matches){

            for(DMatch dMatch : dMatches){
                int img_idx = trainingKeyToDbKey.get(dMatch.imgIdx);
                MyDMatch temp_dm = new MyDMatch(dMatch,img_idx,
                        db.getImage(img_idx).getKeyPoint(dMatch.trainIdx),
                        db.getImage(img_idx).getKeyPoint(dMatch.queryIdx));

                if (image_matches.containsKey(img_idx)){

                }
            }
        }

        return 0;
    }

    @Override
    public void setTrainingParams(JSONObject params) {

    }

    /**
     * This method gets the descriptors from the images
     * @param db the database of images
     * @return the list of descriptors
     */
    private List<Mat> getDescriptors(ImageProvidor db){
        List<Mat> ret = new ArrayList<Mat>();
        trainingKeyToDbKey = new HashMap<Integer, Integer>();
        int counter = 0;
        for(Integer im : db.getImages().keySet()){
            ret.add(db.getImage(im).getDescriptor());
            trainingKeyToDbKey.put(counter,im);
            counter++;
        }
        return ret;
    }
}
