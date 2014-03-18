package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces;

import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageContainer;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageProvidor;
import org.json.JSONObject;
import org.opencv.features2d.DMatch;

import java.util.ArrayList;

/**
 * Created by Ch on 3/3/14.
 * This interface is for the matcher objects to perform the matching of the
 * images
 */
public interface Matcher {

    public enum MatchingType{BruteForce,LSH};
    /**
     * This method trains the database matcher with the given params so
     * that matching can be performed against it
     * @param db the list of images that represents the database
     * @return true if the training succeeded
     */
    public boolean train(ImageProvidor db);

    /**
     * This method matches the query image to the database of images
     * @param params the params to use to match with must contain the type of
     *               Matching that you wish to use (MatchingType) Type as keyword
     *               and Matching type as the value
     * @param query the query image
     * @return a pair of the image index and the string representing its location
     */
    public ArrayList<ArrayList<DMatch>> match(JSONObject params, ImageContainer query);

    /**
     * This method verifies the Matches gotten from match method and returns the image that it
     * thinks is correct
     * @param matches List of matches between training images and query image
     * @param db Database of images
     * @param query The query image that was matched against the database
     * @param distanceThreshold The acceptable distance projection threshold
     * @param inlierThreshold The requiered number of inliers to be considered a good match
     * @return the index of the image
     */
    public int verify(ArrayList<ArrayList<DMatch>> matches, ImageProvidor db, ImageContainer query,double distanceThreshold,
                      double inlierThreshold);

    /**
     * This method sets the params for training the matcher and must be called before train
     * @param params json object representing the params
     */
    public void setTrainingParams(JSONObject params);
}
