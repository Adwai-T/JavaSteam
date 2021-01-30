package trade.classes;

import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import steamapi.Steam_TradeAPI;
import trade.Trade;
import trade.enums.EItemQuality;
import utils.Regex_Handler;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.*;

/*
To get all the items in player's inventory we could use
https://steamcommunity.com/inventory/<Steam64Id>/<appid>/<contextid>?l=english&count=5000
eg : https://steamcommunity.com/inventory/766878/440/2?l=english&count=5000
 */

public class Item {

    //Use the steamIconImageUrl+icon to get the image link.
    public static final String steamIconImageUrl = "http://cdn.steamcommunity.com/economy/image/";
    public Long appid; //App that this item belongs to. Eg 440 is for Team Fortress 2
    public String contextid; //Steam has sub-inventories that are called as context. Eg Community, gift, coupons. Publicly visible inventory context is 2.
    public String assetid; //This is a unique if for the item in the given app and context. Not unique across steam or even the given app.
    /*
    * classid and instanceid are used to map the item to its description
    * We could use the ISteamEconomy/GetAssetClassInfo/ Web API request to get the description
    * It is not necessary to have instanceid in the request but the description might be missing some important details.
    * */
    public String classid;
    public String instanceid;
    public String icon; //Icon/Image of the item
    public String amount; //For stackable items.
    public boolean missing; //If the item is present in the backpack
    public String name; //String Name of Item
    public String name_color; //Color of name, can give an idea of quality of item, not recommended though
    public String background_color;
    public EItemQuality quality; //Inferred by us from other data to confirm exact quality.
    public String effect;
    public String appDataQuality; //Quality of item form app_data. App specific data. Is a number String eg "6"
    public EItemQuality tagQuality;
    public String grade;
    public String def_index; //App data game specific - dont exactly know the use
    public String slot; //Slot that the item is Equip
    public String paint; //Color the item is Painted
    public String collection; //eg Warbird Collections
    public String exterior; //eg Field Test, Factory New
    public Boolean hasKillStreakActive;
    public Boolean hasKillstreakSheen;
    public String killstreakSheen;
    public Boolean hasKillstreakEffect;
    public String killstreakEffect;
    public Boolean isFestivized; //If name has festivized and description does not
    // that means a festivizer has been applied to this item
    public Boolean craftable = true;
    public Boolean hasHolidayRestriction;
    public String market_hash_name;
    public String market_name;
    public String market_tradable_restriction;
    public String market_marketable_restriction;
    public String type_name;
    public String type;
    public String tradable;
    public String commodity;
    public String marketable;
    public String fraudwarnings; //When an item is renamed, this is the renamed string.

    public Item() {}

    public Item(Long appid, String contextid, String assetid, String classid, String instanceid) {
        this.appid = appid;
        this.contextid = contextid;
        this.assetid = assetid;
        this.classid = classid;
        this.instanceid = instanceid;
    }

    public Item(Long appid, String contextid, String assetid, String classid, String instanceid, String amount, Boolean missing) {
        this.appid = appid;
        this.contextid = contextid;
        this.assetid = assetid;
        this.classid = classid;
        this.instanceid = instanceid;
        this.amount = amount;
        this.missing = missing;
    }

    public String toJSONString() {
        JSONObject item = new JSONObject();
        item.put("name", this.name);
        item.put("market_name", this.market_name);
        item.put("quality", this.quality);
        item.put("effect", this.effect);
        item.put("paint", this.paint);
        item.put("craftable", this.craftable);
        if(this.hasKillStreakActive){
            JSONObject killstreak = new JSONObject();
            killstreak.put("active", this.hasKillStreakActive);
            killstreak.put("hasKillstreakSheen", this.hasKillstreakSheen);
            killstreak.put("sheen", this.killstreakSheen);
            killstreak.put("hasKillstreakEffect", this.hasKillstreakEffect);
            killstreak.put("effect", this.killstreakEffect);
            item.put("killstreak", killstreak);
        }
        item.put("festivized", this.isFestivized);
        item.put("grade", this.grade);
        item.put("ware", this.exterior);
        item.put("Warning", this.fraudwarnings);

        return item.toJSONString();
    }

