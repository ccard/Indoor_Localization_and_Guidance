package ccard.thesis.Indoor_Localization_and_Guidance.Backend;

import android.util.Pair;
import org.json.JSONObject;
import org.opencv.features2d.DMatch;

import java.util.ArrayList;

/**
 * Created by Ch on 3/3/14.
 * This interface is for the matcher objects to perform the matching of the
 * images
 */
public interface Matcher {

    /**
     * This method trains the database matcher with the given params so
     * that matching can be performed against it
     * @param params the params to init the database to
     * @param db the list of images that represents the database
     * @return true if the training succeeded
     */
    public boolean train(JSONObject params,ArrayList<ImageContainer> db);

    /**
     * This method matches the query image to the database of images
     * @param params the params to use to match with
     * @param query the query image
     * @return a pair of the image index and the string representing its location
     */
    public ArrayList<DMatch> match(JSONObject params, ImageContainer query);
}
