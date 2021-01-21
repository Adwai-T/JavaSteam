package utils;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Map;

public class HttpRequestBuilder {

    /**
     * Build A HttpRequest and return
     *
     * @param url url String.
     * @param headers     Map of key value pair of headers.
     * @param requestType RequestType Enum eg GET, POST
     * @param body        String value of body to be send ('null' for nobody)
     * @return HttpRequest
     */
    public static HttpRequest build(String url, Map<String, String> headers, RequestType requestType, String body) {

        HttpRequest.Builder request = HttpRequest.newBuilder();

        request.uri(URI.create(url));

        for (String header : headers.keySet()) {
            request.header(header, headers.get(header));
        }

        switch (requestType) {
            case GET:
                request.GET();
                break;
            case POST:
                if (body != null) {
                    request.POST(HttpRequest.BodyPublishers.ofString(body));
                } else request.POST(HttpRequest.BodyPublishers.noBody());
                break;
        }

        return request.build();
    }

    public enum RequestType {
        GET,
        POST,
    }

    /**
     *
     * @param url String url
     * @param headers Map of key value pair of headers.
     * @param requestType RequestType Enum eg GET, POST
     * @param body  String value of body to be send ('null' for nobody)
     * @param urlencoded_params all url parameters url encoded in a single string
     * @return HttpRequest
     */
    public static HttpRequest build(String url, Map<String, String> headers, RequestType requestType, String body, String urlencoded_params) {
        return build(url + "?" + urlencoded_params, headers, requestType, body);
    }
}
