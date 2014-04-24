package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.DataBase;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.Descriptor;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageContainer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.features2d.KeyPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Chris Card on 4/23/14.
 */
public class LocalSQLDb extends SQLiteOpenHelper implements DataBase {

    private static final String DATABASE_NAME = "Images.db";
    private static final int SCHEMA_VERSION = 1;

    private static final String DATABASE_INIT_IMAGE = "CREATE TABLE images (_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
            "location INTEGER REFERENCES locations(_id) NOT DEFERRABLE"+
            "descriptor TEXT, keypoints TEXT);";

    private static final String DATABASE_INIT_LOCATIONS = "CREATE TABLE locations (_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
            "location TEXT);";


    public LocalSQLDb(Context context){
        super(context,DATABASE_NAME,null,SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_INIT_IMAGE);
        sqLiteDatabase.execSQL(DATABASE_INIT_LOCATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        //TODO: implement when sql database is implemented
    }

    @Override
    public boolean openConnection() throws DBError {
        return false;
    }

    @Override
    public boolean closeConnection() throws DBError {
        return false;
    }

    @Override
    public boolean sendRequest(JSONObject request, RequestType type) throws DBError {
        return false;
    }

    @Override
    public Map<Integer, ImageContainer> getImages(Descriptor des, boolean use_des) throws DBError {
        return null;
    }

    @Override
    public ArrayList<Integer> getPath() throws DBError {
        return null;
    }

    @Override
    public JSONObject getParams(ParamReturn paramReturn) throws DBError {
        JSONObject params = new JSONObject();
        switch (paramReturn){
            case Descriptor:
                try {
                    params.put("scaleFactor",1.2f)
                            .put("nLevels",8)
                            .put("firstLevel",0)
                            .put("edgeThreshold",31)
                            .put("patchSize",31)
                            .put("WTA_K",3)
                            .put("scoreType",0)
                            .put("nFeatures",500);
                } catch (JSONException e) {
                    DBError dbe = new DBError("Error occurred setting up descript params",
                            e);
                    throw dbe;
                }

                break;
            case Matcher:
                try {
                    params.put("table_number",30)
                            .put("key_size",20)
                            .put("multi_probe_level",2);
                } catch (JSONException e) {
                    DBError dbe = new DBError("Error occurred setting up descript params",
                            e);
                    throw dbe;
                }
                break;
        }

        return params;
    }

    @Override
    public boolean saveDescriptor_Keypoints(List<ImageContainer> img) throws DBError {

        //TODO: implement saving tringing images
        return false;
    }

    /**
     * This method converts a image descriptor to a json string
     * @param img the image with the descriptor to convert
     * @return the json string representing the descriptor
     * @throws DBError
     */
    private String toJSON(ImageContainer img) throws DBError{
        JSONObject convert = new JSONObject();

        if (img.getDescriptor().isContinuous()){
            int cols = img.getDescriptor().cols();
            int rows = img.getDescriptor().rows();
            int elemSize = (int)img.getDescriptor().elemSize();

            byte[] data = new byte[cols*rows*elemSize];

            img.getDescriptor().get(0,0,data);

            try {
                convert.put("rows",rows);
                convert.put("cols",cols);
                convert.put("type",img.getDescriptor().type());

                String dataString = new String(Base64.encode(data,Base64.DEFAULT));

                convert.put("data",dataString);
                return convert.toString();
            } catch (JSONException e) {
                LogFile.getInstance().e(e.getStackTrace().toString());
                LogFile.getInstance().flushLog();
                DBError dbError = new DBError("Error converting image to JSON",e);
                throw dbError;
            }
        } else {
            LogFile.getInstance().e("Mat is not continious");
            LogFile.getInstance().flushLog();
        }

        return "{}";
    }

    /**
     * This method converts a json string to a image container
     * @param json the json string reperesenting the descriptor
     * @return the imagecontainer with the descriptor
     * @throws DBError
     */
    private ImageContainer fromJSON(String json) throws DBError{
        try {
            JSONObject convert = new JSONObject(json);
            int rows = convert.getInt("rows");
            int cols = convert.getInt("cols");
            int type = convert.getInt("type");

            String dataString = convert.getString("data");

            byte[] data = Base64.decode(dataString.getBytes(),Base64.DEFAULT);

            Mat descriptor = new Mat(rows,cols,type);
            descriptor.put(0,0,data);
            ImageContainer descript = new DescriptorContainer();

            descript.setDescriptor(descriptor);
            return descript;
        } catch (JSONException e) {
            LogFile.getInstance().e(e.getStackTrace().toString());
            LogFile.getInstance().flushLog();
            DBError dbError = new DBError("Failed to load image",e);
            throw dbError;
        }
    }

    private String keyPointsToString(List<KeyPoint> keyPoints) throws DBError{
        List<String> converts = new ArrayList<String>();

        try {
            for (KeyPoint kp : keyPoints){
                float angle = kp.angle;
                int class_id = kp.class_id;
                int octave = kp.octave;
                double x = kp.pt.x;
                double y = kp.pt.y;
                float response = kp.response;
                float size = kp.size;
                JSONObject pt = new JSONObject();
                pt.put("x",x);
                pt.put("y",y);
                JSONObject keypt = new JSONObject();
                keypt.put("pt",pt.toString());
                keypt.put("angle",angle);
                keypt.put("class_id",class_id);
                keypt.put("octave",octave);
                keypt.put("response",response);
                keypt.put("size",size);
                converts.add(keypt.toString());
            }
            JSONArray ret = new JSONArray(converts);
            JSONObject rt = new JSONObject();
            rt.put("keypoints",ret.toString());
            return rt.toString();
        } catch (JSONException e) {
            LogFile.getInstance().e(e.getStackTrace().toString());
            LogFile.getInstance().flushLog();
            DBError dbError = new DBError("Failed to convert key point to json",e);
            throw dbError;
        }
    }

    private List<KeyPoint> keyPointsFromJSON(String json) throws DBError{
        try {
            JSONObject converts = new JSONObject(json);
            List<KeyPoint> ret = new ArrayList<KeyPoint>();

            JSONArray keypoints = converts.getJSONArray("keypoints");
            for (int i = 0; i < keypoints.length(); i++){
                JSONObject keypoint = keypoints.getJSONObject(i);
                float angle = (float)keypoint.getDouble("angle");
                int class_id = keypoint.getInt("class_id");
                int octave = keypoint.getInt("octave");
                JSONObject pt = keypoint.getJSONObject("pt");
                double x = pt.getDouble("x");
                double y = pt.getDouble("y");
                float response = (float)keypoint.getDouble("response");
                float size = (float)keypoint.getDouble("size");
                KeyPoint kp = new KeyPoint();
                kp.angle = angle;
                kp.class_id = class_id;
                kp.octave = octave;
                kp.pt = new Point(x,y);
                kp.response = response;
                kp.size = size;
                ret.add(kp);
            }
            return ret;
        } catch (JSONException e) {
            LogFile.getInstance().e(e.getStackTrace().toString());
            LogFile.getInstance().flushLog();
            DBError dbError = new DBError("Failed to reconstruct key points",e);
            throw dbError;
        }
    }

}
