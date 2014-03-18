package ccard.thesis.Indoor_Localization_and_Guidance.Backend;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Ch on 3/18/14.
 */
public class LocalImageProvider implements ImageProvidor {

    //TODO: add image resources to the application for testing
    private DataBase db;
    private Class<?> intentService;
    private Map<Integer, ImageContainer> images;
    @Override
    public void setIntentService(Class<?> intentService) {
        this.intentService = intentService;
    }

    @Override
    public void setDatabase(DataBase db) {
        this.db = db;
    }

    @Override
    public ImageContainer getImage(int index) {
        if (null == images) return null;
        if (!images.containsKey(index)) return null;
        return images.get(index);
    }

    @Override
    public Map<Integer,ImageContainer> getImages() {
        return images;
    }

    @Override
    public void requestImages(JSONObject params) {
        try {
            images = db.getImages();
        } catch (DBError dbError) {
            dbError.printStackTrace();
        }
    }
}
