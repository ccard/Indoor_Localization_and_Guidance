package ccard.thesis.Indoor_Localization_and_Guidance.Backend;

import android.content.Context;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.features2d.KeyPoint;

import java.util.ArrayList;

/**
 * Created by Chris Card on 3/3/14.
 * This interface represents the descriptor objects and their calculation
 */
public interface Descriptor {

    public enum MaskTypes{Circle,Square}

    /**
     * This returns all the key points for the image
     * @return a list of all the key points
     */
    public ArrayList<KeyPoint> getKeyPoints();

    /**
     * This gets the mat descriptor of the image
     * @return the descriptor of the image
     */
    public Mat getDescriptor();

    /**
     * This method makes a mask
     * @param type the type of the mask that should be created
     * @param radius_side if the lenght of the list is 1 then it is a
     *                    radius if its length is two then it is a square
     * @return true if it succeeded
     */
    public boolean makeMask(MaskTypes type, int... radius_side);

    /**
     * This method inits the descripotr based on the params passed in
     * @param params This requires a fields being the type of descriptor
     * @return true if it succeeded
     */
    public boolean initDescriptor(JSONObject params,Context context);

    /**
     * This method calculates the descriptor for the image
     * @return true if it succeeded
     */
    public boolean calculateDescriptor(ImageContainer image);
}
