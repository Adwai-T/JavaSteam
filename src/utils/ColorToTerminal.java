package utils;

public class ColorToTerminal {

    /*
     *
     * System.out.println(ANSI_GREEN_BACKGROUND + "This text has a green background but default text!" + ANSI_RESET);
     *
     * */

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    //----String Color
    public static final void printRED(String string) {
        System.out.println(ANSI_RED + string + ANSI_RESET);
    }

    public static final void printGREEN(String string) {
        System.out.println(ANSI_GREEN + string + ANSI_RESET);
    }
    public static final void printYELLOW(String string) {
        System.out.println(ANSI_YELLOW + string + ANSI_RESET);
    }

    public static final void printBLUE(String string) {
        System.out.println(ANSI_BLUE + string + ANSI_RESET);
    }

    public static final void printPURPLE(String string) {
        System.out.println(ANSI_PURPLE + string + ANSI_RESET);
    }

    public static final void printCYAN(String string) {
        System.out.println(ANSI_CYAN + string + ANSI_RESET);
    }

    public static final void printWHITE(String string) {
        System.out.println(ANSI_WHITE + string + ANSI_RESET);
    }

    public static final void printBLACK(String string) {
        System.out.println(ANSI_BLACK + string + ANSI_RESET);
    }

    //--- Background
    public static final void printBGRED(String string) {
        System.out.println(ANSI_RED_BACKGROUND + ANSI_GREEN + string + ANSI_RESET);
    }

    public static final void printBGGREEN(String string) {
        System.out.println(ANSI_GREEN_BACKGROUND + ANSI_RED + string + ANSI_RESET);
    }
    public static final void printBGYELLOW(String string) {
        System.out.println(ANSI_YELLOW_BACKGROUND + ANSI_PURPLE + string + ANSI_RESET);
    }

    public static final void printBGBLUE(String string) {
        System.out.println(ANSI_BLUE_BACKGROUND + ANSI_RED + string + ANSI_RESET);
    }

    public static final void printBGPURPLE(String string) {
        System.out.println(ANSI_PURPLE_BACKGROUND + ANSI_YELLOW + string + ANSI_RESET);
    }

    public static final void printBGCYAN(String string) {
        System.out.println(ANSI_CYAN_BACKGROUND + ANSI_BLACK + string + ANSI_RESET);
    }

    public static final void printBGWHITE(String string) {
        System.out.println(ANSI_WHITE_BACKGROUND + ANSI_BLACK + string + ANSI_RESET);
    }

    public static final void printBGBLACK(String string) {
        System.out.println(ANSI_BLACK_BACKGROUND + ANSI_WHITE + string + ANSI_RESET);
    }
}
