package trade.enums;

import java.util.HashMap;
import java.util.Map;

public enum EItemQuality {

    NORMAL("B2B2B2"),
    UNIQUE("7D6D00"),
    VINTAGE("476291"),
    GENUINE("4D7455"),
    STRANGE("CF6A32"),
    UNUSUAL("8650AC"),
    HAUNTED("38F3AB"),
    COLLECTORS("AA0000"),
    DECORATED("FAFAFA"),
    COMMUNITY_SELFMADE("70B04A"),
    VALVE("A50D79"),
    PAINTKITWEAPON("");


    private String color;

    // Reverse-lookup map
    private static final Map<String, EItemQuality> lookup = new HashMap<String, EItemQuality>();

    static {
        for (EItemQuality quality : EItemQuality.values()) {
            lookup.put(quality.getEItemQualityColor(), quality);
        }
    }

    public static EItemQuality get(String color) {
        return lookup.get(color.toUpperCase());
    }

    public static EItemQuality parseFromString(String value) {
        value = value.toUpperCase();
        for(EItemQuality quality : EItemQuality.values()){
            if(value.equals(quality.toString())){
                return quality;
            }
        }
        return null;
    }

    EItemQuality(String color) {
        this.color = color;
    }

    public String getEItemQualityColor(){
        return color;
    }
}
