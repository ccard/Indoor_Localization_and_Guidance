package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.DataBase;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.Descriptor;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageContainer;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageProvidor;
import org.json.JSONObject;

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
    public void requestImages(JSONObject params, Descriptor descriptor) {
        try {
            images = db.getImages();

            for(Integer index : images.keySet()){
                images.get(index).calcDescriptor(descriptor);
            }
        } catch (DBError dbError) {
            dbError.printStackTrace();
        }
    }

    @Override
    public boolean hasImages() {
        if (null == images) return false;
        return !images.isEmpty();
    }

    @Override
    public void release() {
        if (images != null) images.clear();
    }
}
