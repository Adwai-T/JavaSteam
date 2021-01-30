package login.guard;

import login.UserDetails;
import utils.ColorToTerminal;
import utils.Cookies_Handler;
import utils.HttpRequestBuilder;
import utils.Regex_Handler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class SteamGuardAccount {

    private final String sharedSecret;
    private final HttpClient client;
    private final static byte[] steamGuardCodeTranslations = new byte[] { 50, 51, 52, 53, 54, 55, 56, 57, 66, 67, 68, 70, 71, 72, 74, 75, 77, 78, 80, 81, 82, 84, 86, 87, 88, 89 };
    private final String deviceID = UserDetails.DEVICEID;
    private final Map<String, String> cookies;

    /**
     * SteamGuardAccount to get the steamGuard code for login, accept send step of 2 step confirmation.
     * @param client HttpClient
     * @param cookies Cookies map populated after login.
     */
    public SteamGuardAccount(HttpClient client, Map<String, String> cookies) {
        this.client = client;
        this.cookies = cookies;
        this.sharedSecret = UserDetails.SHAREDSECRET;
    }

    /**
     * Generate Steam Guard Code required for login.
     * @return Steam Guard Code
     * @throws IOException
     * @throws InterruptedException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public String generateSteamGuardCode()
            throws IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeyException {
        return generateSteamGuardCodeForTime(TimeAligner.getSteamTime(client));
    }

    /**
     * Generate Steam Guard Code for a given time.
     * @param time Epoch Time/Unix Time
     * @return Steam Guard code
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public String generateSteamGuardCodeForTime(long time)
            throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
        if (this.sharedSecret == null || this.sharedSecret.length() == 0) {
            return null;
        }

        byte[] sharedSecretArray = Base64.getDecoder().decode(sharedSecret);

        byte[] timeArray = new byte[8];

        time /= 30L;

        for (int i = 8; i > 0; i--) {
            timeArray[i - 1] = (byte)time;
            time >>= 8;
        }

        byte[] hashedData = HmacSHA1_Handler.calculateHMACSHA1(timeArray, sharedSecretArray);

        byte[] codeArray = new byte[5];
        try {
            byte b = (byte)(hashedData[19] & 0xF);
            int codePoint = (hashedData[b] & 0x7F) << 24 | (hashedData[b + 1] & 0xFF) << 16 | (hashedData[b + 2] & 0xFF) << 8 | (hashedData[b + 3] & 0xFF);

            for (int i = 0; i < 5; ++i) {
                codeArray[i] = steamGuardCodeTranslations[codePoint % steamGuardCodeTranslations.length];
                codePoint /= steamGuardCodeTranslations.length;
            }
        }
        catch (Exception e) { //Not the best catcher but should work.
            return null;
        }
        return new String(codeArray, StandardCharsets.UTF_8);
    }

    /**
     * Fetch Confirmation requests for Steam Guard confirmation.
     * @return List of Confirmations
     * @throws IOException
     * @throws InterruptedException
     * @throws WGTokenInvalidException
     */
    public List<Confirmation> fetchConfirmations()
            throws IOException, InterruptedException, WGTokenInvalidException {
        String url = generateConfirmationURL();

        String cookiesString = Cookies_Handler.getCookieStringFromMap(cookies);
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", cookiesString);
        headers.put("Referer", EndPoints.COMMUNITY_BASE);
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Google Nexus 4 - 4.1.1 - API 16 - 768x1280 Build/JRO03S) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        headers.put("Accept", "text/javascript, text/html, application/xml, text/xml, */*");
        headers.put("Origin", "https://steamcommunity.com");
        HttpRequest request = HttpRequestBuilder.build(url, headers, HttpRequestBuilder.RequestType.POST, "");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() == 200) {
            ColorToTerminal.printPURPLE("Confirmation Request Fetched Successfully.");
        }

        return FetchConfirmationInternal(response.body());
    }

    private List<Confirmation> FetchConfirmationInternal(String response) throws WGTokenInvalidException {

//          Regex for HTML -- while awful -- makes this way faster than parsing the DOM,
//          plus we don't need another library.
//          And because the data is always in the same place and same format...
//          It's not as if we're trying to naturally understand HTML here, just extracting Strings.
        List<List<String>> confirmations = Regex_Handler.getAllMatches(
                response,
                "<div class=\"mobileconf_list_entry\" id=\"conf[0-9]+\" data-confid=\"(\\d+)\" data-key=\"(\\d+)\" data-type=\"(\\d+)\" data-creator=\"(\\d+)\"");

        if (response == null || confirmations.size() == 0) {
            if (response == null || !response.matches("<div>Nothing to confirm</div>")) {
                throw new WGTokenInvalidException();
            }
            return new ArrayList<>();
        }

        List<Confirmation> ret = new ArrayList<>();
        for (List<String> confirmation : confirmations) {
            //If our confirmation does not have all the five variable groups matched
            if (confirmation.size() != 5) continue;

            String confID = confirmation.get(1);
            String confKey = confirmation.get(2);
            String confType = confirmation.get(3);
            String confCreator = confirmation.get(4);
            ret.add(new Confirmation(confID, confKey, confType, confCreator));
        }
        return ret;
    }

    /**
     * Accept Steam Guard Confirmation
     * @param conf Confirmation instance
     * @return true if confirmed successfully
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean acceptConfirmation(Confirmation conf) throws IOException, InterruptedException {
        return sendConfirmationAjax(conf, "allow");
    }

    /**
     * Decline Steam Guard Confirmation
     * @param conf Confirmation instance
     * @return whether confirmation was declined successfully
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean denyConfirmation(Confirmation conf) throws IOException, InterruptedException {
        return sendConfirmationAjax(conf, "cancel");
    }

    private boolean sendConfirmationAjax(Confirmation conf, String op)
            throws IOException, InterruptedException {
        String url = EndPoints.COMMUNITY_BASE + "/mobileconf/ajaxop";
        String queryString = "?op=" + op + "&";
        queryString += generateConfirmationQueryParams(op);
        queryString += "&cid=" + conf.getID() + "&ck=" + conf.getKey();
        url += queryString;

        String referer = generateConfirmationURL();

        String cookiesString = Cookies_Handler.getCookieStringFromMap(cookies);
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", cookiesString);
        headers.put("Referer", referer);
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Google Nexus 4 - 4.1.1 - API 16 - 768x1280 Build/JRO03S) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        headers.put("Accept", "text/javascript, text/html, application/xml, text/xml, */*");
        headers.put("Origin", "https://steamcommunity.com");
        HttpRequest request = HttpRequestBuilder.build(url, headers, HttpRequestBuilder.RequestType.GET, "");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

