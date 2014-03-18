package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import org.opencv.features2d.DMatch;
import org.opencv.features2d.KeyPoint;

/**
 * Created by Ch on 3/18/14.
 * This is my own more robust version of opencvs DMatch object
 */
public class MyDMatch  implements Comparable<MyDMatch>{
    private int trainIdx,imgIdx,queryIdx;
    private double distance;
    private KeyPoint trainkp, querykp;

    public MyDMatch(DMatch m, int dbIndex, KeyPoint train, KeyPoint q){
        trainIdx = m.trainIdx;
        imgIdx = dbIndex;
        queryIdx = m.queryIdx;
        distance = m.distance;
        trainkp = train;
        querykp = q;
    }

    public int getTrainIdx() {
        return trainIdx;
    }

    public int getImgIdx() {
        return imgIdx;
    }

    public int getQueryIdx() {
        return queryIdx;
    }

    public double getDistance() {
        return distance;
    }

    public KeyPoint getTrainkp() {
        return trainkp;
    }

    public KeyPoint getQuerykp() {
        return querykp;
    }

    @Override
    public int compareTo(MyDMatch other) {
        int comparison1 = (distance < other.getDistance() ? -1 : 0);
        int comparison2 = (distance > other.getDistance() ? 1 : 0);

        if (comparison1 == comparison2){
            return 0;
        } else if (comparison1 != 0){
            return comparison1;
        } else {
            return comparison2;
        }
    }

    /**
     * This method converts MyDMatch to dmatch
     * @return a dmatch object
     */
    public DMatch toDMatch(){
        return new DMatch(queryIdx,trainIdx,(float)distance);
    }

}