    /**
     * Fetch and populate description of this item
     * @param client HttpClient
     * @throws IOException
     * @throws InterruptedException
     */
    public void fetchDescription(HttpClient client) throws IOException, InterruptedException {
        List<Item> items = new ArrayList<>();
        items.add(this);

        fetchDescriptionForItems(client, items);
    }

    /**
     * Fetch and populate the description of all items in the provided list.
     * @param client Httpclient
     * @param items List of Items
     * @throws IOException
     * @throws InterruptedException
     */
    public static void fetchDescriptionForItems(HttpClient client, List<Item> items) throws IOException, InterruptedException {
        HttpResponse<String> response = Steam_TradeAPI.fetchItemsDescription(client, items);

        if(Objects.equals(response.body(), null)) return;

        JSONObject responseBodyObject = (JSONObject) JSONValue.parse(response.body());
        JSONObject result = (JSONObject) responseBodyObject.get("result");

        for(Item item : items) {
            String itemIdentifier = item.classid+"_"+item.instanceid;

            JSONObject itemJSON = (JSONObject) result.get(itemIdentifier);

            //Some weapons especially unique(normal yellow) quality weapons don't have instance id.
            //So their identifier in the json consist only of their class id.
            if(itemJSON == null) {
                itemIdentifier = item.classid;
                itemJSON = (JSONObject) result.get(itemIdentifier);
            }
            item.name = (String) itemJSON.get("name");
            item.icon = (String) itemJSON.get("icon_url");
            item.market_hash_name = (String) itemJSON.get("market_hash_name");
            item.market_name = (String) itemJSON.get("market_name");
            item.name_color = (String) itemJSON.get("name_color");
            item.background_color = (String) itemJSON.get("background_color");
            item.type_name = (String) itemJSON.get("type");
            item.tradable = (String) itemJSON.get("tradable");
            item.marketable = (String) itemJSON.get("marketable");
            item.commodity = (String) itemJSON.get("commodity");
            item.market_tradable_restriction = (String) itemJSON.get("market_tradable_restriction");
            item.market_marketable_restriction = (String) itemJSON.get("market_marketable_restriction");

            //Some Items have fraudwarnings as a JSON object
            Object fraudwarning  = itemJSON.get("fraudwarnings");
            String name = getNameFromFraudWarning(fraudwarning);
            if(name != null) {
                item.fraudwarnings = item.name;
                item.name = name;
            }

            //Some of the items, eg Refined metal, Reclaimed metal, don't have any description,
            //that is their description is an empty string.
            //We just handle the exception right here as we don't want to do anything special in that case.
            try{
                JSONObject descriptionJSON = (JSONObject) itemJSON.get("descriptions");
                populateItemWithDescriptionData(item, descriptionJSON);
            }catch (ClassCastException e){ }

            //"action" and "market_actions" objects are not included, they don't seem necessary right now.

            JSONObject tagsJson = (JSONObject) itemJSON.get("tags");
            populateItemWithTagsData(item, tagsJson);

            JSONObject appdataJson = (JSONObject) itemJSON.get("app_data");
            populateItemWithAppSpecificData(item, appdataJson);

            deduceItemQuality(item);
            deduceKillstreakProperties(item);
            if(item.isFestivized == null) item.isFestivized = false;
        }
    }

    private static void populateItemWithDescriptionData(Item item, JSONObject description){
        //i is taken as 40 as an arbitrary large value.
        for(Integer i = 0; i < 40; i++) {
            JSONObject des = (JSONObject) description.get(i.toString());
            if(des != null) {
                String value = (String) des.get("value");

                //Sometimes that value of subValue might have less than 10 characters.
                //But we are not concerned with those values any ways so we just ignore those cases and
                //handle the exception right here
                try{
                    String subValue = value.substring(0, 10);
                    if(subValue.equals("Paint Colo")){ //Paint Color
                        item.paint = value.substring(13);
                    }else if(subValue.equals("Holiday Re")){ //Holiday Restriction: Halloween / Full Moon
                        item.hasHolidayRestriction = true;
                    }else if(subValue.equals("Killstreak")){ //Killstreaks Active
                        item.hasKillStreakActive = true;
                    }else if(subValue.equals("( Not Usab")) { //( Not Usable in Crafting )
                        item.craftable = false;
                    }else if(subValue.equals("Festivized")) { //Festivized
                        item.isFestivized = true;
                    } else if(subValue.equals("★ Unusual ")) { //★ Unusual Effect: Kaleidoscope
                        item.effect = value.substring(18);
                    }
                } catch (StringIndexOutOfBoundsException ignored) {

                }
            } else break;
        }
    }

