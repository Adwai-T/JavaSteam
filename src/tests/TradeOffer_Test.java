package tests;

import com.mongodb.client.MongoClient;
import steamapi.Steam_TradeAPI;
import trade.TradeOffer_DataBase;
import trade.classes.Item;
import trade.classes.TradeOffer;
import utils.ColorToTerminal;
import utils.GetUserInputFromTerminal;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TradeOffer_Test {

    private HttpClient client;
    private List<TradeOffer>offers;

    public TradeOffer_Test (HttpClient client, String timestamp) {
        this.client = client;
        try{
            this.offers = Steam_TradeAPI.getActiveReceivedTradeOffers(client, timestamp);
            int index = 0;
            for(TradeOffer offer : offers) {
                System.out.println(index + " : "
                        + offer.tradeofferid + " -> "
                        + offer.partnerId.steamId64 + "->"
                        + offer.trade_offer_state + "\n\t"
                );

                index++;
            }
        }catch(Exception e){
            System.out.println("Could not get trade Description. " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void checkItemsInTradeOffer() throws IOException, InterruptedException {
        System.out.println("Please Enter the index of trade offer to check items.");
        Integer index = Integer.parseInt(GetUserInputFromTerminal.getString());
        if(index > offers.size() - 1 || index < 0) {
            System.out.println("Please Enter a valid index");
            checkItemsInTradeOffer();
        } else {
            TradeOffer offer = offers.get(index);

            List<Item> list = new ArrayList<>(offer.items_to_give);
            list.addAll(offer.items_to_receive);

            Item.fetchDescriptionForItems(client, list);

            System.out.println("Items To Give : ");
            for(Item item : offer.items_to_give){
                System.out.println(item.toJSONString());
            }

            System.out.println("Items we will receive : ");
            for(Item item : offer.items_to_receive){
                System.out.println(item.toJSONString());
            }
        }
    }

    public void acceptTradeOfferTest(Map<String, String>cookies) throws IOException, InterruptedException {
        System.out.println("Please Enter the trade offer index to be accept offer.");
        Integer index = Integer.parseInt(GetUserInputFromTerminal.getString());
        if(index > offers.size() - 1 || index < 0) {
            System.out.println("Please Enter a valid index");
            acceptTradeOfferTest(cookies);
        } else {
            offers.get(index).acceptTradeOffer(client, cookies);
        }
    }

    public void savetradeOffer(MongoClient client, TradeOffer_DataBase db) {
        System.out.println("Please Enter the index of trade offer to save to database.");
        Integer index = Integer.parseInt(GetUserInputFromTerminal.getString());
        if(index > offers.size() - 1 || index < 0) {
            System.out.println("Please Enter a valid index");
            savetradeOffer(client, db);
        } else {
            TradeOffer offer = offers.get(index);
            db.saveTradeOffer(offer);
            System.out.println(ColorToTerminal.ANSI_BLUE + "Saved Trade Offer to DB" + ColorToTerminal.ANSI_RESET);
        }
    }
}
