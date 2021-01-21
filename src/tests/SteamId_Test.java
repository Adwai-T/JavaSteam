package tests;

import login.SteamID;

public class SteamId_Test {

    public static String my_id = "76561198409395657";
    public static String my_id32 = "449129929";
    public static String my_accountId = "224564964";
    private static SteamID.AccountType myaccountType = SteamID.AccountType.U;
    private static SteamID.AccountUniverse myaccountUniverse = SteamID.AccountUniverse.Public;
    private SteamID id;

    public SteamId_Test (){
        this.id = new SteamID(my_id);
        assert (my_id.equals(id.steamId64)) : "Steam id64 generated failed.";
        assert (my_id32.equals( id.steamId32)) : "Steam id32 generated failed.";
        assert (my_accountId.equals(id.accountId)) : "Steam accountid generated failed.";
        assert (myaccountType == id.accountType) : "Steam Account Type generated failed. ";;
        assert (myaccountUniverse == id.universe) : "Steam Account Universe generated failed. ";;
        assert (id.steamId32.equals(SteamID.generateSteamId32(id))) : "Steam id SteamID.generateSteamId32 failed.";
        assert (id.steamId64.equals(SteamID.generateSteamID64(id))) : "Steam id SteamID.generateSteamId64 failed.";
    }
}
