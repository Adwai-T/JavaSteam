package login.guard;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import utils.HttpRequestBuilder;
import utils.TimeStamp_Handler;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

public class TimeAligner {

    private static boolean aligned = false;
    private static Long timeDifference = 0L;

    public static long getSteamTime(HttpClient client) throws IOException, InterruptedException {
        if (!TimeAligner.aligned) {
            TimeAligner.alignTime(client);
        }
        return TimeStamp_Handler.getCurrentTimeStamp() + timeDifference;
    }

    private static void alignTime(HttpClient client) throws IOException, InterruptedException {
        long currentTime = TimeStamp_Handler.getCurrentTimeStamp();

        HttpRequest request = HttpRequestBuilder.build(
                EndPoints.TWO_FACTOR_TIME_QUERY,
                new HashMap<String, String>(),
                HttpRequestBuilder.RequestType.POST,
                "steamid=0"
                );
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject resJSON = (JSONObject) JSONValue.parse(response.body());
        JSONObject responseJSON = (JSONObject) resJSON.get("response");
        Long serverTime = Long.parseLong((String) responseJSON.get("server_time"));

        timeDifference = serverTime - currentTime;
        aligned = true;
    }
}
