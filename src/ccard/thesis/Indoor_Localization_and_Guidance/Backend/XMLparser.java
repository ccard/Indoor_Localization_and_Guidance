package ccard.thesis.Indoor_Localization_and_Guidance.Backend;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by Chris Card on 3/4/14.
 * This class contains methods for iterating through json objects and
 * making them into xml
 */
public class XMLparser {

    static final String xml_header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    public static String build_XML(JSONObject j){
        StringBuilder xml = new StringBuilder();
        xml.append(xml_header+"\n");

        xml.append(build_helper(j));

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
}
