package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces;

import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes.DBError;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Chris Card on 3/3/14.
 * This interface is used for interacting with a database so multiple database
 * can be used in the future
 */
public interface DataBase extends Serializable{

    public enum RequestType{Images,Images_Path};

    public enum ParamReturn{Descriptor,Matcher};

    /**
     * This opens the connection to the database
     * @return true if it succeeded in opening the connection
     * @throws DBError
     */
    public boolean openConnection() throws DBError;

    /**
     * Closes the connection to the database
     * @return true if it closed the connection
     * @throws DBError
     */
    public boolean closeConnection() throws DBError;

    /**
     * This method sends a request to the server
     * @param request json object that represents the request
     * @return true if it was the request was sent and a results where
     * gotten from the database
     * @throws DBError
     */
    public boolean sendRequest(JSONObject request,RequestType type) throws DBError;

    /**
     * This method returns the images gotten from the database
     * @return the list of images
     * @throws DBError
     */
    public Map<Integer,ImageContainer> getImages(Descriptor des, boolean use_des) throws DBError;

    /**
     * Returns the path to follow
     * @return path of the images in the database
     * @throws DBError
     */
    public ArrayList<Integer> getPath() throws DBError;

    /**
     * This method gets params from the database
     * @return JSON object representing the params
     * @throws DBError
     */
    public JSONObject getParams(ParamReturn paramReturn) throws DBError;

    /**
     * This method saves a list of image descriptors and key points to the database
     * @param img the list of images to save the descriptors of
     * @return true if success
     * @throws DBError
     */
    public boolean saveDescriptor_Keypoints(List<ImageContainer> img) throws DBError;

}
