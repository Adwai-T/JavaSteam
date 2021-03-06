package login;

import login.guard.EndPoints;
import login.guard.SteamGuardAccount;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import utils.*;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Login {

    private HttpClient client;
    private HashMap<String, String> cookies;
    private HashMap<String, String> transferParameters;

    private static final String url_rsa = "https://steamcommunity.com/login/getrsakey/";
    private static final String url_dologin = "https://steamcommunity.com/mobilelogin/dologin/";
//    private static final String url_dologin = "https://steamcommunity.com/login/dologin";


    //Headers
    private static final String accept = "*/*";
    private static final String user_agentMobile = "Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Google Nexus 4 - 4.1.1 - API 16 - 768x1280 Build/JRO03S) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
    private static final String user_agent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36";
    private static final String content_type = "application/x-www-form-urlencoded; charset=UTF-8";
    private static final String origin = "https://steamcommunity.com";
//    private static final String referer = "https://steamcommunity.com/login/home/?goto=";
    private static final String referer =  EndPoints.COMMUNITY_BASE + "/mobilelogin?oauth_client_id=DE45CD61&oauth_scope=read_profile%20write_profile%20read_client%20write_client";
    private Map<String, String> headers;

    //RSA
    private String rsa_public;
    private String rsa_exp;
    private String rsa_timeStamp;

    /**
     * Login to steam and get authentication data need for further requests.
     * @param client HttpClient
     * @throws InterruptedException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IOException
     */
    public Login(HttpClient client)
            throws InterruptedException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        this.client = client;
        cookies = new HashMap<>();
        transferParameters = new HashMap<>();

        headers = new HashMap<>();
        headers.put("Accept", accept);
        headers.put("Origin", origin);
        headers.put("Referer", referer);
        headers.put("User-Agent", user_agentMobile);
        headers.put("Content-Type", content_type);

        try {
            getRsa();
        } catch (Exception e){
            System.out.println(ColorToTerminal.ANSI_RED + "RSA value update failed" + ColorToTerminal.ANSI_RESET);
            e.printStackTrace();
        }

        //Generate Password
        String password = new RSAEncrypt(UserDetails.PASSWORD, rsa_public, rsa_exp).getEncryptedPassword();

        String twofactorcode;

        if(Objects.equals(UserDetails.SHAREDSECRET, null)) {
//            Get twofactorcode from user.
            System.out.println(ColorToTerminal.ANSI_GREEN_BACKGROUND + ColorToTerminal.ANSI_BLACK + "Please Enter Two-Factor Authentication Code : " + ColorToTerminal.ANSI_RESET);
            twofactorcode = GetUserInputFromTerminal.getString();
        }else {
            SteamGuardAccount steamGuardAccount = new SteamGuardAccount(client, cookies);
            twofactorcode = steamGuardAccount.generateSteamGuardCode();
        }

        //Login Form
        Map<String, String> formData = new HashMap<>();
        formData.put("username", UserDetails.USERNAME);
        formData.put("password", password);
        formData.put("twofactorcode", twofactorcode);
        formData.put("oauth_client_id", "DE45CD61"); //---Added
        formData.put("oauth_scope", "read_profile write_profile read_client write_client");//---Added
        formData.put("emailauth", "");
        formData.put("loginfriendlyname", "");
        formData.put("captchagid", "-1");
        formData.put("captcha_text", "");
        formData.put("emailsteamid", "");
        formData.put("rsatimestamp", rsa_timeStamp);
        formData.put("remember_login", "true");
        String body = Form_UrlEncoder.encode(formData);

        //Login Request
        HttpRequest request = HttpRequestBuilder.build(url_dologin, headers, HttpRequestBuilder.RequestType.POST, body);
        HttpResponse<String> response;
        try{
            response = client.send(request, BodyHandlers.ofString());
            JSONObject responseObject = (JSONObject) JSONValue.parse(response.body());

//            ColorToTerminal.printBLUE(Integer.toString(response.statusCode()));
//            ColorToTerminal.printBLUE(responseObject.toJSONString());
//            ColorToTerminal.printBLUE(response.headers().toString());

            if(responseObject.containsKey("success") && (boolean)responseObject.get("success")){

                System.out.println(ColorToTerminal.ANSI_CYAN + "Logged-in Successfully" + ColorToTerminal.ANSI_RESET);

                //Save Cookies in a file as well as memory
                saveCookiesAndTransferParameters(Cookies_Handler.getCookiesFromHeader(response.headers()), responseObject);

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
    private void saveCookiesAndTransferParameters(HashMap<String, String> allcookies, JSONObject response) throws Exception{
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

        //Get transfer_parameters from response body. Contains oauth_token that needs to be persisted.
        JSONObject transfer_parameters = (JSONObject) response.get("transfer_parameters");
        cookies.put("steamid", (String)transfer_parameters.get("steamid"));

        transferParameters.put("steamid", (String)transfer_parameters.get("steamid"));
        transferParameters.put("webcookie", (String)transfer_parameters.get("webcookie"));
        transferParameters.put("auth", (String)transfer_parameters.get("auth"));
        transferParameters.put("token_secure", (String)transfer_parameters.get("token_secure"));

        //Save to File
        File file = new File("Cookies.json");
        Files_Handler.writeMapAsJSONToFile(file, cookies);
        System.out.println(ColorToTerminal.ANSI_BLUE + "Cookies.json File saved in root folder. Please do not share this file." + ColorToTerminal.ANSI_RESET);

        File file1 = new File("TransferParameters.json");
        Files_Handler.writeMapAsJSONToFile(file1, transferParameters);
        ColorToTerminal.printBLUE("TransferParameters.json File saved in root folder. Please do not share this file.");
    }

    /**
     * Cookies that we get once we have logged in to steam.
     * @return Login set-cookies
     */
    public HashMap<String, String> getCookies() {
        return cookies;
    }

    /**
     * Generates a Steam Session Id.
     * @return sessionId.
     */
    public static String generateSessionId() {
        return new BigInteger(96, new Random()).toString(16);
    }

    /**
     * Steam Transfer Parameters parsed from the response when we login to steam. Used for Trade confirmations.
     * @return Steam Transfer Parameters
     */
    public HashMap<String, String> getTransferParameters() {
        return transferParameters;
    }
}
