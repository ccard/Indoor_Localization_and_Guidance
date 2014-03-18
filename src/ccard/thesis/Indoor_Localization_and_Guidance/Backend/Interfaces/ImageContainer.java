package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces;

import android.view.View;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes.MyDMatch;
import org.opencv.core.Mat;
import org.opencv.features2d.KeyPoint;

import java.util.ArrayList;

/**
 * Created by Chris Card on 3/3/14.
 * This interface is for images
 */
public interface ImageContainer{

    /**
     * Retreives a KeyPoint from an index with in the image
     * @param index of the keypoint
     * @return the keypoint
     */
    public KeyPoint getKeyPoint(int index);

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
     * This method tells the user if they can draw the image onto
     * a view
     * @return true if the image can be drawn
     */
    public boolean hasImageToDraw();

    /**
     * This method draws the image into a view
     * @param view the view to draw the image into
     * @param withKeyPoints if key Points are to be drawn
     * @return true if it succeded
     */
    public boolean render(final View view,boolean withKeyPoints);

    /**
     * This method draws the comparison between the two image keypoints
     * ensure that the image calling this method is the query image
     * @param view the view to draw the image on
     * @param im2 the image that it will be compared to
     * @param matches the list of matches between the two images
     * @return true if it succeeded
     */
    public boolean renderComparision(final View view,ImageContainer im2,
                                     ArrayList<MyDMatch> matches);

    /**
     * This method calculates the descriptor for the image
     * @return true if it succeeded
     */
    public boolean calcDescriptor(Descriptor des);


}