    private static void populateItemWithTagsData(Item item, JSONObject tags){
        for(Integer i = 0; i < 10; i++) {
            JSONObject tag = (JSONObject) tags.get(i.toString());
            if(tag != null) {
                String category = (String) tag.get("category_name");

                if(category.equals("Quality")) {
                    item.tagQuality = EItemQuality.parseFromString((String)tag.get("name"));
                    if(item.tagQuality == null) {
                        item.tagQuality = EItemQuality.parseFromString((String)tag.get("internal_name"));
                    }

                }else if(category.equals("Type")){
                    item.type = (String) tag.get("internal_name");

                }else if(category.equals("Grade")) {
                    item.grade = (String) tag.get("name");

                }else if(category.equals("Collection")) {
                    item.collection = (String) tag.get("name");

                }else if(category.equals("Exterior")) {
                    item.exterior = (String) tag.get("name");
                }
            }else break;
        }
    }

    private static void populateItemWithAppSpecificData(Item item, JSONObject app_data){
        item.def_index = (String) app_data.get("def_index");
        item.appDataQuality = (String) app_data.get("quality");
        item.slot = (String) app_data.get("slot");
    }

    private static void deduceItemQuality(Item item) {
        if(item.tagQuality == EItemQuality.get(item.name_color)){
            item.quality = EItemQuality.get(item.name_color);
        }else if(item.tagQuality == EItemQuality.parseFromString("paintkitweapon")) {
            item.quality = EItemQuality.parseFromString("paintkitweapon");
        }
        else {
            item.quality = null;
        }
    }

    private static String getNameFromFraudWarning(Object fraudWarning) {
        try{
            String name = (String) fraudWarning;
            if(name.length() > 25 && name != null) {
                String regex  = "\\\".*\\\"$";
                return Regex_Handler
                        .getFirstMatch(name, regex)
                        .get(0)
                        .replace("\\", "")
                        .replace("\"", "");
            }else return null;
        }catch (ClassCastException e) {
            JSONObject fraud = (JSONObject) fraudWarning;

            for(Integer i = 0; i < 40; i++) {
                String num = (String) fraud.get(i.toString());
                if(num != null && num.length() > 25) {
                    String regex  = "\\\".*\\\"$";
                    return Regex_Handler
                            .getFirstMatch(num, regex)
                            .get(0)
                            .replace("\\", "")
                            .replace("\"", "");
                }
            }
        }
        return null;
    }

    private static void deduceKillstreakProperties(Item item){
        if(item.hasKillStreakActive == null) {
            item.hasKillStreakActive = false;
            item.hasKillstreakEffect = false;
            item.hasKillstreakSheen = false;
        }
        if(item.hasKillStreakActive) {
            if(item.killstreakSheen != null) item.hasKillstreakSheen = true;
            if(item.killstreakEffect != null) item.hasKillstreakEffect = true;
        }
    }

    /**
     * Return the map representation of this item.
     * @return HashMap of item instance.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> desc = new HashMap<>();
        desc.put("name", this.name);
        desc.put("market_name", this.market_name);
        desc.put("quality", this.quality.toString());
        desc.put("effect", this.effect);
        desc.put("festivized", this.isFestivized);
        desc.put("warning", this.fraudwarnings);
        desc.put("ware", this.exterior);
        desc.put("paint", this.paint);
        desc.put("craftable", this.craftable);

        Map<String, Object> killstreak = new HashMap<>();
        killstreak.put("is_active", this.hasKillStreakActive);
        killstreak.put("hasKillstreakSheen", this.hasKillstreakSheen);
        killstreak.put("killstreakSheen", this.killstreakSheen);
        killstreak.put("hasKillstreakEffect", this.hasKillstreakEffect);
        killstreak.put("killstreakEffect", this.killstreakEffect);
        desc.put("killstreak", killstreak);

        return desc;
    }

    /**
     * Returns a Bson Type Document containing all the relevant Item variables.
     * @return Bson Document
     */
    public Document toBsonDocument() {
        return new Document(this.toMap());
    }