//        ColorToTerminal.printYELLOW(Integer.toString(response.statusCode()));
//        ColorToTerminal.printYELLOW(response.headers().toString());
//        ColorToTerminal.printYELLOW(response.body());
        return response.statusCode() == 200;
    }

    private String generateConfirmationURL(String tag)
            throws IOException, InterruptedException {
        String endpoint = EndPoints.COMMUNITY_BASE + "/mobileconf/conf?";
        String queryString = generateConfirmationQueryParams(tag);
        return endpoint + queryString;
    }

    private String generateConfirmationURL()
            throws IOException, InterruptedException {
        return generateConfirmationURL("conf");
    }

    private String generateConfirmationQueryParams(String tag)
            throws IOException, InterruptedException {
        if(Objects.equals(deviceID, null))
            throw new IllegalArgumentException("Device ID is not present(null)");

        Map<String, String> queryParams = generateConfirmationQueryParamsAsNVC(tag);

        return "p=" + queryParams.get("p")
                + "&a=" + queryParams.get("a")
                + "&k=" + queryParams.get("k")
                + "&t=" + queryParams.get("t")
                + "&m=android&tag=" + queryParams.get("tag");
    }

    private Map<String, String> generateConfirmationQueryParamsAsNVC(String tag)
            throws IOException, InterruptedException {
        if (Objects.equals(deviceID, null))
            throw new IllegalArgumentException("Device ID is not present(null)");

        Long time = TimeAligner.getSteamTime(client);

        Map<String, String> ret = new HashMap<>();
        ret.put("p", deviceID);
        ret.put("a", this.cookies.get("steamid")); //----Check
        ret.put("k", generateConfirmationHashForTime(time, tag));
        ret.put("t", time.toString());
        ret.put("m", "android");
        ret.put("tag", tag);

        return ret;
    }

    private String generateConfirmationHashForTime(long time, String tag)
            throws UnsupportedEncodingException {

        byte[] decode = Base64.getDecoder().decode(UserDetails.IDENTITYSECRET);

        int n2 = 8;
        if (tag != null) {
            if (tag.length() > 32) {
                n2 = 8 + 32;
            }
            else {
                n2 = 8 + tag.length();
            }
        }
        byte[] array = new byte[n2];
        int n3 = 8;
        while (true) {
            int n4 = n3 - 1;
            if (n3 <= 0) {
                break;
            }
            array[n4] = (byte)time;
            time >>= 8;
            n3 = n4;
        }
        if (tag != null) {

            byte[] copyArray = Arrays.copyOfRange(tag.getBytes(StandardCharsets.UTF_8), 0, n2-8);
            for(int i = 0; i < copyArray.length; i++) {
                array[i+8] = copyArray[i];
            }
        }

        try {

            byte[] hashedData = HmacSHA1_Handler.calculateHMACSHA1(array,decode);

            String encodedData = Base64.getEncoder().encodeToString(hashedData);

            String hash = URLEncoder.encode(encodedData, StandardCharsets.UTF_8);
            return hash;
        }
        catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class WGTokenInvalidException extends Exception{
        WGTokenInvalidException(){
            super("No Internal confirmations to fetch.");
        }
    }
}
