package login;

/*
For details check the link :
https://developer.valvesoftware.com/wiki/SteamID
*/
import java.math.BigInteger;

public class SteamID {

    public String accountId;
    public AccountUniverse universe;
    public AccountType accountType; //Hard coded for individual U accounts
    public String instanceOfAccount = "00000000000000000001"; //It is usually 1 for user accounts. No enum for this is present.
    private String y;
    public String steamId64;
    public String steamId32;

    private BigInteger baseId = new BigInteger("76561198092541763");

    /**
     * Construct from steamId STEAM_X:Y:Z example STEAM_1:1:224564964
     * X : Universe, use AccountUniverse
     * Y : 0 or 1
     * Z : account number
     * @param accountId Z
     * @param accountUniverse X
     * @param y Y
     */
    public SteamID(String accountId, AccountUniverse accountUniverse, String y) {
        this.accountId = accountId;
        this.universe = accountUniverse;
        this.y = y;
    }

    /**
     * Construct from steamId32 X:Y:Z  example U:1:449129929
     * @param steamId32 Z
     * @param universe AccountUniverse Y
     */
    public SteamID(String steamId32, AccountType type,AccountUniverse universe) {
        this.steamId32 = steamId32;
        this.universe = universe;
        this.accountType = type;
        BigInteger id = new BigInteger(steamId32);
        this.y = id.remainder(new BigInteger("2")).toString();

        accountId = id.subtract(new BigInteger(y)).divide(new BigInteger("2")).toString();
        this.steamId64 = SteamID.generateSteamID64(this);
    }

    /**
     * Construct From steamId64 example 76561198092541763
     * @param steamId64
     */
    public SteamID(String steamId64) {
        this.steamId64 = steamId64;
        BigInteger id = new BigInteger(steamId64);
        StringBuilder sb = new StringBuilder(id.toString(2));

        if(sb.length() < 64) {
            int imax = 64 - sb.length();
            for(int i = 0; i < imax; i++){
                sb.insert(0, "0");
            }
        }
        String string = sb.toString();
        this.universe = AccountUniverse.getStringValue(Integer.parseInt(string.substring(0, 8)));
        this.accountType = AccountType.getStringValue(Integer.parseInt(string.substring(8, 12)));
        this.accountId = new BigInteger(string.substring(32, 63), 2).toString(10);
        this.y = string.substring(63);
        this.steamId32 = generateSteamId32(this);
    }

    public static String generateSteamId32(SteamID steamId) {
        //W = 2 * Z + Y;
        return new BigInteger(steamId.accountId)
                .multiply(new BigInteger("2"))
                .add(new BigInteger(steamId.y))
                .toString();
    }

    public static String generateSteamID64(SteamID steamId) {
        BigInteger id = new BigInteger(steamId.accountId);
        //Last 32 bit
        StringBuilder idString = new StringBuilder(id.toString(2));
        if(idString.length() < 31) {
            int maxi = 31 - idString.length();
            for(int i = 0; i < maxi; i++) {
                idString.insert(0, "0");
            }
        }
        idString.append(steamId.y);

        //First 32Bit
        String accountDetails = AccountUniverse.getBitStringValue(steamId.universe) + AccountType.getBitStringValue(steamId.accountType)+ steamId.instanceOfAccount;

        BigInteger bigInteger = new BigInteger(idString.insert(0, accountDetails).toString(), 2);
        return bigInteger.toString(10);
    }

    public enum AccountUniverse{
        Individual,
        Public,
        Beta,
        Internal,
        Dev,
        RC;

        public static int getIntValue(AccountUniverse type){
            return type.ordinal();
        }

        public static String getBitStringValue(AccountUniverse type) {
            return "0000000" + Integer.toString(getIntValue(type));
        }

        public static AccountUniverse getStringValue(int intValue){
            for(AccountUniverse type : AccountUniverse.values()) {
                if(AccountUniverse.getIntValue(type) == intValue) {
                    return type;
                };
            }
            return null;
        }
    }

    public enum AccountType{
        I,
        U, //Individual
        M,
        G,
        A,
        P,
        C,
        g, //Clan
        T, //Chat
        a;

        public static int getIntValue(AccountType type){
            return type.ordinal();
        }

        public static String getBitStringValue(AccountType type) {
            return "000" + Integer.toString(getIntValue(type));
        }

        public static AccountType getStringValue(int intValue){
            for(AccountType type : AccountType.values()) {
                if(AccountType.getIntValue(type) == intValue) {
                    return type;
                };
            }
            return null;
        }
    }

    @Override
    public String toString() {

//        StringBuilder bd = new StringBuilder();
//        bd.append("AccountID : " + accountId + "\n");
//        bd.append("Universe : " + universe + "\t");
//        bd.append("Account Type : " + accountType + "\n");
//        bd.append("SteamId64 -> " + steamId64 + "\n");
//        bd.append("SteamId32 -> " + steamId32);
//        return bd.toString();

        return steamId64;
    }
}
