package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.DataBase;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageContainer;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
        return null;
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

        return null;
    }
}
