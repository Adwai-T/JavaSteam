package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex_Handler {

    /**
     *
     * @param input String to match Regex
     * @param regex String regular expression
     * @return
     */
    public static List<String> getMatches(String input, String regex){

        List<String> matchList = new ArrayList<>();
        try{
            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(input);

            while (matcher.find()) {
                matchList.add(matcher.group(0));
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    matchList.add(matcher.group(i));
                }
            }

            return matchList;

        }catch(Exception err){
            System.out.println("There was a problem finding a match in regex");
            System.out.println(err.getMessage());
            err.printStackTrace();
            return null;
        }
    }
}