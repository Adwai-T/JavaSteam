package steamapi;

import login.UserDetails;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import trade.classes.Item;
import trade.classes.TradeOffer;
import utils.ColorToTerminal;
import utils.Form_UrlEncoder;
import utils.HttpRequestBuilder;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Steam_TradeAPI {

    public static boolean hasNewTradeOffers(HttpClient client) throws IOException, InterruptedException {
        try {
            JSONObject response = (JSONObject) JSONValue.parse(
                    getTradeOfferSummary(client).body());

            JSONObject res = (JSONObject) response.get("response");
            if((Long)res.get("pending_received_count") > 0) {
                return true;
            }else {
                return false;
            }

        }catch (Exception e){
            System.out.println(ColorToTerminal.ANSI_RED + "Failed to Get Trade Offer Summary");
            System.out.println(e.getMessage() + ColorToTerminal.ANSI_RESET);
            e.printStackTrace();
            hasNewTradeOffers(client);
            return false;
        }
    }

    private static HttpResponse<String> getTradeOfferSummary(HttpClient client) throws IOException, InterruptedException {
        String url = "https://api.steampowered.com/IEconService/GetTradeOffersSummary/v1";

        Map<String, String> params = new HashMap<>();
        params.put("key", UserDetails.APIKEY);
        params.put("language", "english");
        String urlEncodedParams = Form_UrlEncoder.encode(params);

        Map<String, String> headers = new HashMap<>();

        HttpRequest request = HttpRequestBuilder.build(url, headers, HttpRequestBuilder.RequestType.GET, null, urlEncodedParams);

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> fetchActiveTradeOffers(HttpClient client, String cutoffTime) throws IOException, InterruptedException {
        String url = "https://api.steampowered.com/IEconService/GetTradeOffers/v1";

        Map<String, String> params = new HashMap<>();
        params.put("key", UserDetails.APIKEY);
        params.put("get_sent_offers", "true");
        params.put("get_received_offers", "true");
        params.put("get_descriptions", "false");
        params.put("language", "english");
        params.put("active_only", "true");
        params.put("historical_only", "false");
        params.put("time_historical_cutoff", cutoffTime);
        String urlEncodedParams = Form_UrlEncoder.encode(params);

        Map<String, String> headers = new HashMap<>();

        HttpRequest request = HttpRequestBuilder.build(url, headers, HttpRequestBuilder.RequestType.GET, null, urlEncodedParams);

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static List<TradeOffer> getActiveReceivedTradeOffers(HttpClient client, String cutoffTime) throws IOException, InterruptedException {
        HttpResponse<String> response = fetchActiveTradeOffers(client, cutoffTime);
        List<TradeOffer> tradeOffers = new ArrayList<>();

        JSONObject object = (JSONObject) JSONValue.parse(response.body());
        JSONObject resp = (JSONObject) object.get("response");
        JSONArray tradeReceived = (JSONArray) resp.get("trade_offers_received");

        for(Object trade : tradeReceived){
            JSONObject tradeofferObject = (JSONObject) trade;
            TradeOffer tradeOffer =  TradeOffer.parseTradeOfferFromJSON(tradeofferObject);
            tradeOffers.add(tradeOffer);
        }

        return tradeOffers;
    }
    
    public static HttpResponse<String> fetchItemsDescription(HttpClient client, List<Item> items) throws IOException, InterruptedException {

        if(items.size() <= 0) {
            return null;
        }

        String url = "https://api.steampowered.com/ISteamEconomy/GetAssetClassInfo/v1";

        Map<String, String> params = new HashMap<>();
        params.put("key", UserDetails.APIKEY);
        params.put("appid", items.get(0).appid.toString());
        params.put("language", "english");

        params.put("class_count", Integer.toString(items.size()));
        for(Integer i = 0; i < items.size(); i++) {
            params.put("classid"+i.toString(), items.get(i).classid);
            params.put("instanceid"+i.toString(), items.get(i).instanceid);
        }
        String paramsString = Form_UrlEncoder.encode(params);

        Map<String, String> headers = new HashMap<>();

        HttpRequest request = HttpRequestBuilder.build(
                url, headers, HttpRequestBuilder.RequestType.GET, null, paramsString);

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> declineTradeOffer(HttpClient client, TradeOffer offer) throws IOException, InterruptedException {
        String url = "https://api.steampowered.com/IEconService/DeclineTradeOffer/v1";

        Map<String, String> params = new HashMap<>();
        params.put("key", UserDetails.APIKEY);
        params.put("tradeofferid", offer.tradeofferid);
        String paramsString = Form_UrlEncoder.encode(params);

        Map<String, String> headers = new HashMap<>();

        HttpRequest request = HttpRequestBuilder.build(
                url, headers, HttpRequestBuilder.RequestType.POST, null, paramsString);

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

}