    /**
     * We give leeway when we buy item, so we can buy items more valuable than what we asked for.
     * eg. A item that has paint can be bought at the price that we have set for a non painted item.
     * But when we sell items we want the item to be exactly matched so that we get the exact price
     * for that item as we have set.
     * eg. When we sell we want so differentiate between painted and non painted items and even the
     * paint color of item.
     * @param doc Document to compare to this item
     * @param operation BUY or SELL
     * @return true if Match
     */
    public boolean compareItemToDocument(Document doc, Trade.TradeOperation operation) {
        if(name.equals(doc.getString("name")) && market_name.equals(doc.get("market_name"))){
            //effect can be null and hence a String comparison will not work here.
            if(
                    quality.toString().equals(doc.get("quality"))
                    && Objects.equals(effect, doc.get("effect"))
                    && Objects.equals(craftable, doc.get("craftable"))
                    && Objects.equals(exterior, doc.get("ware"))
            ){
                if(operation == Trade.TradeOperation.BUY) {
                    return compareBuySpecific(doc);
                }else return compareSellSpecific(doc);
            }
        }
        return false;
    }

    //Also can be named as Soft-Compare. Might not be the exact item in the document.
    private boolean compareBuySpecific(Document doc) {
        if(isFestivized || Objects.equals(doc.get("isFestivized"), isFestivized)){
            if(doc.get("paint") != null) {
                if(Objects.equals(doc.get("paint"), paint)) {
                    return compareKillstreakFromDocumentToBuy(doc);
                } else return false;
            } else return compareKillstreakFromDocumentToBuy(doc);
        }
        return false;
    }

    private boolean compareKillstreakFromDocumentToBuy(Document doc) {
        if(hasKillStreakActive || Objects.equals(doc.get("hasKillstreakActive"), hasKillStreakActive)){
            if(hasKillstreakSheen || Objects.equals(doc.get("hasKillstreakSheen"), hasKillstreakSheen)){
                if(hasKillstreakEffect || Objects.equals(doc.get("hasKillstreakEffect"), hasKillstreakEffect)){
                    if(doc.get("killstreakSheen") != null) {
                        if(doc.get("killstreakSheen").equals(killstreakSheen)) {
                            if (doc.get("killstreakEffect") != null) {
                               if(doc.get("killstreakEffect").equals(killstreakEffect)) {
                                   return true;
                               }else return false;
                            }else return true;
                        }else {
                            return false;
                        }
                    }else {
                        if (doc.get("killstreakEffect") != null) {
                            if(doc.get("killstreakEffect").equals(killstreakEffect)) {
                                return true;
                            }else return false;
                        }else return true;
                    }
                }
            }
        }
        return false;
    }

    //Can also be called Hard-Compare as the item will be the same as the item specified in the Document.
    private boolean compareSellSpecific(Document doc) {
        if(Objects.equals(doc.get("isFestivized"), isFestivized)){
            if(Objects.equals(doc.get("paint"), paint)) {
                return compareKillstreakFromDocumentToSell(doc);
            }
        }
        return false;
    }

    private boolean compareKillstreakFromDocumentToSell(Document doc) {
        if(Objects.equals(doc.get("hasKillstreakActive"), hasKillStreakActive)){
            if(Objects.equals(doc.get("hasKillstreakSheen"), hasKillstreakSheen)) {
                if (Objects.equals(doc.get("hasKillstreakEffect"), hasKillstreakEffect)) {
                    if (Objects.equals(killstreakSheen, doc.get("killstreakSheen"))) {
                        if (Objects.equals(killstreakEffect, doc.get("killstreakEffect"))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
