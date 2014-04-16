package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Pair;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageContainer;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageProvidor;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.Matcher;
import ccard.thesis.Indoor_Localization_and_Guidance.JNI_Interface.LSH_Wrapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorMatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Ch on 3/5/14.
 */
public class LSHMatcher implements Matcher {

//    private DescriptorMatcher matcher;
    private LSH_Wrapper matcher;
    private Context context;
    private boolean hasParams;
    //This stores the location of the image in the training list
    //to the index in the database
    Map<Integer,Integer> trainingKeyToDbKey;

    public LSHMatcher(Context context){
//        matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        /*try {
            File i = File.createTempFile("test",".xml",context.getCacheDir());
            matcher.write(i.getPath());
            BufferedReader r = new BufferedReader(new FileReader(i));
            StringBuilder s = new StringBuilder();
            String v = "";
            while((v = r.readLine()) != null){
                s.append(v);
            }
            new AlertDialog.Builder(context).setMessage(s.toString()).create().show();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        this.context = context;
    }

    @Override
    public boolean train(ImageProvidor db) {
        if (!hasParams) return false;
        matcher.addImages(getDescriptors(db));
        matcher.trainMatcher();
        return true;
    }

    @Override
    public ArrayList<ArrayList<DMatch>> match(JSONObject params, ImageContainer query) {
        if(!params.has("Type")) return null;
        ArrayList<ArrayList<DMatch>> matches = new ArrayList<ArrayList<DMatch>>();
        try {
            if((MatchingType)params.get("Type") == MatchingType.LSH){
                int k = 5;
                ArrayList<Mat> masks = null;
                boolean compactres = false;

                k = params.getInt("k");
                compactres = params.getBoolean("compactResults");

                matches = matcher.match(query.getDescriptor(),k,compactres);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return (!matches.isEmpty() ? matches : null);
    }

    @Override
    public int verify(ArrayList<ArrayList<DMatch>> matches, ImageProvidor db,
                      ImageContainer query, double distanceThreshold, int inlierThreshold, int matchThreshold) {
        Map<Integer,ArrayList<MyDMatch>> images = filter(buildMatchMap(matches,db,query),20,0.6);
        Map<Integer,Pair<Mat,List<Integer>>> homographies = buildHomography(images,distanceThreshold);

        Map<Integer,Integer> imageInliers = new HashMap<Integer, Integer>();
        int best_inlierCount = 0;
        int second_best = 0;
        for(Integer i : homographies.keySet()){
            Pair<Mat,List<Integer>> HandIn = homographies.get(i);
            int sum = 0;
            for(Integer inlier : HandIn.second){
                sum += inlier;
            }

            if (best_inlierCount < sum) {
                second_best = best_inlierCount;
                best_inlierCount = sum;
            } else if (second_best < sum) {
                second_best = sum;
            }

            imageInliers.put(i,sum);
        }

        Set<Integer> bestImages = new HashSet<Integer>();
        for(Integer index : imageInliers.keySet()){
            if (imageInliers.get(index) >= second_best){
                bestImages.add(index);
            }
        }

        Map<Integer,ArrayList<MyDMatch>> matches2 = bruteForcesMatch(bestImages,db,query);

        Map<Integer,Pair<Mat,List<Integer>>> fundamentals = buildFundamental(matches2,distanceThreshold,0.99);

        int best = 0;
        int img = -1;
        for(Integer i : fundamentals.keySet()){
            Pair<Mat,List<Integer>> HandIn = fundamentals.get(i);
            int sum = 0;
            for(Integer inlier : HandIn.second){
                sum += inlier;
            }

            if (best < sum){
                best = sum;
                img = i;
            }
        }

        return (best >= inlierThreshold ? img : -1);
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
     * This method uses a hamming brute forces matcher to match the query image to each image
     * represented by the imageindex
     * @param imageIndex the indecies of the images to rematch
     * @param db the database of images
     * @param query the query image
     * @return The map of image indecies to their matches
     */
    private Map<Integer,ArrayList<MyDMatch>> bruteForcesMatch(Set<Integer> imageIndex, ImageProvidor db,
                                                              ImageContainer query){
        Map<Integer,ArrayList<MyDMatch>> matches = new HashMap<Integer, ArrayList<MyDMatch>>();
        DescriptorMatcher brute = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);

        for (Integer i : imageIndex){
            MatOfDMatch tempm = new MatOfDMatch();
            brute.match(query.getDescriptor(),db.getImage(i).getDescriptor(),tempm);
            List<DMatch> tempdm = tempm.toList();
            ArrayList<MyDMatch> myDMatches =  new ArrayList<MyDMatch>();
            for(DMatch d : tempdm){
                MyDMatch dm = new MyDMatch(d,i,
                        db.getImage(i).getKeyPoint(d.trainIdx),
                        query.getKeyPoint(d.queryIdx));
                myDMatches.add(dm);
            }

            matches.put(i,myDMatches);
        }

        return matches;
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
     * This method filters the image matches based on minMatchingBins and percentBetter
     * the percentBetter is used when there is more then one training keypoint associated with a
     * query keypoint the closest keypoint must be percentBetter then the second closest keypoint
     * if it is it is kept other wise all training keypoints associated with that query keypoint are discareded
     * @param images The map of image indexs to their match objectgs
     * @param minMatchingBins The fewest matches to the query image that the training images have
     * @param percentBetter the percentage that the closest has to be better than the second closest
     * @return the filter map of images
     */
    private Map<Integer,ArrayList<MyDMatch>> filter(Map<Integer,ArrayList<MyDMatch>> images,
                                                    int minMatchingBins, double percentBetter){
        Map<Integer,ArrayList<MyDMatch>> filtered = new HashMap<Integer, ArrayList<MyDMatch>>();
        Map<Integer,ArrayList<MyDMatch>> keyPointMap = new HashMap<Integer, ArrayList<MyDMatch>>();
        ArrayList<MyDMatch> matches = new ArrayList<MyDMatch>();

        for(Integer index : images.keySet()){
            if (images.get(index).size() >= minMatchingBins){
                keyPointMap.clear();
                matches.clear();
                for(MyDMatch dm : images.get(index)){
                    if (!keyPointMap.containsKey(dm.getQueryIdx())){
                        ArrayList<MyDMatch> temp = new ArrayList<MyDMatch>();
                        temp.add(dm);
                        keyPointMap.put(dm.getQueryIdx(),temp);
                    } else {
                        keyPointMap.get(dm.getQueryIdx()).add(dm);
                    }
                }

                for(Integer i : keyPointMap.keySet()){
                    if (keyPointMap.get(i).size() > 1){
                        Collections.sort(keyPointMap.get(i));
                        double closest = keyPointMap.get(i).get(0).getDistance();
                        double secondClosest = keyPointMap.get(i).get(1).getDistance();

                        if ((closest/secondClosest) <=  percentBetter){
                            matches.add(keyPointMap.get(i).get(0));
                        }
                    } else {
                        matches.add(keyPointMap.get(i).get(0));
                    }
                }

                if (matches.size() >= minMatchingBins){
                    filtered.put(index,matches);
                }
            }
        }
        return filtered;
    }


    /**
     * This method builds a map from the image index to the image it self
     * @param matches The matches between the images
     * @param db The database of images
     * @param query the query image
     * @return a map of image index to
     */
    private Map<Integer,ArrayList<MyDMatch>> buildMatchMap(ArrayList<ArrayList<DMatch>> matches, ImageProvidor db, ImageContainer query){
        Map<Integer,ArrayList<MyDMatch>> imgMap = new HashMap<Integer, ArrayList<MyDMatch>>();

        for(ArrayList<DMatch> dm : matches){
            for(DMatch match : dm){
                int dbIndex = trainingKeyToDbKey.get(match.imgIdx);
                MyDMatch tempDM = new MyDMatch(match,dbIndex,
                        db.getImage(dbIndex).getKeyPoint(match.trainIdx),
                        query.getKeyPoint(match.queryIdx));
                if (!imgMap.containsKey(dbIndex)){
                    ArrayList<MyDMatch> templ = new ArrayList<MyDMatch>();
                    templ.add(tempDM);
                    imgMap.put(dbIndex,templ);
                } else {
                    imgMap.get(dbIndex).add(tempDM);
                }
            }
        }
        return imgMap;
    }

    @Override
    public void setTrainingParams(JSONObject params) {
        int num_tables = -1;
        int key_size = -1;
        int probe_level = -1;
        try {
           num_tables = params.getInt("table_number");
           key_size = params.getInt("key_size");
           probe_level = params.getInt("multi_probe_level");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (num_tables < 0 || key_size < 0 || probe_level < 0){
            num_tables = 30;
            key_size = 20;
            probe_level = 2;
        }

        matcher = new LSH_Wrapper(num_tables,key_size,probe_level);
        hasParams = true;
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
