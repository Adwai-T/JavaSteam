package trade.classes;

import login.SteamID;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import steamapi.Steam_TradeAPI;
import trade.enums.ETradeOfferState;
import utils.Cookies_Handler;
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

public class TradeOffer {

    public static ArrayList<String> recentlyAcceptedTradeOffers = new ArrayList<>();

    //Names Of all variables are kept the same as those in the json reply.
    public String tradeofferid;
    public String accountid_other; //Partner account 32Bit\
    public SteamID partnerId;
    public String message;
    public String expiration_time; //Epoch time when trade offer expires
    public ETradeOfferState trade_offer_state; //Refer ETradeOfferState
    public List<Item> items_to_give; //Items we give.
    public List<Item> items_to_receive; //Items we will get.
    public Boolean is_our_offer; //If we had originally send this offer that they have countered.
    // In any case the offer should be checked again before accepting.
    public Long time_created;
    public Long time_updated;
    public Boolean from_real_time_trade; //Should be false.
    public Long escrow_end_date; //Should always be "0" or the items will be stuck in escrow.
    public Boolean hasEscrowTime; //If there is a wait time till we get our items after trade completes.
    public int confirmation_method;//Only necessary for the trade that are already completed. "0" for pending trade offers.

    public TradeOffer() {}

    //TODO : Add mobile Authentication confirmation so the trade is accepted completely here.
    /**
     * Accept Trade offer.
     * @param client HttpClient
     * @param cookies The Cookies Map.
     * @return boolean, True if trade accept was successful.
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean accept(HttpClient client, Map<String, String> cookies) throws IOException, InterruptedException {

        if(this.trade_offer_state != ETradeOfferState.Active) {
            return false;
        }

        String url = "https://steamcommunity.com/tradeoffer/" + this.tradeofferid + "/accept";

        Map<String, String> body = new HashMap<>();
        body.put("sessionid", cookies.get("sessionid"));
        body.put("serverid", "1");
        body.put("tradeofferid", this.tradeofferid);
        body.put("partner", this.partnerId.steamId64);
        String bodyString = Form_UrlEncoder.encode(body);

        String cookiesString = Cookies_Handler.getCookieStringFromMap(cookies);
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", cookiesString);
        headers.put("Referer", "https://steamcommunity.com/tradeoffer/" + this.tradeofferid);
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");
        headers.put("Accept", "*/*");
        headers.put("Origin", "https://steamcommunity.com");

        HttpRequest request = HttpRequestBuilder.build(url, headers, HttpRequestBuilder.RequestType.POST,bodyString);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() == 200) {
            recentlyAcceptedTradeOffers.add(this.tradeofferid);
            return true;
        }
        return false;
    }

    /**
     * Decline this trade offer.
     * @param client
     * @return true if successful
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean decline(HttpClient client) throws IOException, InterruptedException {
        int status = Steam_TradeAPI.declineTradeOffer(client, this).statusCode();
        if(status == 200) {
            return true;
        }
        return false;
    }

    /**
     * Parse trade offer from the Json Response that we get from steam API.
     * @param jsonTradeOffer
     * @return TradeOffer instance
     */
    public static TradeOffer parseTradeOfferFromJSON(JSONObject jsonTradeOffer) {
        TradeOffer tradeOffer = new TradeOffer();
        tradeOffer.tradeofferid = (String) jsonTradeOffer.get("tradeofferid");
        tradeOffer.accountid_other = ((Long) jsonTradeOffer.get("accountid_other")).toString();
        tradeOffer.message = (String) jsonTradeOffer.get("message");
        tradeOffer.expiration_time = ((Long) jsonTradeOffer.get("expiration_time")).toString();
        tradeOffer.trade_offer_state = ETradeOfferState.getStringValue(
                ((Long) jsonTradeOffer.get("trade_offer_state")).intValue());
        tradeOffer.is_our_offer = (Boolean) jsonTradeOffer.get("is_our_offer");
        tradeOffer.time_created = (Long) jsonTradeOffer.get("time_created");
        tradeOffer.time_updated = (Long) jsonTradeOffer.get("time_updated");
        tradeOffer.from_real_time_trade = (Boolean) jsonTradeOffer.get("from_real_time_trade");
        tradeOffer.escrow_end_date = (Long) jsonTradeOffer.get("escrow_end_date");
        tradeOffer.confirmation_method = ((Long) jsonTradeOffer.get("confirmation_method")).intValue();
        tradeOffer.setHasEscrowTime((Long) jsonTradeOffer.get("escrow_end_date"));

        tradeOffer.items_to_give = parseItemList((JSONArray) jsonTradeOffer.get("items_to_give"));
        tradeOffer.items_to_receive = parseItemList((JSONArray) jsonTradeOffer.get("items_to_receive"));

        tradeOffer.partnerId = new SteamID(tradeOffer.accountid_other, SteamID.AccountType.U, SteamID.AccountUniverse.Public);
        return tradeOffer;
    }

    private static List<Item> parseItemList(JSONArray tradeOfferArray) {
        List<Item> items = new ArrayList<>();

        for(Object object : tradeOfferArray) {
            JSONObject jsonItem = (JSONObject) object;
            Item item = new Item(
                    (Long)jsonItem.get("appid"),
                    (String)jsonItem.get("contextid"),
                    (String)jsonItem.get("assetid"),
                    (String)jsonItem.get("classid"),
                    (String)jsonItem.get("instanceid"),
                    (String)jsonItem.get("amount"),
                    (Boolean)jsonItem.get("missing")
                    );
            items.add(item);
        }
        return items;
    }

    private void setHasEscrowTime(Long time){
        if(time == 0) hasEscrowTime = false;
        else hasEscrowTime = true;
    }

    /**
     * Returns this Item as a Document(BSONDocument) that can be saved to MongoDB.
     * @return org.bson.Document
     */
    public Document toBsonDocument() {
        Document doc = new Document();
        doc.append("ID", tradeofferid);
        doc.append("Partner",partnerId.toString());
        doc.append("Other Account", accountid_other);
        doc.append("Message", message);
        doc.append("Is our Offer", is_our_offer.toString());
        doc.append("Time Created", time_created);
        doc.append("Time Updated", time_updated);

        List<Document> itemsToGive = new ArrayList<>();
        for(Item item : items_to_give) {
            itemsToGive.add(item.toBsonDocument());
        }
        doc.append("Items To Give", itemsToGive);

        List<Document> itemsToReceive = new ArrayList<>();
        for(Item item : items_to_receive) {
            itemsToReceive.add(item.toBsonDocument());
        }
        doc.append("Items To Receive", itemsToReceive);

        return doc;
    }
}
