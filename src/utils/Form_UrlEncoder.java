package utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Form_UrlEncoder {

    /**
     * Convert the key-value pair in the Map to a String that is x-www-form-urlencoded
     * @param params
     * @return
     */
    public static String encode(Map<String, String> params){

        StringBuilder stringBuilder = new StringBuilder();

        for(String param : params.keySet()){
            stringBuilder.append(
                            URLEncoder.encode(param, StandardCharsets.UTF_8)
                            + "="
                            + URLEncoder.encode(params.get(param), StandardCharsets.UTF_8));
            stringBuilder.append("&");
        }

        String encodedString = stringBuilder.toString();

        //Remove '&' at the end of the String
        return encodedString.substring(0, encodedString.length() - 1);
    }
}
