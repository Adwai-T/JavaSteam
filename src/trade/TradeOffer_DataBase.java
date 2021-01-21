package trade;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import trade.classes.TradeOffer;
import utils.ColorToTerminal;

import java.util.ArrayList;
import java.util.List;

public class TradeOffer_DataBase {

    private MongoClient client;
    private MongoDatabase db;

    public TradeOffer_DataBase (MongoClient client) {
        if(client != null) {
            this.client = client;
            db = this.client.getDatabase("Steam");
        }else System.out.println(ColorToTerminal.ANSI_RED + "You have not added a link to MongoDB, local file will be used." + ColorToTerminal.ANSI_RESET);
    }

    public FindIterable<Document> updateTradesList() {
        MongoCollection<Document> update = db.getCollection("Accept");
        return update.find();
    }

    public boolean saveTradeOffer(TradeOffer offer){
        MongoCollection<Document> offers = db.getCollection("Offers");
        offers.insertOne(offer.toBsonDocument());
        return true;
    }

    public boolean saveTradeOffers(List<TradeOffer> tradeOffers) {
        MongoCollection<Document> offers = db.getCollection("Offers");
        try {
            List<Document> offerDocs = new ArrayList<>();
            for(TradeOffer offer : tradeOffers) {
                offerDocs.add(offer.toBsonDocument());
            }
            offers.insertMany(offerDocs);
        }catch (Exception e) {
            System.out.println(ColorToTerminal.ANSI_RED + "Database tradeoffer Save failed.");
            System.out.println(e.getMessage() + ColorToTerminal.ANSI_RESET);
            return false;
        }

        return false;
    }
}
