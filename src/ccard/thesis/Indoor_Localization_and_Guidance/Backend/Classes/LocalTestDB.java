package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.DataBase;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.Descriptor;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageContainer;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ch on 3/5/14.
 */
public class LocalTestDB implements DataBase {

    private Context context;
    public LocalTestDB(Context context){
        this.context = context;
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
    public boolean saveDescriptor_Keypoints(String location,ImageContainer img) throws DBError {
        return false;
    }

    @Override
    public Map<Integer, ImageContainer> getImages(Descriptor des, boolean use_des) throws DBError{
        Map<Integer, ImageContainer> images = new HashMap<Integer, ImageContainer>();

        ArrayList<String> files = new ArrayList<String>();

        try {
            AssetManager manager = context.getResources().getAssets();
            InputStream is = null,is2 = null;
            is = manager.open("image_locations.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String file;
            while((file = in.readLine()) != null){
                file += "/";
                is2 = manager.open(file+"images.txt");
                BufferedReader in2 = new BufferedReader(new InputStreamReader(is2));
                String file2;
                while((file2 = in2.readLine()) != null){
                    files.add(file+file2);
                }
                in2.close();
            }
            in.close();
            is.close();
            is2.close();
            is = null;
            int id = 0;
            for(String i : files){
                is = manager.open(i);
                MyMat tmp = new MyMat(BitmapFactory.decodeStream(is));

                if (use_des){
                    tmp.calcDescriptor(des);
                    tmp.release();
                    images.put(id++,tmp);
                } else {
                    Imgproc.resize(tmp, tmp, new Size(tmp.cols() / 2, tmp.rows() / 2));
                    images.put(id++,new MyMat(tmp));
                    tmp.release();
                }
            }
            is.close();
        } catch (FileNotFoundException e) {
            DBError dbError = new DBError("File not found",e);
            throw dbError;
        } catch (IOException e){
            DBError dbError = new DBError("IO error",e);
            throw dbError;
        }

        return images;
    }

    @Override
    public ArrayList<Integer> getPath() throws DBError{
        return null;
    }

    @Override
    public JSONObject getParams(ParamReturn paramReturn) throws DBError{
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
    public String getLocation(int image_id) throws DBError{
        return null;
    }

    /**
     * This method gets the list of all image files
     * @return the list of image files
     * @throws DBError
     */
    public List<String> getImageFiles() throws DBError{
        ArrayList<String> files = new ArrayList<String>();

        try {
            AssetManager manager = context.getResources().getAssets();
            InputStream is = null,is2 = null;
            is = manager.open("image_locations.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String file;
            while((file = in.readLine()) != null){
                file += "/";
                is2 = manager.open(file+"images.txt");
                BufferedReader in2 = new BufferedReader(new InputStreamReader(is2));
                String file2;
                while((file2 = in2.readLine()) != null){
                    files.add(file+file2);
                }
                in2.close();
            }
            in.close();
            is.close();
            return files;
        } catch (FileNotFoundException e) {
            DBError dbError = new DBError("File not found",e);
            throw dbError;
        } catch (IOException e){
            DBError dbError = new DBError("IO error",e);
            throw dbError;
        }
    }

    /**
     * This method loads an image file from the specified image file
     * @param file the file
     * @return the image
     * @throws DBError
     */
    public ImageContainer loadImage(String file,Descriptor des) throws DBError{
        AssetManager manager = context.getResources().getAssets();
        InputStream is = null;
        try {
            is = manager.open(file);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = false;
            opt.inPreferredConfig = Bitmap.Config.RGB_565;
            opt.inDither = false;
            Bitmap img = BitmapFactory.decodeStream(is,null,opt);
            MyMat tmp = new MyMat(img);
            img.recycle();
            img = null;
            tmp.calcDescriptor(des);
            Imgproc.resize(tmp,tmp,new Size(tmp.rows()/2,tmp.cols()/2));
            is.close();
            return tmp;
        } catch (IOException e) {
            DBError dbError = new DBError("Failed to load file",e);
            throw dbError;
        }
    }
}
