package de.brentspine.games.util;

import de.brentspine.util.Dice;

import java.util.ArrayList;
import java.util.HashMap;

public enum UNOCard {

    //G, Y, B, R, S
    //p2, reverse, expose,      0, 1, 2, 3, 4, 5, 6, 7, 8, 9      wish, wish4

    Gp2("green_p2"),
    GReverse("green_reverse"),
    GExpose("green_expose"),
    G0("green_0"),
    G1("green_1"),
    G2("green_2"),
    G3("green_3"),
    G4("green_4"),
    G5("green_5"),
    G6("green_6"),
    G7("green_7"),
    G8("green_8"),
    G9("green_9"),

    Yp2("yellow_p2"),
    YReverse("yellow_reverse"),
    YExpose("yellow_expose"),
    Y0("yellow_0"),
    Y1("yellow_1"),
    Y2("yellow_2"),
    Y3("yellow_3"),
    Y4("yellow_4"),
    Y5("yellow_5"),
    Y6("yellow_6"),
    Y7("yellow_7"),
    Y8("yellow_8"),
    Y9("yellow_9"),

    Bp2("blue_p2"),
    BReverse("blue_reverse"),
    BExpose("blue_expose"),
    B0("blue_0"),
    B1("blue_1"),
    B2("blue_2"),
    B3("blue_3"),
    B4("blue_4"),
    B5("blue_5"),
    B6("blue_6"),
    B7("blue_7"),
    B8("blue_8"),
    B9("blue_9"),

    Rp2("red_p2"),
    RReverse("red_reverse"),
    RExpose("red_expose"),
    R0("red_0"),
    R1("red_1"),
    R2("red_2"),
    R3("red_3"),
    R4("red_4"),
    R5("red_5"),
    R6("red_6"),
    R7("red_7"),
    R8("red_8"),
    R9("red_9"),

    SWish("special_wish"),
    SWish4("special_wish4"),

    UNO("uno"),
    NONE("none");

    UNOCard(String s) {

    }

    private static boolean running = false;
    private static ArrayList<UNOCard> allCards = new ArrayList<>();
    private static HashMap<UNOCard, String> allCardEmojis = new HashMap<>();

    public static UNOCard generateRandom() {
        run();
        return allCards.get(new Dice(0, allCards.size() - 1).roll());
    }

    public static String getCardEmoteID(UNOCard card) {
        run();
        return allCardEmojis.get(card);
    }

    private static void run() {
        if(!running) {
            for(UNOCard current : UNOCard.values()) {
                allCards.add(current);
            }
            allCards.add(UNOCard.SWish);
            allCards.add(UNOCard.SWish4);
            allCards.remove(UNOCard.UNO);
            allCards.remove(UNOCard.NONE);

            allCardEmojis.put(UNOCard.Gp2, "898516124956774420");
            allCardEmojis.put(UNOCard.GReverse, "898516125065809960");
            allCardEmojis.put(UNOCard.GExpose, "898516124705099827");
            allCardEmojis.put(UNOCard.G0, "898590339487633458");
            allCardEmojis.put(UNOCard.G1, "898516124977745930");
            allCardEmojis.put(UNOCard.G2, "898516124814155816");
            allCardEmojis.put(UNOCard.G3, "898516125371994122");
            allCardEmojis.put(UNOCard.G4, "898516124851920896");
            allCardEmojis.put(UNOCard.G5, "898516124818350130");
            allCardEmojis.put(UNOCard.G6, "898516124965146634");
            allCardEmojis.put(UNOCard.G7, "898516124851908619");
            allCardEmojis.put(UNOCard.G8, "898516124868685865");
            allCardEmojis.put(UNOCard.G9, "898516124898058270");

            allCardEmojis.put(UNOCard.Yp2, "898515956584828978");
            allCardEmojis.put(UNOCard.YReverse, "898515956119269418");
            allCardEmojis.put(UNOCard.YExpose, "898515956664524840");
            allCardEmojis.put(UNOCard.Y0, "898590339579924521");
            allCardEmojis.put(UNOCard.Y1, "898515956203126805");
            allCardEmojis.put(UNOCard.Y2, "898515956484149328");
            allCardEmojis.put(UNOCard.Y3, "898515956098297857");
            allCardEmojis.put(UNOCard.Y4, "898515956358320148");
            allCardEmojis.put(UNOCard.Y5, "898515956354125824");
            allCardEmojis.put(UNOCard.Y6, "898515956026978316");
            allCardEmojis.put(UNOCard.Y7, "898515956337369098");
            allCardEmojis.put(UNOCard.Y8, "898515956387708928");
            allCardEmojis.put(UNOCard.Y9, "898515956341551125");

            allCardEmojis.put(UNOCard.Bp2, "898515793896165436");
            allCardEmojis.put(UNOCard.BReverse, "898515794294632518");
            allCardEmojis.put(UNOCard.BExpose, "898515794315599942");
            allCardEmojis.put(UNOCard.B0, "898590339454091274");
            allCardEmojis.put(UNOCard.B1, "898515793866792971");
            allCardEmojis.put(UNOCard.B2, "898515793426399273");
            allCardEmojis.put(UNOCard.B3, "898515793833242644");
            allCardEmojis.put(UNOCard.B4, "898515793875193877");
            allCardEmojis.put(UNOCard.B5, "898515794193956955");
            allCardEmojis.put(UNOCard.B6, "898515793707417610");
            allCardEmojis.put(UNOCard.B7, "898515793841623061");
            allCardEmojis.put(UNOCard.B8, "898515793841651732");
            allCardEmojis.put(UNOCard.B9, "898515793468350476");

            allCardEmojis.put(UNOCard.Rp2, "898515461665341440");
            allCardEmojis.put(UNOCard.RReverse, "898515461321412660");
            allCardEmojis.put(UNOCard.RExpose, "898515461975732264");
            allCardEmojis.put(UNOCard.R0, "898590339437322250");
            allCardEmojis.put(UNOCard.R1, "898515461388501014");
            allCardEmojis.put(UNOCard.R2, "898515461401116673");
            allCardEmojis.put(UNOCard.R3, "898515461958955038");
            allCardEmojis.put(UNOCard.R4, "898515461644386324");
            allCardEmojis.put(UNOCard.R5, "898515461078155275");
            allCardEmojis.put(UNOCard.R6, "898515461451436063");
            allCardEmojis.put(UNOCard.R7, "898515461476614215");
            allCardEmojis.put(UNOCard.R8, "898515461841489940");
            allCardEmojis.put(UNOCard.R9, "898515399950344222");

            allCardEmojis.put(UNOCard.SWish, "898515293201121282");
            allCardEmojis.put(UNOCard.SWish4, "898515334972203019");

            allCardEmojis.put(UNOCard.UNO, "898558045590794300");
            allCardEmojis.put(UNOCard.NONE, "898558045590794300");

            running = true;
        }
    }

    public static ArrayList<UNOCard> getAllCards() {
        run();
        return allCards;
    }

    public String toEmoji() {
        run();
        return "<:" + this.toString() + ":" + getCardEmoteID(this) + ">";
    }

    public String toEmoteURL() {
        run();
        return "https://cdn.discordapp.com/emojis/" + getCardEmoteID(this) + ".png?size=128";
    }

    public String getEmojiID() {
        run();
        return getCardEmoteID(this);
    }

    public UNOColor getColor() {
        return UNOColor.color(this.name().substring(0, 1));
    }

    public String getSecondArg() {
        return this.name().substring(1, 2);
    }

    public String getLast2Letters() {
        return this.name().substring(this.name().length() - 2);
    }
}
