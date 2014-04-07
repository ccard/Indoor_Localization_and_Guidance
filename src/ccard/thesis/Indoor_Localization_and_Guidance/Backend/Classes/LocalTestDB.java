package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.DataBase;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageContainer;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ch on 3/5/14.
 */
public class LocalTestDB implements DataBase {
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
    public Map<Integer, ImageContainer> getImages() throws DBError{
        Map<Integer, ImageContainer> images = new HashMap<Integer, ImageContainer>();

        ArrayList<String> files = new ArrayList<String>();

        try {
            BufferedReader in = new BufferedReader(new FileReader("image_locations.txt"));
            String file;
            while((file = in.readLine()) != null){
                file += "/";
                BufferedReader in2 = new BufferedReader(new FileReader(file+"images.txt"));
                String file2;
                while((file2 = in2.readLine()) != null){
                    files.add(file+file2);
                }
                in2.close();
            }
            in.close();

            int id = -1;
            for(String i : files){
                MyMat tmp = new MyMat();
                Highgui.imread(i).copyTo(tmp);
                Imgproc.resize(tmp,tmp,new Size(tmp.cols()/4,tmp.rows()/4));
                images.put(id++,tmp);
                tmp.release();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
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
                    .put("WTA_K",2)
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
}
