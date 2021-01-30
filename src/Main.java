import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import login.Login;
import login.UserDetails;
import login.guard.Confirmation;
import login.guard.SteamGuardAccount;
import org.bson.Document;
import org.json.simple.parser.ParseException;
import tests.SteamId_Test;
import trade.Trade;
import trade.TradeOffer_DataBase;
import trade.classes.TradeOffer;
import utils.ColorToTerminal;
import utils.Files_Handler;
import utils.GetUserInputFromTerminal;
import utils.TimeStamp_Handler;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//TODO : Add threads to parallel fetch data.

//TODO : Implement local file support for TradeOffers.

//TODO : Update MongDB accepted tradeOffer list if tradeOffer confirmation fails.

public class Main {

    public final static HttpClient client = HttpClient.newHttpClient();
    public HashMap<String, String> cookies;
    public HashMap<String, String> transferParameters;
    public MongoClient db_client;
    public TradeOffer_DataBase db;
    public SteamGuardAccount steamGuard;
    public static ArrayList<Document> trades;
    public static final String TIMEOFFSET = "19800"; //This is timezoneOffset for India.

    private void initalizeClients() {
        //Initialize MongoClient to send and get trade offer details.
        if(UserDetails.MONGODBLINK != null) {
            db_client = MongoClients.create(UserDetails.MONGODBLINK);
            db = new TradeOffer_DataBase(db_client);
        }else {
            db_client = null;
        }

        //trades from database that have selling and buying price for items.
        trades = new ArrayList<>();
    }

    public Main(){
        // Bot Start
        System.out.println(ColorToTerminal.ANSI_GREEN + "Bot Started" + ColorToTerminal.ANSI_RESET);

        //This step is necessary before performing any tests
        initalizeClients();
        try{
            getAuthenticationDetails();
            addCommonCookiesToMap();
            steamGuard = new SteamGuardAccount(client, cookies);
        }catch(Exception e) {
            ColorToTerminal.printRED("Login Failed");
            ColorToTerminal.printRED(e.getMessage());
            e.printStackTrace();
            return;
        }

//        Tests
//        Do the test after login or after we have our authentication details. As all test depend on them.
        test();

        Boolean running = true;
        while(running) {

            ColorToTerminal.printYELLOW("Running ... ");
            Trade trade = new Trade(client, db, cookies, steamGuard);
            trade.run();
            try{
                ColorToTerminal.printYELLOW("Sleeping ... ");
                Thread.sleep(10000);
                //Let steam to catch up so that it has confirmations ready.
                //This will only confirm trade offers accepted by this bot.
                steamGuardConfirmAcceptedTradeOffers();

            }catch (InterruptedException ie) {
                System.out.println(ColorToTerminal.ANSI_RED + "Thread Sleep Failed." + ColorToTerminal.ANSI_RESET);
            } catch (SteamGuardAccount.WGTokenInvalidException e) {
                e.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        //Bot Terminates
        System.out.println(ColorToTerminal.ANSI_GREEN + "Bot ShutDown" + ColorToTerminal.ANSI_RESET);
    }

    public static void main(String[] args) {
        Main main = new Main();
    }

    private void getAuthenticationDetails()
            throws InterruptedException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        int selectedOption = selectOption();
        if(selectedOption == 1) {
            Login login = new Login(client);
            cookies = login.getCookies();
        }else if(selectedOption == 2) {
            try{
                cookies = Files_Handler.readJSONFromFile(new File("Cookies.json"));
            }catch(IOException e) {
                System.out.println(ColorToTerminal.ANSI_RED + "Could not find/read the Auth file." + ColorToTerminal.ANSI_RESET);
            }catch(ParseException e) {
                System.out.println(ColorToTerminal.ANSI_RED + "Could not parse Auth data from file." + ColorToTerminal.ANSI_RESET);
            }
        }
    }

    private static int selectOption(){
        System.out.println(
                ColorToTerminal.ANSI_GREEN_BACKGROUND
                        + ColorToTerminal.ANSI_BLACK
                        + "Please Select One of the numbers"
        );
        System.out.println(ColorToTerminal.ANSI_RESET + ColorToTerminal.ANSI_PURPLE);
        System.out.println("1 -> Login");
        System.out.println("2 -> Use Saved Auth File");
        System.out.println("3 -> Exit");
        System.out.println(ColorToTerminal.ANSI_RESET);
        return Integer.parseInt(GetUserInputFromTerminal.getString());
    }

    private void addCommonCookiesToMap() {
        cookies.put("Steam_Language", "english");
        cookies.put("timezoneOffset", TIMEOFFSET);
        cookies.put("bCompletedTradeOfferTutorial", "true");
        cookies.put("sessionid", Login.generateSessionId());
        cookies.put("mobileClientVersion", "0 (2.1.3)");
        cookies.put("mobileClient", "android");
        cookies.put("dob", "");
    }

    private void steamGuardConfirmAcceptedTradeOffers()
            throws InterruptedException, SteamGuardAccount.WGTokenInvalidException, IOException {
        if(TradeOffer.recentlyAcceptedTradeOffers.size() > 0) {
            List<Confirmation> confs = steamGuard.fetchConfirmations();
            if(confs.size() > 0) {
                for(Confirmation conf : confs) {
                    if(TradeOffer.recentlyAcceptedTradeOffers.contains(conf.getCreator())) {
                        if(steamGuard.acceptConfirmation(conf)) {
                            TradeOffer.recentlyAcceptedTradeOffers.remove(conf.getCreator());
                        }
                    }
                }
            }
        }
    }

    private boolean test() {
        try{
            SteamId_Test id_test = new SteamId_Test();

            Long timestamp = TimeStamp_Handler.getCurrentTimeStamp() - 72 * TimeStamp_Handler.OneHour;

//            TradeOffer_Test tradeOffer_test = new TradeOffer_Test(client, timestamp.toString());
//            tradeOffer_test.checkItemsInTradeOffer();
//            tradeOffer_test.savetradeOffer(db_client, db);
//            tradeOffer_test.acceptTradeOfferTest(cookies);

//            LocalDatabase_Test db_test = new LocalDatabase_Test(db_client);

            return true;

        }catch (Exception e) {
            System.out.println("One Of the Test failed");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}