package ccard.thesis.Indoor_Localization_and_Guidance.Backend;

import android.content.Context;
import android.util.Pair;
import org.json.JSONObject;
import org.opencv.features2d.DescriptorMatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

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

        return false;
    }

    @Override
    public Pair<Integer, String> match(JSONObject params, ImageContainer query) {
        return null;
    }
}
