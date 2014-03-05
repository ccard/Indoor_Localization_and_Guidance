package ccard.thesis.Indoor_Localization_and_Guidance.Backend;

import android.content.Context;
import android.util.Pair;
import org.json.JSONObject;
import org.opencv.core.Mat;
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
    public ArrayList<DMatch> match(JSONObject params, ImageContainer query) {
        return null;
    }

    private List<Mat> getDescriptors(ArrayList<ImageContainer> db){
        List<Mat> ret = new ArrayList<Mat>();

        for(ImageContainer im : db){
            ret.add(im.getDescriptor());
        }
        return ret;
    }

}
