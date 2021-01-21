package login;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import utils.*;

import java.io.File;
import java.math.BigInteger;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.*;

public class Login {

    private HttpClient client;
    private HashMap<String, String> cookies;

    private static final String url_rsa = "https://steamcommunity.com/login/getrsakey/";
    private static final String url_dologin = "https://steamcommunity.com/mobilelogin/dologin/";

    //Headers
    private static final String accept = "*/*";
    private static final String user_agent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36";
    private static final String content_type = "application/x-www-form-urlencoded; charset=UTF-8";
    private static final String origin = "https://steamcommunity.com";
    private static final String referer = "https://steamcommunity.com/login/home/?goto=";
    private Map<String, String> headers;

    //RSA
    private String rsa_public;
    private String rsa_exp;
    private String rsa_timeStamp;

    public Login(HttpClient client){
        this.client = client;
        cookies = new HashMap<>();

        headers = new HashMap<>();
        headers.put("Accept", accept);
        headers.put("Origin", origin);
        headers.put("Referer", referer);
        headers.put("User-Agent", user_agent);
        headers.put("Content-Type", content_type);

        try {
            getRsa();
        } catch (Exception e){
            System.out.println(ColorToTerminal.ANSI_RED + "RSA value update failed" + ColorToTerminal.ANSI_RESET);
            e.printStackTrace();
        }

        //Generate Password
        String password = new RSAEncrypt(UserDetails.PASSWORD, rsa_public, rsa_exp).getEncryptedPassword();

        //Get twofactorcode from user.
        System.out.println(ColorToTerminal.ANSI_GREEN_BACKGROUND + ColorToTerminal.ANSI_BLACK + "Please Enter Two-Factor Authentication Code : " + ColorToTerminal.ANSI_RESET);
        String twofactorcode = GetUserInputFromTerminal.getString();

        //Login Form
        Map<String, String> fromData = new HashMap<>();
        fromData.put("username", UserDetails.USERNAME);
        fromData.put("password", password);
        fromData.put("twofactorcode", twofactorcode);
        fromData.put("emailauth", "");
        fromData.put("loginfriendlyname", "");
        fromData.put("captchagid", "-1");
        fromData.put("captcha_text", "");
        fromData.put("emailsteamid", "");
        fromData.put("rsatimestamp", rsa_timeStamp);
        fromData.put("remember_login", "false");
        String body = Form_UrlEncoder.encode(fromData);

        //Login Request
        HttpRequest request = HttpRequestBuilder.build(url_dologin, headers, HttpRequestBuilder.RequestType.POST, body);
        HttpResponse<String> response;
        try{
            response = client.send(request, BodyHandlers.ofString());
            JSONObject responseObject = (JSONObject) JSONValue.parse(response.body());

            if(responseObject.containsKey("success") && (boolean)responseObject.get("success")){

                System.out.println(ColorToTerminal.ANSI_CYAN + "Logged-in Successfully" + ColorToTerminal.ANSI_RESET);

                //Save Cookies in a file as well as memory
                saveCookies(Cookies_Handler.getCookiesFromHeader(response.headers()));

            }else {
                System.out.println(response.body());
                throw new Exception("Login Failed");
            }

        }catch (Exception e){
            System.out.println(ColorToTerminal.ANSI_RED + e.getMessage() + ColorToTerminal.ANSI_RESET);
            e.printStackTrace();
        }
    }

    //Gets Rsa key
    private HttpResponse<String> getRsa() throws Exception {
        Map<String, String> formData = new HashMap<>();
        formData.put("username", UserDetails.USERNAME);
        String body = Form_UrlEncoder.encode(formData);

        HttpRequest request = HttpRequestBuilder.build(url_rsa, headers, HttpRequestBuilder.RequestType.POST, body);

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        JSONObject responseObject = (JSONObject) JSONValue.parse(response.body());

        if(responseObject.containsKey("success") && (boolean)responseObject.get("success")){
            rsa_public = (String) responseObject.get("publickey_mod");
            rsa_exp = (String) responseObject.get("publickey_exp");
            rsa_timeStamp = (String) responseObject.get("timestamp");
            System.out.println(ColorToTerminal.ANSI_CYAN+ "RSA values set/updated" + ColorToTerminal.ANSI_RESET);
        }
        else{
            throw new Exception("RSA Update failed");
        }

        return response;
    }

    //Put needed cookies in cookies Map
    private void saveCookies(HashMap<String, String> allcookies) throws Exception{
        //Delete all previous cookies as we need to add new Cookies every login
        cookies.clear();

        //Add any needed cookies to the list
        List<String> neededCookies = new ArrayList<>();
        neededCookies.add("steamCountry");
        neededCookies.add("steamMachineAuth76561198865293952");
        neededCookies.add("steamLoginSecure");
//        neededCookies.add("steamRememberLogin"); //Might Not be needed;

        for(String neededCookie : neededCookies){
            if(allcookies.containsKey(neededCookie)){
                cookies.put(neededCookie, allcookies.get(neededCookie));
            }else throw new Exception("Cookies does not contain one of the needed Authentication Cookie");
        }

        //Save to File
        File file = new File("Auth.json");
        Files_Handler.writeMapAsJSONToFile(file, cookies);
        System.out.println(ColorToTerminal.ANSI_BLUE + "Auth.json File saved in root folder. Please do not share this file." + ColorToTerminal.ANSI_RESET);
    }

    public HashMap<String, String> getCookies() {
        return cookies;
    }

    public static String generateSessionId() {
        return new BigInteger(96, new Random()).toString(16);
    }
}
