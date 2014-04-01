package ccard.thesis.Indoor_Localization_and_Guidance.JNI_Interface;


import org.opencv.core.Mat;
import org.opencv.features2d.DMatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Ch on 3/27/14.
 */
public class LSH_Wrapper {

    public LSH_Wrapper(int table_num,int key_size,int multi_probe_level){
        init(table_num,key_size,multi_probe_level);
    }

    /**
     * Adds images to the matcher
     * @param db the images to add
     */
    public void addImages(List<Mat> db){
        for(Mat im : db){
            add(im.getNativeObjAddr());
        }
    }

    /**
     * Trains the matcher
     */
    public void trainMatcher(){
        train();
    }

    /**
     * performs a kNN matching using lsh on the trained set of images
     * @param query the image to match to the database
     * @param k the number of nearest neighbors to return
     * @param compactResults  removes duplicate matches
     * @return a map from image database number
     */
    public ArrayList<ArrayList<DMatch>> match(Mat query, int k, boolean compactResults){
        String matches[] = knnMatch(query.getNativeObjAddr(),k,compactResults);
        return null;
    }

    /**
     * This method inits the lsh matcher
     * @param table_num number of tables
     * @param key_size the size of the key
     * @param multi_probe_level the prob level for matching
     */
    private native void init(int table_num,int key_size,int multi_probe_level);

    /**
     * This method trains the matcher
     */
    private native void train();

    /**
     * This method adds images to the database of images that the mathcer trains
     * against
     * @param img the list of images to add
     */
    private native void add(long img);

    /**
     * This method gets and returns the mathces to the database that the query image has
     * @param query the image to query the database with
     * @param k the number of nearest neighbors
     * @param compactResults should the results be compacted
     * @return a list of the list of dmatches
     */
    private native String[] knnMatch(long query, int k, boolean compactResults);

    static {
        System.loadLibrary("LSH_Matcher");
    }

}
