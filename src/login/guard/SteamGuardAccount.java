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
    private HttpClient client;
    private static byte[] steamGuardCodeTranslations = new byte[] { 50, 51, 52, 53, 54, 55, 56, 57, 66, 67, 68, 70, 71, 72, 74, 75, 77, 78, 80, 81, 82, 84, 86, 87, 88, 89 };
    private String deviceID = "android:fdba102f-5e2d-4af6-92d7-27e11448044b";
    private String referer = EndPoints.COMMUNITY_BASE + "/mobilelogin?oauth_client_id=DE45CD61&oauth_scope=read_profile%20write_profile%20read_client%20write_client";

    //Set to true if the authenticator has actually been applied to the account.
    private boolean FullyEnrolled;
    private Map<String, String> cookies;

    public SteamGuardAccount(HttpClient client, Map<String, String> cookies) {
        this.client = client;
        this.cookies = cookies;
        this.sharedSecret = UserDetails.SHAREDSECRET;
    }

    public String generateSteamGuardCode()
            throws IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeyException {
        return generateSteamGuardCodeForTime(TimeAligner.getSteamTime(client));
    }

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
        catch (Exception e)
        {
            return null; //Change later, catch-alls are bad!
        }
        return new String(codeArray, "UTF-8");
    }

//    public boolean deactivateAuthenticator() {
//        return deactivateAuthenticator(2);
//    }

//    public boolean deactivateAuthenticator(Integer scheme)
//    {
//        Map<String, String> postData = new HashMap<>();
//        postData.put("steamid", this.Session.SteamID.ToString());
//        postData.put("steamguard_scheme", scheme.toString());
//        postData.put("revocation_code", this.RevocationCode);
//        postData.put("access_token", this.Session.OAuthToken);
//
//        try
//        {
//            String response = SteamWeb.MobileLoginRequest(EndPoints.STEAMAPI_BASE + "/ITwoFactorService/RemoveAuthenticator/v0001", "POST", postData);
////            var removeResponse = JsonConvert.DeserializeObject<RemoveAuthenticatorResponse>(response);
//
//            if (removeResponse == null || removeResponse.Response == null || !removeResponse.Response.Success) return false;
//            return true;
//        }
//        catch (Exception)
//        {
//            return false;
//        }
//    }

    public List<Confirmation> fetchConfirmations()
            throws IOException, InterruptedException, WGTokenInvalidException {
        String url = generateConfirmationURL();

//        Map<String, String> cookies = new HashMap<>();
//        this.Session.AddCookies(cookies);

//        String response = SteamWeb.Request(url, "GET", "", cookies);
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

        ColorToTerminal.printPURPLE(Integer.toString(response.statusCode()));
        ColorToTerminal.printPURPLE(response.headers().toString());
        ColorToTerminal.printPURPLE(response.body());

        return FetchConfirmationInternal(response.body());
    }

    private List<Confirmation> FetchConfirmationInternal(String response) throws WGTokenInvalidException {

//          Regex for HTML -- while awful -- makes this way faster than parsing a DOM,
//          plus we don't need another library.
//          And because the data is always in the same place and same format...
//          It's not as if we're trying to naturally understand HTML here. Just extract strings.
        List<List<String>> confirmations = Regex_Handler.getAllMatches(
                response,
                "<div class=\"mobileconf_list_entry\" id=\"conf[0-9]+\" data-confid=\"(\\d+)\" data-key=\"(\\d+)\" data-type=\"(\\d+)\" data-creator=\"(\\d+)\"");

        if (response == null || confirmations.size() == 0) {
            if (response == null || !response.matches("<div>Nothing to confirm</div>")) {
                throw new WGTokenInvalidException();
            }
            return new ArrayList<>();
        }

//        MatchCollection confirmations = confRegex.Matches(response);
//        List<Confirmation> ret = new List<Confirmation>();
//        foreach (Match confirmation in confirmations)
//        {
//            if (confirmation.Groups.Count != 5) continue;

        List<Confirmation> ret = new ArrayList<>();
        for (List<String> confirmation : confirmations) {

            //I know this looks weird but it is to confirm that they are numbers as we need
            Long confID = null;
            Long confKey = null;
            Long confType = null;
            Long confCreator = null;

            //If our confirmation does not have all the five variable groups matched
            if (confirmations.size() != 5) continue;
            if (
                    !tryParse(confirmation.get(1), confID) ||
                    !tryParse(confirmation.get(2), confKey) ||
                    !tryParse(confirmation.get(3), confType) ||
                    !tryParse(confirmation.get(4), confCreator)
                )
            {
                ColorToTerminal.printBGPURPLE("Continued in Addd --->");
                continue;
            }
            ret.add(new Confirmation(
                    confID.toString()
                    , confKey.toString()
                    , confType.toString()
                    , confCreator.toString())
            );
        }
        return ret;
    }

    private boolean tryParse(String value, Long setValueOf) {
        try {
            setValueOf = Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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
//        byte[] decode = Convert.FromBase64String(this.IdentitySecret);
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

//            Array.Copy(Encoding.UTF8.GetBytes(tag), 0, array, 8, n2 - 8);
            //arrayToCopy, startIndex, destArray, destArrayIndexStart, numberOfElements
            byte[] copyArray = Arrays.copyOfRange(tag.getBytes("UTF-8"), 0, n2-8);
            for(int i = 0; i < copyArray.length; i++) {
                array[i+8] = copyArray[i];
            }
        }

        try {
//            HMACSHA1 hmacGenerator = new HMACSHA1();
//            hmacGenerator.Key = decode;
//            byte[] hashedData = hmacGenerator.ComputeHash(array);
            byte[] hashedData = HmacSHA1_Handler.calculateHMACSHA1(array,decode);
//            String encodedData = Convert.ToBase64String(hashedData, Base64FormattingOptions.None);
            String encodedData = Base64.getEncoder().encodeToString(hashedData);
//            String hash = WebUtility.UrlEncode(encodedData);
            String hash = URLEncoder.encode(encodedData, StandardCharsets.UTF_8);
            return hash;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class WGTokenInvalidException extends Exception{
        WGTokenInvalidException(){
            super("No Internal confirmations to fetch.");
        }
    }
}
