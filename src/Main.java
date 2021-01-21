import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import login.Login;
import login.UserDetails;
import org.json.simple.parser.ParseException;
import tests.SteamId_Test;
import trade.TradeOffer_DataBase;
import utils.ColorToTerminal;
import utils.Files_Handler;
import utils.GetUserInputFromTerminal;
import utils.TimeStamp_Handler;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.HashMap;

//TODO : Add threads to parallel fetch data.

//TODO : Implement local file support for TradeOffers.

public class Main {

    public final static HttpClient client = HttpClient.newHttpClient();
    public HashMap<String, String> cookies;
    public static MongoClient db_client;
    public static TradeOffer_DataBase db;
    public static ArrayList<String> trades;

    public Main(){

        // Bot Start
        System.out.println(ColorToTerminal.ANSI_GREEN + "Bot Started" + ColorToTerminal.ANSI_RESET);

        //Initialize MongoClient to send and get trade offer details.
        if(UserDetails.MONGODBLINK != null) {
            db_client = MongoClients.create(UserDetails.MONGODBLINK);
            db = new TradeOffer_DataBase(db_client);
        }
        db_client = null;

        //trades from database that have selling and buying price for items.
        trades = new ArrayList<>();

        //This step is necessary before performing any tests
        getAuthenticationDetails();
        addCommonCookiesToMap();

        //Tests
        //Do the test after login or after we have our authentication details. As all test depend on them.
        try{
            SteamId_Test id_test = new SteamId_Test();

            Long timestamp = TimeStamp_Handler.getCurrentTimeStamp() - 72 * TimeStamp_Handler.OneHour;

//            TradeOffer_Test tradeOffer_test = new TradeOffer_Test(client, timestamp.toString());
//            tradeOffer_test.checkItemsInTradeOffer();
//            tradeOffer_test.savetradeOffer(db_client, db);
//            tradeOffer_test.acceptTradeOfferTest(cookies);

//            LocalDatabase_Test db_test = new LocalDatabase_Test(db_client);

        }catch (Exception e) {
            System.out.println("One Of the Test failed");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        //Bot Terminates
        System.out.println(ColorToTerminal.ANSI_RED + "Bot ShutDown" + ColorToTerminal.ANSI_RESET);
    }

    public static void main(String[] args) {
        Main main = new Main();
    }

    private static int selectOption(){
        System.out.println(ColorToTerminal.ANSI_GREEN_BACKGROUND + ColorToTerminal.ANSI_BLACK + "Please Select One of the numbers");
        System.out.println(ColorToTerminal.ANSI_RESET + ColorToTerminal.ANSI_PURPLE);
        System.out.println("1 -> Login");
        System.out.println("2 -> Use Saved Auth File");
        System.out.println("3 -> Exit");
        System.out.println(ColorToTerminal.ANSI_RESET);
        return Integer.parseInt(GetUserInputFromTerminal.getString());
    }

    private void getAuthenticationDetails() {
        int selectedOption = selectOption();
        if(selectedOption == 1) {
            Login login = new Login(client);
            cookies = login.getCookies();
        }else if(selectedOption == 2) {
            try{
                cookies = Files_Handler.readJSONFromFile(new File("Auth.json"));
            }catch(IOException e) {
                System.out.println(ColorToTerminal.ANSI_RED + "Could not find/read the Auth file." + ColorToTerminal.ANSI_RESET);
            }catch(ParseException e) {
                System.out.println(ColorToTerminal.ANSI_RED + "Could not parse Auth data from file." + ColorToTerminal.ANSI_RESET);
            }
        }
    }

    private void addCommonCookiesToMap() {
        cookies.put("Steam_Language", "english");
        cookies.put("timezoneOffset", "19800");// This is timezoneOffset for india.
        cookies.put("bCompletedTradeOfferTutorial", "true");
        cookies.put("sessionid", Login.generateSessionId());
    }
}