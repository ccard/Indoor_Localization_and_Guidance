package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import android.content.Context;
import android.util.Pair;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageContainer;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageProvidor;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.Matcher;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorMatcher;

import java.util.*;

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
    public int verify(ArrayList<ArrayList<DMatch>> matches, ImageProvidor db, ImageContainer query,
                      double distanceThreshold, double inlierThreshold) {

        Map<Integer,ArrayList<MyDMatch>> image_matches = new HashMap<Integer, ArrayList<MyDMatch>>();

        for(ArrayList<DMatch> dMatches : matches){

            for(DMatch dMatch : dMatches){
                int img_idx = trainingKeyToDbKey.get(dMatch.imgIdx);
                MyDMatch temp_dm = new MyDMatch(dMatch,img_idx,
                        db.getImage(img_idx).getKeyPoint(dMatch.trainIdx),
                        query.getKeyPoint(dMatch.queryIdx));

                if (image_matches.containsKey(img_idx)){
                    image_matches.get(img_idx).add(temp_dm);
                } else {
                    ArrayList<MyDMatch> temp = new ArrayList<MyDMatch>();
                    temp.add(temp_dm);
                    image_matches.put(img_idx,temp);
                }
            }
        }

        Set<Integer> to_del = new HashSet<Integer>();

        for(Integer i : image_matches.keySet()){
            if (image_matches.get(i).size() < inlierThreshold){
                to_del.add(i);
            }
        }

        for(Integer del : to_del){
            image_matches.remove(del);
        }

        Map<Integer,Pair<Mat,List<Integer>>> fundamentals = buildFundamental(image_matches,distanceThreshold,0.99);

        int best_match = 0, image = -1;
        for(Integer fun : fundamentals.keySet()){
            List<Integer> inliers = fundamentals.get(fun).second;
            int sum = 0;
            for(Integer s : inliers) sum += s;

            if (best_match < sum){
                best_match = sum;
                image = (best_match >= inlierThreshold ? fun : -1);
            }
        }

        return image;
    }

    @Override
    public void setTrainingParams(JSONObject params) {

    }

    /**
     * This methods finds the fundamental matrix for each of the images
     * @param images the images to find the fundimentals for
     * @param distThreshold the max distance from the epipolar lines
     * @param confidence the amount of confidence in the measure
     * @return map of indecies to a pair of fundamental and inliers list
     */
    private Map<Integer,Pair<Mat,List<Integer>>> buildFundamental(Map<Integer,ArrayList<MyDMatch>> images,
                                                                  double distThreshold, double confidence){
        Map<Integer,Pair<Mat,List<Integer>>> fundamentals = new HashMap<Integer, Pair<Mat, List<Integer>>>();

        for(Integer index : images.keySet()){
            Pair<MatOfPoint2f,MatOfPoint2f> train_scene = buildTrainScene(images.get(index));
            Mat inliers = new Mat();
            Mat H = Calib3d.findFundamentalMat(train_scene.first, train_scene.second,
                    Calib3d.FM_RANSAC, distThreshold, confidence,inliers);
            List<Integer> liers = new ArrayList<Integer>();
            int size = (int)inliers.total()*inliers.channels();
            byte[] buff = new byte[size];
            inliers.get(0,0,buff);

            for(int i = 0; i < size; i++){
                liers.add((buff[i] > 0) ? 1 : 0);
            }

            fundamentals.put(index,new Pair<Mat, List<Integer>>(H,liers));
        }
        return fundamentals;
    }

    /**
     * This method builds homographies for each of the images and also gets the inliers of the
     * images
     * @param images The images to make the homographies for
     * @param distThreshold The max projection error for points when RANSAC is calculating the homography
     * @return a map from the db image index to the homography and the inlier list
     */
    private Map<Integer,Pair<Mat,List<Integer>>> buildHomography(Map<Integer,ArrayList<MyDMatch>> images,
                                                                 double distThreshold){
        Map<Integer,Pair<Mat,List<Integer>>> homographies = new HashMap<Integer, Pair<Mat, List<Integer>>>();

        for(Integer index : images.keySet()){
            Pair<MatOfPoint2f,MatOfPoint2f> train_scene = buildTrainScene(images.get(index));
            Mat inliers = new Mat();
            Mat H = Calib3d.findHomography(train_scene.first, train_scene.second,
                    Calib3d.FM_RANSAC, distThreshold,inliers);
            List<Integer> liers = new ArrayList<Integer>();
            int size = (int)inliers.total()*inliers.channels();
            byte[] buff = new byte[size];
            inliers.get(0,0,buff);

            for(int i = 0; i < size; i++){
                liers.add((buff[i] > 0) ? 1 : 0);
            }

            homographies.put(index,new Pair<Mat, List<Integer>>(H,liers));
        }
        return homographies;
    }

    /**
     * This method builds the train and scene matofpoint2f and returns them as a pair
     * @param matches the matches to build the matofpoint2f off of
     * @return The pair of matofpoint2f where the first object of the pair is the training
     * points and the second is the scene points
     */
    private Pair<MatOfPoint2f,MatOfPoint2f> buildTrainScene(ArrayList<MyDMatch> matches){
        List<Point> train = new ArrayList<Point>();
        List<Point> scene = new ArrayList<Point>();

        for(MyDMatch dm : matches){
            train.add(dm.getTrainkp().pt);
            scene.add(dm.getQuerykp().pt);
        }

        MatOfPoint2f trainMat = new MatOfPoint2f(train.toArray(new Point[0]));
        MatOfPoint2f sceneMat = new MatOfPoint2f(scene.toArray(new Point[0]));

        return new Pair<MatOfPoint2f, MatOfPoint2f>(trainMat,sceneMat);
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
