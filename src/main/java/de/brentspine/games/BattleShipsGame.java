package de.brentspine.games;

import de.brentspine.Main;
import de.brentspine.util.Dice;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.*;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.interaction.callback.ComponentInteractionOriginalMessageUpdater;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class BattleShipsGame {

    DiscordApi api = Main.getApi();

    private final User user1;
    private final User user2;
    private String messageID;
    private Message message;
    private int[][] board1;
    private int[][] board2;
    private final Server server;
    private final TextChannel channel;
    private String whoseTurn;

    // SETTINGS
    private int ships1 = 1; // Submarine
    private int ships2 = 3; // Scouter
    private int ships3 = 2; // Destroyer
    private int ships4 = 1; // Cruiser
    private int ships5 = 1; // Aircraft carrier

    private int phase;

    /**
     * board
     *  0 --> Water             -   empty
     *  1 --> Ship              -   X
     *  2 --> Shot water        -   O
     *  3 --> Shot ship         -   X + O
     *  4 --> Destroyed Ship    -   X + O + ---
     *
     *
     * phase
     * 0 --> Initial Constructor value
     * 1 --> Confirmation message
     * 2 --> Settings phase
     * 3 --> Set ships
     * 4 --> InGame
     * 5 --> Ending
     *
     */

    public BattleShipsGame(User user1, User user2, Server server, TextChannel channel) {
        this.user1 = user1;
        this.user2 = user2;
        this.server = server;
        this.channel = channel;
        this.messageID = "";
        this.board1 = new int[12][12]; // [x] [y]    x --   y |
        this.board2 = new int[12][12];
        if(Dice.generateRandomNumberBetween(1, 2) == 1)
            whoseTurn = user1.getIdAsString();
        else
            whoseTurn = user2.getIdAsString();

        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                board1[i][j] = 0;
                board2[i][j] = 0;
            }
        }
        board1[1][5] = 1;
        board1[2][5] = 2;
        board1[3][5] = 1;
        board1[4][5] = 4;
        board1[5][5] = 3;
        board2[11][11] = 4;
        phase = 0;
    }


    public void moveSetBoatsPhase(int x, int y, User user) {
        getBoard(user)[x][y] = 1;
        
    }


    public BattleShipsGame sendConfirmationMessage() {
        phase = 1;
        try {
            message = channel.sendMessage("<@" + user2.getIdAsString() + ">\n" + user1.getDisplayName(server) + " has challenged you for a game of Battleships", getConfirmationActionRow(false)).get();
            messageID = message.getIdAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public ActionRow getConfirmationActionRow(boolean disabled) {
        ButtonBuilder buttonBuilder1 = new ButtonBuilder()
                .setStyle(ButtonStyle.SUCCESS)
                .setLabel("Accept")
                .setCustomId("battleships accept")
                .setDisabled(disabled);

        ButtonBuilder buttonBuilder2 = new ButtonBuilder()
                .setStyle(ButtonStyle.DANGER)
                .setLabel("Decline")
                .setCustomId("battleships decline")
                .setDisabled(disabled);

        ActionRowBuilder actionRowBuilder = new ActionRowBuilder()
                .addComponents(buttonBuilder1.build())
                .addComponents(buttonBuilder2.build());
        return actionRowBuilder.build();
    }

    public void startGame(ButtonInteraction buttonInteraction) throws ExecutionException, InterruptedException {
        phase = 2;
        sendSettings(buttonInteraction, false);
    }

    public void sendSettings(ButtonInteraction buttonInteraction, boolean newMessage) throws ExecutionException, InterruptedException {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Battleships: Settings")
                .addInlineField("Submarine", "`Length:` 1\n" +
                        "`Amount:` " + ships1)
                .addInlineField("Scouter", "`Length:` 2\n" +
                        "`Amount:` " + ships2)
                .addInlineField("Destroyer", "`Length:` 3\n" +
                        "`Amount:` " + ships3)
                .addInlineField("Cruiser", "`Length:` 4\n" +
                        "`Amount:` " + ships4)
                .addInlineField("Aircraft carrier", "`Length:` 5\n" +
                        "`Amount:` " + ships5)
                .addInlineField("⠀", "⠀")
                .setImage("https://cdn.discordapp.com/attachments/892110434889502761/953737654955020348/jafgnlkjdfnglksjdfngljksfndlgjksnfg.jpg")
                .setFooter("Bot by Brentspine", api.getOwner().get().getAvatar());
        if(!newMessage) {

            ComponentInteractionOriginalMessageUpdater messageUpdater = buttonInteraction.createOriginalMessageUpdater()
                    .setContent("Settings phase")
                    .addEmbed(embedBuilder);
            for(ActionRow actionRow : createSettingsActionRow(false)) {
                messageUpdater.addComponents(actionRow);
            }
            messageUpdater.update();

        }
    }

    public List<ActionRow> createSettingsActionRow(boolean disabled) {
        List<ActionRow> r = new ArrayList<>();
        List<String> left = new ArrayList<>();
        left.add("954457875244273764");
        left.add("954457893472714813");
        left.add("954457923214520350");
        left.add("954457947877031957");
        left.add("954463616097189950");
        List<String> right = new ArrayList<>();
        right.add("954457833523535892");
        right.add("954457893749538876");
        right.add("954457922908356689");
        right.add("954457948241944646");
        right.add("954463615916838922");
        for (int i = 0; i < 5; i++) {
            String type = "Loading...";
            switch (i) {
                case 0:
                    type = "Submarine";
                    break;
                case 1:
                    type = "Scouter";
                    break;
                case 2:
                    type = "Destroyer";
                    break;
                case 3:
                    type = "Cruiser";
                    break;
                case 4:
                    type = "Aircraft Carrier";
                    break;
            }
            ButtonBuilder buttonBuilder1 = new ButtonBuilder()
                    .setStyle(ButtonStyle.PRIMARY)
                    .setEmoji(api.getCustomEmojiById(left.get(i)).get())
                    .setLabel("")
                    .setCustomId("battleships settings " + type.toLowerCase().replace(" ", "") + " minus")
                    .setDisabled(disabled);

            ButtonBuilder buttonBuilder2 = new ButtonBuilder()
                    .setStyle(ButtonStyle.PRIMARY)
                    .setLabel(type)
                    .setCustomId("battleships settings " + type.toLowerCase().replace(" ", "") + " description")
                    .setDisabled(disabled);

            ButtonBuilder buttonBuilder3 = new ButtonBuilder()
                    .setStyle(ButtonStyle.PRIMARY)
                    .setEmoji(api.getCustomEmojiById(right.get(i)).get())
                    .setLabel("")
                    .setCustomId("battleships settings " + type.toLowerCase().replace(" ", "") + " plus")
                    .setDisabled(disabled);

            ActionRowBuilder actionRowBuilder = new ActionRowBuilder()
                    .addComponents(buttonBuilder1.build())
                    .addComponents(buttonBuilder2.build())
                    .addComponents(buttonBuilder3.build());
            if(i == 4) {
                ButtonBuilder buttonBuilder4 = new ButtonBuilder()
                        .setStyle(ButtonStyle.SUCCESS)
                        .setLabel("Start")
                        .setCustomId("battleships settings start")
                        .setDisabled(disabled);
                actionRowBuilder.addComponents(buttonBuilder4.build());
            }
            r.add(actionRowBuilder.build());
        }

        return r;
    }

    public void startInGame(ButtonInteraction buttonInteraction) throws ExecutionException, InterruptedException {
        phase = 3;
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Battleships: Game Overview")
                .setImage("https://cdn.discordapp.com/attachments/892110434889502761/953737654955020348/jafgnlkjdfnglksjdfngljksfndlgjksnfg.jpg")
                .setDescription("**Players:** " + user1.getDisplayName(server) + " vs. " + user2.getDisplayName(server) + "\n⠀")
                .addField(user1.getDisplayName(server), "" +
                        "`Parts shot:` 69\n" +
                        "`Parts remaining:` 69\n" +
                        "`Ships shot:` 69\n" +
                        "`Ships remaining:` 69")
                .addField(user2.getDisplayName(server), "" +
                        "`Parts shot:` 69\n" +
                        "`Parts remaining:` 69\n" +
                        "`Ships shot:` 69\n" +
                        "`Ships remaining:` 69")
                .setFooter("Bot by Brentspine", api.getOwner().get().getAvatar());


        buttonInteraction.createOriginalMessageUpdater()
                .setContent(user1.getMentionTag() + " vs. " + user2.getMentionTag() + "\nClick the Menu button to play")
                .addEmbed(embedBuilder)
                .addComponents(getMainActionRow(false))
                .update();
    }

    public ActionRow getMainActionRow(boolean disabled) {
        ButtonBuilder buttonBuilder1 = new ButtonBuilder()
                .setStyle(ButtonStyle.PRIMARY)
                .setLabel("Show Board")
                .setCustomId("battleships showboard")
                .setDisabled(disabled);

        ButtonBuilder buttonBuilder2 = new ButtonBuilder()
                .setStyle(ButtonStyle.DANGER)
                .setLabel("End Game")
                .setCustomId("battleships end")
                .setDisabled(disabled);

        ActionRowBuilder actionRowBuilder = new ActionRowBuilder()
                .addComponents(buttonBuilder1.build())
                .addComponents(buttonBuilder2.build());
        return actionRowBuilder.build();
    }

    public void sendBoard(ButtonInteraction buttonInteraction, int site, int[][] board, boolean newMessage, boolean moved, Integer... firstMove) {
        //buttonInteraction.respondLater();
        List<ActionRow> actionRows;
        StringBuilder description;
        if(site == 1) {
            description = new StringBuilder("**OPPONENT**\n" + user1.getDisplayName(server) + " vs. " + user2.getDisplayName(server) + "\n\n");
            actionRows = getBoardActionRow(true, false, false, moved, firstMove);
            for(int[] line : board) {
                for(int i : line) {
                    switch (i) {
                        case 0:
                        case 1:
                            description.append("<:te:953755661462798387> ");
                            //System.out.println(description);
                            break;
                        case 2:
                            description.append("<:to:953755697080827955> ");
                            break;
                        case 3:
                            description.append("<:th:954096934681989201> ");
                            break;
                        case 4:
                            description.append("<:td:954097341835640913> ");
                            break;

                        default:
                            description.append("<:te:953755661462798387>  ");
                            break;
                    }
                }
                description.append("\n");
            }
        }
        else if(site == 2) {
            actionRows = getBoardActionRow(false, true, false, moved, firstMove);
            description = new StringBuilder("**YOU**\n" + user1.getDisplayName(server) + " vs. " + user2.getDisplayName(server) + "\n\n");
            for(int[] line : board) {
                for(int i : line) {
                    switch (i) {
                        case 0:
                            description.append("<:te:953755661462798387> ");
                            //System.out.println(description);
                            break;
                        case 1:
                            description.append("<:tx:953755696745308172> ");
                            break;
                        case 2:
                            description.append("<:to:953755697080827955> ");
                            break;
                        case 3:
                            description.append("<:th:954096934681989201> ");
                            break;
                        case 4:
                            description.append("<:td:954097341835640913> ");
                            break;

                        default:
                            description.append("<:te:953755661462798387>  ");
                            break;
                    }
                }
                description.append("\n");
            }
        }
        else {
            actionRows = getBoardActionRow(true, true, false, moved, firstMove);
            description = new StringBuilder("UNKNOWN PAGE");
        }

        try {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Battleships")
                    .setDescription(description.toString())
                    .setFooter("Bot by Brentspine", api.getOwner().get().getAvatar());

            if(newMessage) {
                InteractionImmediateResponseBuilder responseBuilder = buttonInteraction.createImmediateResponder()
                        .setFlags(MessageFlag.EPHEMERAL)
                        .setContent("It's " + getWhoseTurnUser().getMentionTag() + "'s turn")
                        .addEmbed(embedBuilder);
                for(ActionRow c : actionRows) {
                    responseBuilder.addComponents(c);
                }
                responseBuilder.respond();
                //.thenAccept(message -> {
                //message.getChannel().sendMessage("Test");
                //}).exceptionally(ExceptionLogger.get());
            } else {
                ComponentInteractionOriginalMessageUpdater messageUpdater = buttonInteraction.createOriginalMessageUpdater()
                        .setFlags(MessageFlag.EPHEMERAL)
                        .setContent("It's " + getWhoseTurnUser().getMentionTag() + "'s turn")
                        .addEmbed(embedBuilder);
                for(ActionRow c : actionRows) {
                    messageUpdater.addComponents(c);
                }
                messageUpdater.update();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ActionRow> getBoardActionRow(boolean disabledFirst, boolean disabledSecond, boolean disabledFields, boolean numberPhase, Integer... move) {
        List<ActionRow> r = new ArrayList<>();
        UUID uuid = UUID.randomUUID();
        String firstMove = "";
        if(move.length > 0)
            firstMove = String.valueOf(move[0]);
        Main.getBattleShipsCommand().getGames().put(uuid.toString(), this);
        if(disabledFirst && disabledSecond)
            Main.getBattleShipsCommand().getBoardSites().put(uuid.toString(), 0);
        else if(disabledFirst)
            Main.getBattleShipsCommand().getBoardSites().put(uuid.toString(), 1);
        else if(disabledSecond)
            Main.getBattleShipsCommand().getBoardSites().put(uuid.toString(), 2);
        else
            Main.getBattleShipsCommand().getBoardSites().put(uuid.toString(), -1);

        ButtonBuilder buttonBuilder1 = new ButtonBuilder()
                .setStyle(ButtonStyle.PRIMARY)
                .setEmoji("◀")
                .setLabel("Opponent")
                .setCustomId("battleships board opponent " + uuid)
                .setDisabled(disabledFirst);

        ButtonBuilder buttonBuilder2 = new ButtonBuilder()
                .setStyle(ButtonStyle.PRIMARY)
                .setEmoji("▶")
                .setLabel("You")
                .setCustomId("battleships board you " + uuid)
                .setDisabled(disabledSecond);

        ActionRowBuilder actionRowBuilder = new ActionRowBuilder()
                .addComponents(buttonBuilder1.build())
                .addComponents(buttonBuilder2.build());
        r.add(actionRowBuilder.build());

        actionRowBuilder = new ActionRowBuilder();
        int a = 0;
        for (int i = 0; i < 12; i++) {
            String field;
            int movePhase = 0;
            if(numberPhase)
                movePhase = 1;
            if(!numberPhase)
                field = digitToLetter(i + 1);
            else
                field = digitToLetter(Integer.valueOf(firstMove) + 1) + (i + 1);
            ButtonBuilder buttonBuilder = new ButtonBuilder()
                    .setStyle(ButtonStyle.SECONDARY)
                    .setLabel(field)
                    .setCustomId("battleships move " + uuid + " " + movePhase + " " + i + " " + firstMove)
                    .setDisabled(disabledFields);
            if(a >= 5) {
                r.add(actionRowBuilder.build());
                actionRowBuilder = new ActionRowBuilder();
                a = 0;
            }
            actionRowBuilder.addComponents(buttonBuilder.build());
            a++;
        }
        r.add(actionRowBuilder.build());

        return r;
    }

    public String digitToLetter(int i) {
        switch (i) {
            case 1:
                return "a";
            case 2:
                return "b";
            case 3:
                return "c";
            case 4:
                return "d";
            case 5:
                return "e";
            case 6:
                return "f";
            case 7:
                return "g";
            case 8:
                return "h";
            case 9:
                return "i";
            case 10:
                return "j";
            case 11:
                return "k";
            case 12:
                return "l";
            case 13:
                return "m";
            case 14:
                return "n";
            case 15:
                return "o";
            case 116:
                return "p";
            case 17:
                return "q";
            case 18:
                return "r";
            case 19:
                return "s";
            case 20:
                return "t";

            default:
                return "-1";
        }
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public void setBoard1(int[][] board1) {
        this.board1 = board1;
    }

    public void setBoard2(int[][] board2) {
        this.board2 = board2;
    }

    public void setShips1(int ships1) {
        this.ships1 = ships1;
    }

    public void setShips2(int ships2) {
        this.ships2 = ships2;
    }

    public void setShips3(int ships3) {
        this.ships3 = ships3;
    }

    public void setShips4(int ships4) {
        this.ships4 = ships4;
    }

    public void setShips5(int ships5) {
        this.ships5 = ships5;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public DiscordApi getApi() {
        return api;
    }

    public Message getMessage() {
        return message;
    }

    public int[][] getBoard1() {
        return board1;
    }

    public int[][] getBoard2() {
        return board2;
    }

    public Server getServer() {
        return server;
    }

    public TextChannel getChannel() {
        return channel;
    }

    public int getShips1() {
        return ships1;
    }

    public int getShips2() {
        return ships2;
    }

    public int getShips3() {
        return ships3;
    }

    public int getShips4() {
        return ships4;
    }

    public int getShips5() {
        return ships5;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public int getPhase() {
        return phase;
    }

    public boolean isUserPlayer(String id) {
        return user1.getIdAsString().equalsIgnoreCase(id) || user2.getIdAsString().equalsIgnoreCase(id);
    }

    public boolean canUserAccept(String id) {
        return user2.getIdAsString().equalsIgnoreCase(id);
    }

    public User getOtherUser(User user) {
        if(user.getIdAsString().equalsIgnoreCase(user1.getIdAsString()))
            return user2;
        if(user.getIdAsString().equalsIgnoreCase(user2.getIdAsString()))
            return user1;
        return null;
    }

    public User userIdToUser(String id) {
        if(user1.getIdAsString().equalsIgnoreCase(id))
            return user1;
        if(user2.getIdAsString().equalsIgnoreCase(id))
            return user2;
        return null;
    }

    public String userToId(User user) {
        if(user.getIdAsString().equalsIgnoreCase(user1.getIdAsString()))
            return user1.getIdAsString();
        if(user.getIdAsString().equalsIgnoreCase(user2.getIdAsString()))
            return user2.getIdAsString();
        return null;
    }

    public int[][] getBoard(User user) {
        if(user.getIdAsString().equalsIgnoreCase(user1.getIdAsString()))
            return board1;
        if(user.getIdAsString().equalsIgnoreCase(user2.getIdAsString()))
            return board2;
        return null;
    }

    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }

    public String getWhoseTurn() {
        return whoseTurn;
    }

    public User getWhoseTurnUser() {
        return userIdToUser(getWhoseTurn());
    }

}
