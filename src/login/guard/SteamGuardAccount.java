package login.guard;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.http.HttpClient;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SteamGuardAccount {

    private final String sharedSecret;
    private static byte[] steamGuardCodeTranslations = new byte[] { 50, 51, 52, 53, 54, 55, 56, 57, 66, 67, 68, 70, 71, 72, 74, 75, 77, 78, 80, 81, 82, 84, 86, 87, 88, 89 };

    public SteamGuardAccount(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public String generateSteamGuardCode(HttpClient client)
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
}
