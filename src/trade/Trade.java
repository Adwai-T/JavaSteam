package trade;

import org.bson.Document;
import steamapi.Steam_TradeAPI;
import trade.classes.Item;
import trade.classes.TradeOffer;
import utils.ColorToTerminal;
import utils.TimeStamp_Handler;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//TODO : Retry if the trade offer save fails and if it still fails save to local disc and retry after some time.

public class Trade implements Runnable {

    private List<Document> accept;

    public Trade() {}

    private void trade(HttpClient client, TradeOffer_DataBase db, Map<String, String> cookies) {
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
                    if(checkTradeValue(offer)){
                        if(offer.accept(client, cookies)) {
                            db.saveTradeOffer(offer);
                        }
                    }else {
                        offer.decline();
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

    private boolean checkTradeValue(TradeOffer offer) {
        Long valueItemsToReceive = Long.valueOf(0);
        List<Item> itemsNotFoundFirst = new ArrayList<>();
        for(Item item : offer.items_to_receive) {
            boolean foundItemInAccept = false;
            for(Document doc : accept) {
                if(item.compareItemToDocument(doc, TradeOperation.SELL)) {
                    valueItemsToReceive += (Long)doc.get("BuyAt");
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
                    valueItemsToReceive += (Long)doc.get("BuyAt");
                }
            }
        }

        long valueItemsToGive = 0;
        for(Item item : offer.items_to_give){
            for(Document doc : accept) {
                if(item.compareItemToDocument(doc, TradeOperation.SELL)) {
                    valueItemsToGive += (Long)doc.get("SellAt");
                }
            }
        }

        if(valueItemsToGive <= valueItemsToReceive) {
            return true;
        }

        return false;
    }

    @Override
    public void run() {

    }

    public enum TradeOperation {
        BUY,
        SELL
    }
}
