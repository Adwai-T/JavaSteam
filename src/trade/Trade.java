package trade;

import steamapi.Steam_TradeAPI;
import trade.classes.Item;
import trade.classes.TradeOffer;
import utils.ColorToTerminal;
import utils.TimeStamp_Handler;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

//TODO : Retry if the trade offer save fails and if it still fails save to local disc and retry after some time.

public class Trade implements Runnable {

    public Trade() {}

    private void trade(HttpClient client, TradeOffer_DataBase db, Map<String, String> cookies) {
        try{
            if(Steam_TradeAPI.hasNewTradeOffers(client)) {



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

    //TODO : Implement checkTradeValue Method
    private static boolean checkTradeValue(TradeOffer offer) {
        long valueItemsToReceive = 0;
        for(Item item : offer.items_to_receive) {

        }

        long valueItemsToGive = 0;
        for(Item item : offer.items_to_give){

        }

        if(valueItemsToGive <= valueItemsToReceive) {
            return true;
        }

        return false;
    }

    @Override
    public void run() {

    }
}
