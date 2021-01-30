package utils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Map;

public class Files_Handler {

    /**
     * Write String to file
     * @param file
     * @param string
     * @throws IOException
     */
    public static void writeToFile(File file, String string) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(string);
        fileWriter.close();
    }

    /**
     * Write JSONObject to File
     * @param file
     * @param jsonObject
     * @throws IOException
     */
    public static void writeJSONToFile(File file, JSONObject jsonObject) throws IOException {
        writeToFile(file, JSONObject.toJSONString(jsonObject));
    }

    /**
     * Write Map to file
     * @param file
     * @param map
     * @throws IOException
     */
    public static void writeMapAsJSONToFile(File file, Map<String, String> map) throws IOException {
        writeToFile(file, new JSONObject(map).toJSONString());
    }

    /**
     * Read From file
     * @param file
     * @return
     * @throws IOException
     */
    public static String readFromFile(File file) throws IOException {
        String string = "";
        BufferedReader br = new BufferedReader(new FileReader(file));
        String currentLine;
        while ((currentLine = br.readLine()) != null) {
            string = string.concat(currentLine);
        }
        return string;
    }

    /**
     * Read From file, Parse and return JSONObject.
     * @param file
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public static JSONObject readJSONFromFile(File file) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        Reader reader = new FileReader(file);
        JSONObject jsonObject = (JSONObject) parser.parse(reader);
        return jsonObject;
    }
}
