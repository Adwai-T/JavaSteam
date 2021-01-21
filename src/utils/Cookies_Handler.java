package utils;

import java.net.http.HttpHeaders;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cookies_Handler {

    public static HashMap<String, String> getCookiesFromHeader(HttpHeaders headers){

        List<String> headerCookies = headers.map().get("set-cookie");
        HashMap<String, String> allCookieValues = new HashMap<>();

        for(String cookie : headerCookies){
            String[] cookie_strings =  cookie.split(";");

            for (String cookie_part : cookie_strings){
                String[] cookie_parts = cookie_part.split("=");

                for(int i = 0; i < cookie_parts.length; i++) {
                    cookie_parts[i] = cookie_parts[i].replace(" ", "");
                }
                if(cookie_parts.length >= 2){
                    allCookieValues.put(cookie_parts[0], cookie_parts[1]);
                }else {
                    allCookieValues.put(cookie_parts[0], "true");
                }
            }
        }
        return allCookieValues;
    }

    public static String getCookieStringFromMap(Map<String, String> cookies) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String cookie : cookies.keySet()) {
            stringBuilder.append(cookie+"="+cookies.get(cookie)+";");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }
}
