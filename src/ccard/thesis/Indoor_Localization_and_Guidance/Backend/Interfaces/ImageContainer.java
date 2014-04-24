package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces;

import android.graphics.Bitmap;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes.MyDMatch;
import org.opencv.core.Mat;
import org.opencv.features2d.KeyPoint;

import java.util.ArrayList;
import java.util.List;

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
     * @param withKeyPoints if key Points are to be drawn
     * @return true if it succeded
     */
    public Bitmap render(boolean withKeyPoints);

    /**
     * This method draws the comparison between the two image keypoints
     * ensure that the image calling this method is the query image
     * @param im2 the image that it will be compared to
     * @param matches the list of matches between the two images
     * @return true if it succeeded
     */
    public Bitmap renderComparision(ImageContainer im2,
                                     ArrayList<MyDMatch> matches);

    /**
     * This method calculates the descriptor for the image
     * @return true if it succeeded
     */
    public boolean calcDescriptor(Descriptor des);

    /**
     * This method sets the descriptor for the image
     * @param descriptor the desciptor set the descriptor to
     * @return true if success false other wise
     */
    public boolean setDescriptor(Mat descriptor);

    /**
     * This method sets the key points for the image
     * @param keyPoints the keypoints to set the keypoints to
     * @return true if success
     */
    public boolean setKeypoints(List<KeyPoint> keyPoints);


}
