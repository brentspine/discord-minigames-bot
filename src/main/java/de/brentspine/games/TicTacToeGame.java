package de.brentspine.games;

import de.brentspine.Main;
import de.brentspine.util.Dice;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.ActionRowBuilder;
import org.javacord.api.entity.message.component.ButtonBuilder;
import org.javacord.api.entity.message.component.ButtonStyle;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ButtonInteraction;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class TicTacToeGame {

    DiscordApi api = Main.getApi();

    private boolean running = false;

    private final User user1;
    private final User user2;
    private String messageID;
    private Message message;
    private int[][] board;
    private boolean gameEnded;
    private String lastAction;
    private Server server;
    private TextChannel channel;
    private String whoseTurn;

    private int phase;

    public TicTacToeGame(User user1, User user2, Server server, TextChannel channel) {
        this.user1 = user1;
        this.user2 = user2;
        this.server = server;
        this.channel = channel;
        this.messageID = "";
        this.board = new int[3][3];
        gameEnded = false;
        lastAction = "Game started";
        if(Dice.generateRandomNumberBetween(1, 2) == 1)
            whoseTurn = user1.getIdAsString();
        else
            whoseTurn = user2.getIdAsString();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = 0;
            }
        }
        phase = 0;
    }

    public boolean makeMove(String x, String y, User user) {
        return makeMove(Integer.valueOf(x), Integer.valueOf(y), user);
    }

    public boolean makeMove(int x, int y, User user) {
        if(!isMovePossible(x, y)) {
            return false;
        }
        if(user.getIdAsString().equalsIgnoreCase(user1.getIdAsString())) {
            board[x][y] = 1;
            whoseTurn = user2.getIdAsString();
        } else {
            board[x][y] = 2;
            whoseTurn = user1.getIdAsString();
        }
        return true;
    }

    public boolean hasWinner() {
        return !getWinner().equalsIgnoreCase("");
    }

    public String getWinner() {
        switch (privateGetWinner()) {
            case "1":
                return user1.getIdAsString();
            case "2":
                return user2.getIdAsString();
            default:
            case "0":
            case "-1":
                return "";
        }
    }

    private String privateGetWinner() {
        for (int a = 0; a < 8; a++) {
            String line = null;

            switch (a) {
                case 0:
                    line = String.valueOf(board[0][0]) + board[0][1] + board[0][2];
                    break;
                case 1:
                    line = String.valueOf(board[1][0]) + board[1][1] + board[1][2];
                    break;
                case 2:
                    line = String.valueOf(board[2][0]) + board[2][1] + board[2][2];
                    break;
                case 3:
                    line = String.valueOf(board[0][0]) + board[1][0] + board[2][0];
                    break;
                case 4:
                    line = String.valueOf(board[0][1]) + board[1][1] + board[2][1];
                    break;
                case 5:
                    line = String.valueOf(board[0][2]) + board[1][2] + board[2][2];
                    break;
                case 6:
                    line = String.valueOf(board[0][0]) + board[1][1] + board[2][2];
                    break;
                case 7:
                    line = String.valueOf(board[0][2]) + board[1][1] + board[2][0];
                    break;
            }
            //For X winner
            if (line.equals("111")) {
                return "1";
            }

            // For O winner
            else if (line.equals("222")) {
                return "2";
            }
        }

        String r = "-1";
        for (int a = 0; a < board.length; a++) {
            for(int[] c : Arrays.asList(board)) {
                if(Arrays.asList(c).contains(0))
                    r = "0";
            }
        }
        return r;
    }

    public boolean isMovePossible(String x, String y) {
        return isMovePossible(Integer.valueOf(x), Integer.valueOf(y));
    }

    public boolean isMovePossible(int x, int y) {
        if(board[x][y] == 0) return true;
        return false;
    }

    public TicTacToeGame sendConfirmationMessage() {
        phase = 1;
        try {
            message = channel.sendMessage("<@" + user2.getIdAsString() + ">\n" + user1.getDisplayName(server) + " has challenged you for a game of TicTacToe", getConfirmationActionRow(false)).get();
            messageID = message.getIdAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Message message = channel.sendMessage(unoGame.getWhoseTurn().getNicknameMentionTag() + " its your turn", embedBuilder, unoGame.createActionRow()).get();
        return this;
    }

    public ActionRow getConfirmationActionRow(boolean disabled) {
        ButtonBuilder buttonBuilder1 = new ButtonBuilder()
                .setStyle(ButtonStyle.SUCCESS)
                .setLabel("Accept")
                .setCustomId("ttt accept")
                .setDisabled(disabled);

        ButtonBuilder buttonBuilder2 = new ButtonBuilder()
                .setStyle(ButtonStyle.DANGER)
                .setLabel("Decline")
                .setCustomId("ttt decline")
                .setDisabled(disabled);

        ActionRowBuilder actionRowBuilder = new ActionRowBuilder()
                .addComponents(buttonBuilder1.build())
                .addComponents(buttonBuilder2.build());
        return actionRowBuilder.build();
    }

    public TicTacToeGame startGame(ButtonInteraction buttonInteraction) {
        phase = 2;
        updateGameBoard(buttonInteraction);
        return this;
    }

    public TicTacToeGame updateGameBoard(ButtonInteraction buttonInteraction) {
        if(hasWinner()) {
            String winnerID = getWinner();
            buttonInteraction.createOriginalMessageUpdater()
                    .setContent("**" + user1.getDisplayName(server) + " vs. " + user2.getDisplayName(server) + "**\n\n" +
                            "<@" + winnerID + "> won the game!")
                    .addComponents(getGameActionRow(true))
                    .update();
            return this;
        }
        try {
            buttonInteraction.createOriginalMessageUpdater().setContent("**" + user1.getDisplayName(server) + " vs. " + user2.getDisplayName(server) + "**\n\n" +
                    "It's <@" + api.getUserById(whoseTurn).get().getIdAsString() + "> turn").addComponents(getGameActionRow(false)).update();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public ActionRow[] getGameActionRow(boolean disabled) {
        int x = 0;
        ActionRow[] rows = new ActionRow[3];
        for(int[] c : board) {
            int y = 0;
            ActionRowBuilder actionRowBuilder = new ActionRowBuilder();
            for(int cc : c) {
                ButtonBuilder buttonBuilder1 = new ButtonBuilder()
                        .setStyle(ButtonStyle.SUCCESS)
                        .setEmoji(getEmoji(cc))
                        .setLabel("")
                        .setCustomId("ttt move " + x + " " + y)
                        .setDisabled(disabled || !isMovePossible(x, y));

                actionRowBuilder.addComponents(buttonBuilder1.build());
                y++;
            }
            rows[x] = actionRowBuilder.build();
            x++;
        }
        return rows;
    }

    public KnownCustomEmoji getEmoji(int i) {
        switch (i) {
            case 0:
                return api.getCustomEmojiById("953376687763124295").get();
            case 1:
                return api.getCustomEmojiById("953372617233031248").get();
            case 2:
                return api.getCustomEmojiById("953372461351706674").get();
            default:
                return api.getCustomEmojiById("953373618690207754").get();
        }
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
        if(user1.getIdAsString().equalsIgnoreCase(id) || user2.getIdAsString().equalsIgnoreCase(id))
            return true;
        return false;
    }

    public boolean canUserAccept(String id) {
        if(user2.getIdAsString().equalsIgnoreCase(id))
            return true;
        return false;
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

}
