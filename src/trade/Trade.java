package trade;

import org.bson.Document;
import steamapi.Steam_TradeAPI;
import trade.classes.Item;
import trade.classes.TradeOffer;
import trade.enums.ETradeOfferState;
import utils.ColorToTerminal;
import utils.TimeStamp_Handler;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//TODO : Retry if the trade offer save fails and if it still fails save to local disc and retry after some time.

public class Trade implements Runnable {

    private List<Document> accept;//List of items of known value in database
    private HttpClient client;
    private TradeOffer_DataBase db;
    Map<String, String> cookies;

    public Trade(HttpClient client, TradeOffer_DataBase db, Map<String, String> cookies) {
        this.client = client;
        this.db = db;
        this.cookies = cookies;
    }

    private void trade() {
        try{
            if(Steam_TradeAPI.hasNewTradeOffers(client)) {

                accept = db.getTradesList();

                Long tradeFetchTime = TimeStamp_Handler.getCurrentTimeStamp() - 12 * TimeStamp_Handler.OneHour;
                List<TradeOffer> offers;
                offers = Steam_TradeAPI.getActiveReceivedTradeOffers(
                        client,
                        tradeFetchTime.toString()
                        );

                for(TradeOffer offer : offers) {

                    if(offer.trade_offer_state != ETradeOfferState.Active) {
                        continue;
                    }

                    if(TradeOffer.recentlyAcceptedTradeOffers.contains(offer.tradeofferid)) {
                        continue;
                    }

                    List<Item> items = new ArrayList<>(offer.items_to_give);
                    items.addAll(offer.items_to_receive);

                    Item.fetchDescriptionForItems(client, items);

                    if(checkTradeValue(offer)){
                        if(offer.accept(client, cookies)) {
                            db.saveTradeOffer(offer, "Accepted");
                            System.out.println(ColorToTerminal.ANSI_CYAN
                                    + "Trade Offer Accepted and Saved to DB"
                                    + ColorToTerminal.ANSI_RESET
                            );
                        }
                    }else {
                        if(offer.decline(client)) {
                            db.saveTradeOffer(offer, "Declined");
                            ColorToTerminal.printCYAN("TradeOffer Declined, and Saved to DB");
                        }

                    }
                }
            }
        }catch (IOException io) {
            System.out.println(ColorToTerminal.ANSI_RED + io.getMessage() + ColorToTerminal.ANSI_RESET);
//            io.printStackTrace();
        }catch (InterruptedException ie) {
            System.out.println(ColorToTerminal.ANSI_RED + ie.getMessage() + ColorToTerminal.ANSI_RESET);
//            io.printStackTrace();
        }
    }

    /**
     * While checking trade value we first hard compare the item to find if we have an exactly matching item.
     * If we find the item we buy or sell at the price of the item.
     * But if we do not find the item in hard compare, we will soft compare the item only if we are buying the item,
     * and set the price to the closest match that is available.
     * @param offer TradeOffer
     * @return If the value of trade is fair.
     */
    private boolean checkTradeValue(TradeOffer offer) {
        Integer valueItemsToReceive = Integer.valueOf(0);
        List<Item> itemsNotFoundFirst = new ArrayList<>();
        for(Item item : offer.items_to_receive) {

            boolean foundItemInAccept = false;
            for(Document doc : accept) {
                if(item.compareItemToDocument(doc, TradeOperation.SELL)) {
                    valueItemsToReceive += (Integer)doc.get("BuyAt");
                    foundItemInAccept = true;
                }
            }
            if(!foundItemInAccept) {
                itemsNotFoundFirst.add(item);
            }
        }
        for(Item item : itemsNotFoundFirst) {
            for(Document doc : accept) {
                if(item.compareItemToDocument(doc, TradeOperation.BUY)) {
                    valueItemsToReceive += (Integer)doc.get("BuyAt");
                }
            }
        }

        Integer valueItemsToGive = 0;
        for(Item item : offer.items_to_give){
            for(Document doc : accept) {
                if(item.compareItemToDocument(doc, TradeOperation.SELL)) {
                    valueItemsToGive += (Integer)doc.get("SellAt");
                }
            }
        }

//        System.out.println("Item to give value : " + valueItemsToGive);
//        System.out.println("Item to receive value : " + valueItemsToReceive);

        if(valueItemsToGive > 0 && valueItemsToReceive > 0 && valueItemsToGive <= valueItemsToReceive) {
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        trade();
    }

    public enum TradeOperation {
        BUY,
        SELL
    }
}
