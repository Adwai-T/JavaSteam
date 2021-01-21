package utils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Map;

public class Files_Handler {

    public static void writeToFile(File file, String string) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(string);
        fileWriter.close();
    }

    public static void writeJSONToFile(File file, JSONObject jsonObject) throws IOException {
        writeToFile(file, JSONObject.toJSONString(jsonObject));
    }

    public static void writeMapAsJSONToFile(File file, Map<String, String> map) throws IOException {
        writeToFile(file, new JSONObject(map).toJSONString());
    }

    public static String readFromFile(File file) throws IOException {
        String string = "";
        BufferedReader br = new BufferedReader(new FileReader(file));
        String currentLine;
        while ((currentLine = br.readLine()) != null) {
            string = string.concat(currentLine);
        }
        return string;
    }

    public static JSONObject readJSONFromFile(File file) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        Reader reader = new FileReader(file);
        JSONObject jsonObject = (JSONObject) parser.parse(reader);
        return jsonObject;
    }
}
