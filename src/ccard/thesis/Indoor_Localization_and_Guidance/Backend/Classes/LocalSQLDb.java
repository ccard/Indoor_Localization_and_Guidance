package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Chris Card on 4/23/14.
 */
public class LocalSQLDb extends SQLiteOpenHelper implements DataBase {

    private static final String DATABASE_NAME = "Images.db";
    private static final int SCHEMA_VERSION = 1;

    private static final String DATABASE_INIT_IMAGE = "CREATE TABLE images (_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
            "location INTEGER REFERENCES locations(_id) NOT DEFERRABLE NOT NULL ON CONFLICT FAIL, "+
            "descriptor TEXT, keypoints TEXT)";

    private static final String DATABASE_INIT_LOCATIONS = "CREATE TABLE locations (_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
            "location TEXT)";

    private static final String CHECK_LOCATION = "SELECT _id FROM locations WHERE location=?";

    private static final String GET_LOCATION = "SELECT locations.location FROM locations, images "+
            "WHERE locations._id=images.location AND images._id=?";

    private static final String GET_ALL_IMAGES = "SELECT _id AS ID, location, descriptor, keypoints "+
            "FROM images";

    private static final String CHECK_FOR_IMAGES = "SELECT COUNT(*) FROM images";


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
        long numRows = DatabaseUtils.queryNumEntries(getReadableDatabase(),"images");
        return (numRows > 0 ? true : false);
    }

    @Override
    public boolean closeConnection() throws DBError {
        this.close();
        return true;
    }

    @Override
    public boolean sendRequest(JSONObject request, RequestType type) throws DBError {
        return false;
    }

    @Override
    public Map<Integer, ImageContainer> getImages(Descriptor des, boolean use_des) throws DBError {
        Map<Integer,ImageContainer> images = new HashMap<Integer, ImageContainer>();

        Cursor c = getReadableDatabase().rawQuery(GET_ALL_IMAGES,null);
        if (c.getCount() == 0){
            c.close();
            return null;
        }

        c.moveToFirst();
        do {
            int id_index = c.getColumnIndex("ID");
            int id = c.getInt(id_index);
            int descript_idx = c.getColumnIndex("descriptor");
            String descriptor = c.getString(descript_idx);
            int keypt_index = c.getColumnIndex("keypoints");
            String keypoints = c.getString(keypt_index);

            ImageContainer temp = fromJSON(descriptor);
            temp.setKeypoints(keyPointsFromJSON(keypoints));
            images.put(id,temp);
            c.moveToNext();
        } while (!c.isAfterLast());
        c.close();
        return images;
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
    public boolean saveDescriptor_Keypoints(String location,ImageContainer img) throws DBError {
        String discriptor = toJSON(img.getDescriptor());
        String keypoints = keyPointsToString(img.getKeyPoints());
        long id = checkLocation(location);
        if (id < 0){
            ContentValues loc = new ContentValues();
            loc.put("location",location);
            id = getWritableDatabase().insert("locations","location",loc);
            if (-1 == id){
                DBError dbError = new DBError(new Exception("Failed to insert location"));
                throw dbError;
            }
        }

        ContentValues image = new ContentValues();
        image.put("location",id);
        image.put("descriptor",discriptor);
        image.put("keypoints",keypoints);

        long test = getWritableDatabase().insert("images","location",image);
        return (test == -1 ? false : true);
    }

    @Override
    public String getLocation(int image_id) throws DBError{
        String[] args = {image_id+""};
        Cursor c = getReadableDatabase().rawQuery(GET_LOCATION,args);
        String location = "unknown";
        if (c.getCount() > 0){
            c.moveToFirst();
            int index = c.getColumnIndex("location");
            location = c.getString(index);
        }
        c.close();
        return location;
    }

    /**
     * This method checks to see of the location with the name already exists
     * @param location the location name to check
     * @return the id of the location or -1 if not found
     * @throws DBError
     */
    private long checkLocation(String location) throws DBError{
        String[] args = {location};
        Cursor c = getReadableDatabase().rawQuery(CHECK_LOCATION,args);
        long res = -1;
        if (c.getCount() == 1){
            c.moveToFirst();
            int index = c.getColumnIndex("_id");
            res = c.getLong(index);
        }
        c.close();
        return res;
    }

    /**
     * This method converts a image descriptor to a json string
     * @param img the image with the descriptor to convert
     * @return the json string representing the descriptor
     * @throws DBError
     */
    private String toJSON(Mat img) throws DBError{
        JSONObject convert = new JSONObject();

        if (img.isContinuous()){
            int cols = img.cols();
            int rows = img.rows();
            int elemSize = (int)img.elemSize();

            byte[] data = new byte[cols*rows*elemSize];

            img.get(0, 0, data);

            try {
                convert.put("rows",rows);
                convert.put("cols",cols);
                convert.put("type",img.type());

                String dataString = new String(Base64.encode(data,Base64.DEFAULT));

                convert.put("data",dataString);
                return convert.toString();
            } catch (JSONException e) {
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
            DBError dbError = new DBError("Failed to load image",e);
            throw dbError;
        }
    }

    /**
     * This method returns a string representing the list of keypoints
     * @param keyPoints the list of key points to convert to a string
     * @return the string representing the list of key points
     * @throws DBError
     */
    private String keyPointsToString(List<KeyPoint> keyPoints) throws DBError{
        JSONArray converts = new JSONArray();

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
                keypt.put("pt",pt);
                keypt.put("angle",angle);
                keypt.put("class_id",class_id);
                keypt.put("octave",octave);
                keypt.put("response",response);
                keypt.put("size",size);
                converts.put(keypt);
            }
            JSONObject rt = new JSONObject();
            rt.put("keypoints",converts);
            return rt.toString();
        } catch (JSONException e) {
            DBError dbError = new DBError("Failed to convert key point to json",e);
            throw dbError;
        }
    }

    /**
     * This method converts a string json to a list of key points
     * @param json the string representing the list of keypoints
     * @return the list of keypoints
     * @throws DBError
     */
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
            e.printStackTrace();
            DBError dbError = new DBError("Failed to reconstruct key points",e);
            throw dbError;
        }
    }

}
