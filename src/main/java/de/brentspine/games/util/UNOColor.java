package de.brentspine.games.util;

public enum UNOColor {

    GREEN("green"),
    YELLOW("yellow"),
    BLUE("blue"),
    RED("red"),
    SPECIAL("special");

    UNOColor(String s) {

    }

    public static UNOColor color(String s) {
        if(s.equalsIgnoreCase("g")) {
            return GREEN;
        }
        if(s.equalsIgnoreCase("y")) {
            return YELLOW;
        }
        if(s.equalsIgnoreCase("b")) {
            return BLUE;
        }
        if(s.equalsIgnoreCase("r")) {
            return RED;
        }
        if(s.equalsIgnoreCase("green")) {
            return GREEN;
        }
        if(s.equalsIgnoreCase("yellow")) {
            return YELLOW;
        }
        if(s.equalsIgnoreCase("blue")) {
            return BLUE;
        }
        if(s.equalsIgnoreCase("red")) {
            return RED;
        }
        return SPECIAL;
    }

}
