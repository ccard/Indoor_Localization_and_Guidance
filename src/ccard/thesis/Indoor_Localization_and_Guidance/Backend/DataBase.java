package ccard.thesis.Indoor_Localization_and_Guidance.Backend;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Chris Card on 3/3/14.
 * This interface is used for interacting with a database so multiple database
 * can be used in the future
 */
public interface DataBase extends Serializable{

    public enum RequestType{Paramaters,Images,Images_Path};
    /**
     * This opens the connection to the database
     * @return true if it succeeded in opening the connection
     */
    public boolean openConnection();

    /**
     * Closes the connection to the database
     * @return true if it closed the connection
     */
    public boolean closeConnection();

    /**
     * This method sends a request to the server
     * @param request json object that represents the request
     * @return true if it was the request was sent and a results where
     * gotten from the database
     */
    public boolean sendRequest(JSONObject request,RequestType type);

    /**
     * This method returns the images gotten from the database
     * @return the list of images
     */
    public Map<Integer,ImageContainer> getImages();

    /**
     * Returns the path to follow
     * @return path of the images in the database
     */
    public ArrayList<Integer> getPath();

    /**
     * This method gets params from the database
     * @return
     */
    public JSONObject getParams();

}
