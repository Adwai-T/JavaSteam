package login.guard;

public class EndPoints {
    public static final String STEAMAPI_BASE = "https://api.steampowered.com";
    public static final String COMMUNITY_BASE = "https://steamcommunity.com";
    public static final String MOBILEAUTH_BASE = STEAMAPI_BASE + "/IMobileAuthService/%s/v0001";
    public static final String MOBILEAUTH_GETWGTOKEN =STEAMAPI_BASE + "/IMobileAuthService/GetWGToken/v0001";
    public static final String TWO_FACTOR_BASE = STEAMAPI_BASE + "/ITwoFactorService/%s/v0001";
    public static final String TWO_FACTOR_TIME_QUERY = STEAMAPI_BASE + "/ITwoFactorService/QueryTime/v0001";
}
