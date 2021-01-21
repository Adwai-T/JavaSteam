package utils;

import java.util.Scanner;

public class GetUserInputFromTerminal {

    /**
     * Get user Input from commandline
     * @return String UserInput
     */
    public static String getString(){
        String userInput = "";
        Scanner scanner = new Scanner(System.in);
        userInput = userInput.concat(scanner.nextLine());
        return userInput;
    }
}