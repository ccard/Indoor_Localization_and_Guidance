package ccard.thesis.Indoor_Localization_and_Guidance.Backend;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Chris Card on 3/3/14.
 * This is an interface for the Image Providor and will get
 * provides images from a database or a server etc..
 */
public interface ImageProvidor {

    /**
     * This method sets the intentservice to use for when retrieving data
     * @param intentService the class of the intent service to use when retreivng
     *                      images
     */
    public void setIntentService(Class<?> intentService);

    /**
     * This method sets the database for the intentservice to communicate with
     * @param db the database object to use as the source of the images
     */
    public void setDatabase(DataBase db);

    /**
     * Retrieves the image at the desired index
     * @param index of the image in the database
     * @return the image container of the image at the index
     */
    public ImageContainer getImage(int index);

    /**
     * This methods gets all images that the image provider has
     * @return the list of images
     */
    public ArrayList<ImageContainer> getImages();

    /**
     * This method requests
     * @param params
     */
    public void requestImages(JSONObject params);

}
