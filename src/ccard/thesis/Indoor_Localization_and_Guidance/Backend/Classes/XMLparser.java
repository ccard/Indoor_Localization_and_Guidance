package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;

/**
 * Created by Chris Card on 3/4/14.
 * This class contains methods for iterating through json objects and
 * making them into xml
 */
public class XMLparser {

    static final String xml_header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    public enum DESCRIPTORS{
        ORB("Feature2D.ORB"), LSH("FLANN_INDEX_LSH");
    private String val;
        DESCRIPTORS(String v){
            val = v;
        }
        public String val(){
            return val;
        }

    };

    public static String build_XML(JSONObject j,DESCRIPTORS d){
        StringBuilder xml = new StringBuilder();
        xml.append(xml_header+"\n");
        xml.append("<opencv_storage>\n");
        if(d.val() == DESCRIPTORS.LSH.val()){
            xml.append("<indexParams>\n");
        }
        xml.append("<name>"+d.val()+"</name>");
        xml.append(build_helper(j));
        if(d.val() == DESCRIPTORS.LSH.val()){
            xml.append("</indexParams>\n");
        }
        xml.append("</opencv_storage>\n");

        return xml.toString();
    }

    private static String build_helper(JSONObject j){
        StringBuilder xml = new StringBuilder();

        Iterator<String> keys = j.keys();
        while (keys.hasNext()){
            String key = keys.next();
            String val = null;
            try {
                JSONObject value = j.getJSONObject(key);
                xml.append("<"+key+">").append(build_helper(value)).append("</"+key+">\n");
            } catch (Exception e) {
                try {
                    Object temp = j.get(key);
                    val = temp.toString();
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

            if (val != null) {
                xml.append("<"+key+">"+val+"</"+key+">\n");
            }
        }
        return xml.toString();
    }

    public static File createXMLFile(String name, String xml, Context context){
        File outDir = context.getCacheDir();
        File outFile;

        try {
            outFile = File.createTempFile(name,".xml",outDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if(outFile == null) return null;

        try {
            FileOutputStream out = new FileOutputStream(outFile);
            OutputStreamWriter writeOut = new OutputStreamWriter(out);
            writeOut.write(xml);
            writeOut.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
       return outFile;
    }
}
