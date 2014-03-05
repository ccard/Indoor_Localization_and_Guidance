package ccard.thesis.Indoor_Localization_and_Guidance.Backend;

import android.content.Context;
import android.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorMatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ch on 3/5/14.
 */
public class LSHMatcher implements Matcher {

    private DescriptorMatcher matcher;
    private Context context;
    public LSHMatcher(Context context){
        matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        this.context = context;
    }

    @Override
    public boolean train(JSONObject params, ArrayList<ImageContainer> db) {

        String xml = XMLparser.build_XML(params);
        File outFile = XMLparser.createXMLFile("LSHParams",xml,context);
        matcher.read(outFile.getPath());

        matcher.add(getDescriptors(db));
        matcher.train();
        return false;
    }

    @Override
    public ArrayList<ArrayList<DMatch>> match(JSONObject params, ImageContainer query) {
        if(!params.has("Type")) return null;
        ArrayList<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
        try {
            if((MatchingType)params.get("Type") == MatchingType.LSH){
                int k = 5;
                ArrayList<Mat> masks = null;
                boolean compactres = false;

                k = params.getInt("k");
                compactres = params.getBoolean("compactResults");

                matcher.knnMatch(query.getDescriptor(),matches,k,masks,compactres);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (matches.isEmpty()) return null;

        ArrayList<ArrayList<DMatch>> match = new ArrayList<ArrayList<DMatch>>(matches.size());

        for(int i = 0; i < matches.size(); i++){
            match.add(i,new ArrayList<DMatch>(matches.get(i).toList()));
        }
        return match;
    }

    private List<Mat> getDescriptors(ArrayList<ImageContainer> db){
        List<Mat> ret = new ArrayList<Mat>();

        for(ImageContainer im : db){
            ret.add(im.getDescriptor());
        }
        return ret;
    }

}
