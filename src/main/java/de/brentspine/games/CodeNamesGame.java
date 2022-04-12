package de.brentspine.games;

import de.brentspine.games.util.CodeNamesRole;
import de.brentspine.games.util.CodeNamesWords;
import de.brentspine.Main;
import de.brentspine.util.Dice;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.ActionRowBuilder;
import org.javacord.api.entity.message.component.ButtonBuilder;
import org.javacord.api.entity.message.component.ButtonStyle;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class CodeNamesGame {

    DiscordApi api = Main.getApi();

    private ArrayList<User> players = new ArrayList<User>();
    private String mainMessageID;
    private Server server;
    private TextChannel channel;
    private User startedBy;
    private boolean readyToStart = false;
    private ArrayList<User> kickedPlayers = new ArrayList<>();
    public HashMap<User, CodeNamesRole> roles = new HashMap<>();
    public CodeNamesWords [] [] gameBoard;
    public Color [] [] gameBoardColor;

    public CodeNamesGame(Server server, TextChannel channel, User... users) {
        this.server = server;
        this.channel = channel;
        for (User current : users) {
            players.add(current);
        }
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                gameBoard[i][j] = CodeNamesWords.values()[new Dice(0, CodeNamesWords.values().length).roll()];
                Dice dice = new Dice(1, 3);
                dice.roll();
                if(dice.getResult() == 1) {

                }
            }
        }
    }

    public CodeNamesGame(User startedBy) {
        this.startedBy = startedBy;
        players.add(startedBy);
    }

    public String sendJoinMessage() throws ExecutionException, InterruptedException {

        ButtonBuilder buttonBuilder1 = new ButtonBuilder()
                .setStyle(ButtonStyle.PRIMARY)
                .setLabel("Join")
                .setCustomId("codenames join");

        ButtonBuilder buttonBuilder2 = new ButtonBuilder()
                .setStyle(ButtonStyle.PRIMARY)
                .setLabel("Leave")
                .setCustomId("codenames leave");

        ButtonBuilder buttonBuilder3 = new ButtonBuilder()
                .setStyle(ButtonStyle.PRIMARY)
                .setLabel("Start")
                .setCustomId("codenames start");

        ButtonBuilder buttonBuilder4 = new ButtonBuilder()
                .setStyle(ButtonStyle.PRIMARY)
                .setLabel("Kick a player")
                .setCustomId("codenames kick message");

        ActionRowBuilder actionRowBuilder = new ActionRowBuilder()
                .addComponents(buttonBuilder1.build())
                .addComponents(buttonBuilder2.build())
                .addComponents(buttonBuilder3.build())
                .addComponents(buttonBuilder4.build());
        ActionRow actionRow = actionRowBuilder.build();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("CodeNames started by " + startedBy.getDisplayName(server))
                .setDescription("Waiting for the Game to start\n\n**INFORMATION**\nPlayers: " + players.size() + "\nPlayers required: 4\n\nReady to start  " + getReadyToStartEmote())
                .setColor(getReadyToStartColor());
        Message message = channel.sendMessage("Waiting for players to join...", embedBuilder, actionRow).get();
        return message.getIdAsString();
    }

    public String sendSelectTeamMessage() throws ExecutionException, InterruptedException {

        ButtonBuilder buttonBuilder1 = new ButtonBuilder()
                .setStyle(ButtonStyle.PRIMARY)
                .setLabel("⠀⠀Operative⠀⠀")
                .setCustomId("codenames team blue operative");

        ButtonBuilder buttonBuilder2 = new ButtonBuilder()
                .setStyle(ButtonStyle.PRIMARY)
                .setLabel("⠀⠀Spymaster⠀⠀")
                .setCustomId("codenames team blue spymaster");

        ButtonBuilder buttonBuilder3 = new ButtonBuilder()
                .setStyle(ButtonStyle.DANGER)
                .setLabel("⠀⠀Operative⠀⠀")
                .setCustomId("codenames team red operative");

        ButtonBuilder buttonBuilder4 = new ButtonBuilder()
                .setStyle(ButtonStyle.DANGER)
                .setLabel("⠀⠀Spymaster⠀⠀")
                .setCustomId("codenames team red spymaster");

        ButtonBuilder buttonBuilder5 = new ButtonBuilder()
                .setStyle(ButtonStyle.SUCCESS)
                .setLabel("Start")
                .setCustomId("codenames team start");

        ButtonBuilder buttonBuilder6 = new ButtonBuilder()
                .setStyle(ButtonStyle.SECONDARY)
                .setLabel("End")
                .setCustomId("codenames team start");

        ActionRowBuilder actionRowBuilder1 = new ActionRowBuilder()
                .addComponents(buttonBuilder1.build())
                .addComponents(buttonBuilder3.build());
        ActionRow actionRow1 = actionRowBuilder1.build();
        ActionRowBuilder actionRowBuilder2 = new ActionRowBuilder()
                .addComponents(buttonBuilder2.build())
                .addComponents(buttonBuilder4.build());
        ActionRow actionRow2 = actionRowBuilder2.build();
        ActionRowBuilder actionRowBuilder3 = new ActionRowBuilder()
                .addComponents(buttonBuilder5.build())
                .addComponents(buttonBuilder6.build());
        ActionRow actionRow3 = actionRowBuilder3.build();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("CodeNames started by " + startedBy.getDisplayName(server))
                .setDescription("**INFO**\nWaiting for players to select a role and team\n⠀")
                .addInlineField("Blue Team  :blue_circle:", "⠀\n**Operative(s)**\n" + getUserListForRoleAsString(CodeNamesRole.BLUE_OPERATIVE) + "\n**Spymaster(s)**\n" + getUserListForRoleAsString(CodeNamesRole.BLUE_SPYMASTER))
                .addInlineField("Red Team  :red_circle:", "⠀\n**Operative(s)**\n" + getUserListForRoleAsString(CodeNamesRole.RED_OPERATIVE) + "\n**Spymaster(s)**\n" + getUserListForRoleAsString(CodeNamesRole.RED_SPYMASTER))
                .setColor(Color.BLACK);
        api.getMessageById(mainMessageID, channel).get().delete();
        Message message = channel.sendMessage("Select a team and role...", embedBuilder, actionRow1, actionRow2, actionRow3).get();
        mainMessageID = message.getIdAsString();
        return message.getIdAsString();
    }

    public String sendGameMessage() throws ExecutionException, InterruptedException {

        ButtonBuilder buttonBuilder1 = new ButtonBuilder()
                .setStyle(ButtonStyle.PRIMARY)
                .setLabel("Press here")
                .setCustomId("codenames presshere");

        ActionRowBuilder actionRowBuilder1 = new ActionRowBuilder()
                .addComponents(buttonBuilder1.build());
        ActionRow actionRow1 = actionRowBuilder1.build();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("CodeNames started by " + startedBy.getDisplayName(server))
                .setDescription("**Hint by None**\n`Example` `1`\n⠀")
                .addInlineField("Blue Team  :blue_circle:", "⠀\n**Operative(s)**\n" + getUserListForRoleAsString(CodeNamesRole.BLUE_OPERATIVE) + "\n**Spymaster(s)**\n" + getUserListForRoleAsString(CodeNamesRole.BLUE_SPYMASTER) + "\n\n")
                .addInlineField("Red Team  :red_circle:", "⠀\n**Operative(s)**\n" + getUserListForRoleAsString(CodeNamesRole.RED_OPERATIVE) + "\n**Spymaster(s)**\n" + getUserListForRoleAsString(CodeNamesRole.RED_SPYMASTER) + "\n\n")
                .setColor(Color.BLACK);
        api.getMessageById(mainMessageID, channel).get().delete();
        Message message = channel.sendMessage("Select a team and role...", embedBuilder, actionRow1).get();
        mainMessageID = message.getIdAsString();
        return message.getIdAsString();
    }

    public void updateJoinMessage() throws ExecutionException, InterruptedException {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("CodeNames started by " + startedBy.getDisplayName(server))
                .setDescription("Waiting for the Game to start\n\n**INFORMATION**\nPlayers: " + players.size() + "\nPlayers required: 4\n\nReady to start  " + getReadyToStartEmote())
                .setColor(getReadyToStartColor());
        api.getMessageById(Long.valueOf(mainMessageID), channel).get().edit("Waiting for players to join...", embedBuilder);
    }

    public void updateSelectTeamMessage() throws ExecutionException, InterruptedException {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("CodeNames started by " + startedBy.getDisplayName(server))
                .setDescription("**INFO**\nWaiting for players to select a role and team\n⠀")
                .addInlineField("Blue Team  :blue_circle:", "⠀\n**Operative(s)**\n" + getUserListForRoleAsString(CodeNamesRole.BLUE_OPERATIVE) + "\n**Spymaster(s)**\n" + getUserListForRoleAsString(CodeNamesRole.BLUE_SPYMASTER))
                .addInlineField("Red Team  :red_circle:", "⠀\n**Operative(s)**\n" + getUserListForRoleAsString(CodeNamesRole.RED_OPERATIVE) + "\n**Spymaster(s)**\n" + getUserListForRoleAsString(CodeNamesRole.RED_SPYMASTER))
                .setColor(Color.BLACK);
        api.getMessageById(Long.valueOf(mainMessageID), channel).get().edit("Select a team and role...", embedBuilder);
    }

    public void joinPlayer(User user) throws ExecutionException, InterruptedException {
        players.add(user);
        updateJoinMessage();
    }

    public void leavePlayer(User user) throws ExecutionException, InterruptedException {
        players.remove(user);
        updateJoinMessage();
    }


    public ArrayList<ActionRow> createKickPlayerActionRow() {
        ArrayList<User> alreadyDefined = new ArrayList<>();
        ArrayList<ActionRow> r = new ArrayList<>();
        //Integer i = 0;
        ArrayList<User> users = new ArrayList<>();
        for(User current : players) {
            users.add(current);
        }

        while (!users.isEmpty()) {
            ActionRowBuilder actionRowBuilder = new ActionRowBuilder();
            for (int i = 0; i < 5 && !users.isEmpty(); i++) {
                User current = users.get(0);
                users.remove(0);
                if(!alreadyDefined.contains(current) && current.getId() != startedBy.getId()) {
                    ButtonBuilder buttonBuilder = new ButtonBuilder()
                            .setStyle(ButtonStyle.PRIMARY)
                            .setLabel(current.getDiscriminatedName())
                            .setCustomId("codenames kick " + current.getIdAsString() + " " + mainMessageID);
                    actionRowBuilder.addComponents(buttonBuilder.build());
                    alreadyDefined.add(current);
                } else
                    i--;
            }
            r.add(actionRowBuilder.build());
        }

        return r;
    }

    private ArrayList<User> getUserListForRole(CodeNamesRole role) {
        ArrayList<User> r = new ArrayList<>();
        for(User user : players) {
            if(roles.get(user) == role) {
                r.add(user);
            }
        }
        return r;
    }

    private String getUserListForRoleAsString(CodeNamesRole role) {
        String r = "";
        for(User user : players) {
            if(roles.get(user) == role) {
                r = r + user.getDisplayName(server) + "\n";
            }
        }
        if(r == "") {
            return "none\n";
        }
        return r;
    }

    private Color getReadyToStartColor() {
        if(players.size() >= 4) {
            return Color.GREEN;
        } else
            return Color.RED;

    }

    private String getReadyToStartEmote() {
        if(players.size() >= 4) {
            return ":white_check_mark:";
        } else
            return ":no_entry_sign:";
    }

    private Integer getNumberOfColor(Color color) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {

            }
        }
        return 0;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void setChannel(TextChannel channel) {
        this.channel = channel;
    }

    public void setMainMessageID(String mainMessageID) {
        this.mainMessageID = mainMessageID;
    }

    public ArrayList<User> getPlayers() {
        return players;
    }

    public Server getServer() {
        return server;
    }

    public TextChannel getChannel() {
        return channel;
    }

    public User getStartedBy() {
        return startedBy;
    }

    public boolean isReadyToStart() {
        return readyToStart;
    }

    public ArrayList<User> getKickedPlayers() {
        return kickedPlayers;
    }

    public void kickPlayer(User user) throws ExecutionException, InterruptedException {
        kickedPlayers.add(user);
        players.remove(user);
        updateJoinMessage();
    }

    public void kickPlayer(String userID) throws ExecutionException, InterruptedException {
        User user = api.getUserById(userID).get();
        kickedPlayers.add(user);
        players.remove(user);
        updateJoinMessage();
    }

    public String getMainMessageID() {
        return mainMessageID;
    }
}
