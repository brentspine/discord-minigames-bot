package de.brentspine.games;

import de.brentspine.games.util.UNOCard;
import de.brentspine.games.util.UNOColor;
import de.brentspine.Main;
import de.brentspine.util.Dice;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.ActionRowBuilder;
import org.javacord.api.entity.message.component.ButtonBuilder;
import org.javacord.api.entity.message.component.ButtonStyle;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ButtonInteraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class UNOGame {

    DiscordApi api = Main.getApi();

    private boolean running = false;

    private final User user1;
    private final User user2;
    private String messageID;
    private ArrayList<UNOCard> user1Hand = new ArrayList<>();
    private ArrayList<UNOCard> user2Hand = new ArrayList<>();
    private boolean user1Turn;
    private UNOCard onTable;
    private boolean gameEnded;
    private UNOColor userWish;
    private String lastAction;
    private Server server;
    private Channel channel;
    private HashMap<String, ButtonInteraction> moveMessagesMap;

    public UNOGame(User user1, User user2, Server server, Channel channel) {
        this.user1 = user1;
        this.user2 = user2;
        this.server = server;
        this.channel = channel;
        this.messageID = "";
        onTable = UNOCard.NONE;
        gameEnded = false;
        userWish = UNOColor.BLUE;
        lastAction = "Game started";
        moveMessagesMap = new HashMap<>();
    }


    public boolean makeMove(UNOCard unoCard, User user) throws ExecutionException, InterruptedException {
        if(!isMovePossible(unoCard)) {
            return false;
        }

        if(unoCard.getSecondArg().equalsIgnoreCase("p")) {
            getUserHand(getOtherUser(user)).add(UNOCard.generateRandom());
            getUserHand(getOtherUser(user)).add(UNOCard.generateRandom());
            setLastAction(user.getDisplayName(server) + " played " + unoCard.toEmoji() + " and " + getOtherUser(user).getDisplayName(server) + " had to draw 2 cards\n**[You should delete the old message now!](https://gfycat.com/scalyunevenjuliabutterfly)**");
            setWhoseTurn(getOtherUser(user));
        }
        else if(unoCard.getLast2Letters().equalsIgnoreCase("sh")) {
            setLastAction(user.getDisplayName(server) + " played " + unoCard.toEmoji() + "\n");
        }
        else if(unoCard.getLast2Letters().equalsIgnoreCase("h4")) {
            getUserHand(getOtherUser(user)).add(UNOCard.generateRandom());
            getUserHand(getOtherUser(user)).add(UNOCard.generateRandom());
            getUserHand(getOtherUser(user)).add(UNOCard.generateRandom());
            getUserHand(getOtherUser(user)).add(UNOCard.generateRandom());
            setLastAction(user.getDisplayName(server) + " played " + unoCard.toEmoji() + " and " + getOtherUser(user).getDisplayName(server) + " had to draw 4 cards\n**Wish: Selecting...**\n**[You should delete the old message now!](https://gfycat.com/scalyunevenjuliabutterfly)**");
        }
        else if(unoCard.getSecondArg().equalsIgnoreCase("e") || unoCard.getSecondArg().equalsIgnoreCase("r")) {
            setWhoseTurn(user);
            setLastAction(user.getDisplayName(server) + " played " + unoCard.toEmoji() + " and made " + getOtherUser(user).getDisplayName(server) + " expose 1 round");
        } else {
            setLastAction(user.getDisplayName(server) + " played " + unoCard.toEmoji());
            setWhoseTurn(getOtherUser(user));
        }

        getUserHand(user).remove(unoCard);
        onTable = unoCard;
        updateEmbed();


        return true;
    }

    public boolean isMovePossible(UNOCard unoCard) {
        if(unoCard.getColor().equals(onTable.getColor()) || unoCard.getSecondArg().equalsIgnoreCase(onTable.getSecondArg())) {
            return true;
        }
        if(unoCard.getColor().equals(UNOColor.SPECIAL)) {
            return true;
        }
        if(onTable.getColor().equals(UNOColor.SPECIAL)) {
            if(unoCard.getColor().equals(userWish)) {
                return true;
            }
            return false;
        }
        return false;
    }

    public UNOGame startGame() {
        run();
        for(int i = 0; i < 7; i++) {
            user1Hand.add(UNOCard.generateRandom());
            user2Hand.add(UNOCard.generateRandom());
        }
        onTable = UNOCard.generateRandom();

        user1Turn = false;
        if(new Dice(1, 2).roll() == 1) {
            user1Turn = true;
        }
        return this;
    }


    public ActionRow createActionRow() {
        KnownCustomEmoji unoEmoji = api.getCustomEmojiById("898558045590794300").get();

        ButtonBuilder buttonBuilder1 = new ButtonBuilder()
                .setStyle(ButtonStyle.PRIMARY)
                .setLabel("Make Move")
                .setCustomId("uno makemove");

        ButtonBuilder buttonBuilder2 = new ButtonBuilder()
                .setStyle(ButtonStyle.PRIMARY)
                .setLabel("My Hand")
                .setCustomId("uno myhand");

        ButtonBuilder buttonBuilder3 = new ButtonBuilder()
                .setStyle(ButtonStyle.PRIMARY)
                .setEmoji(unoEmoji)
                .setLabel("Draw one")
                .setCustomId("uno draw");

        ActionRowBuilder actionRowBuilder = new ActionRowBuilder()
                .addComponents(buttonBuilder1.build())
                .addComponents(buttonBuilder2.build())
                .addComponents(buttonBuilder3.build());
        return actionRowBuilder.build();
    }

    public ArrayList<ActionRow> createMoveActionRow(User user) {
        ArrayList<UNOCard> alreadyDefined = new ArrayList<>();
        ArrayList<ActionRow> r = new ArrayList<>();
        //Integer i = 0;
        ArrayList<UNOCard> hand = new ArrayList<>();
        for(UNOCard current : getUserHand(user)) {
            hand.add(current);
        }

        while (!hand.isEmpty()) {
            ActionRowBuilder actionRowBuilder = new ActionRowBuilder();
            for (int i = 0; i < 5 && !hand.isEmpty(); i++) {
                UNOCard current = hand.get(0);
                hand.remove(0);
                if(!alreadyDefined.contains(current)) {
                    KnownCustomEmoji emoji = api.getCustomEmojiById(current.getEmojiID()).get();
                    ButtonBuilder buttonBuilder = new ButtonBuilder()
                            .setStyle(ButtonStyle.PRIMARY)
                            .setEmoji(emoji)
                            .setLabel(current.name())
                            .setCustomId("uno move " + current.name() + " " + messageID);
                    actionRowBuilder.addComponents(buttonBuilder.build());
                    alreadyDefined.add(current);
                } else
                    i--;
            }
            r.add(actionRowBuilder.build());
        }

        return r;
    }

    public ActionRow createColorPickActionRow() {
        ButtonBuilder buttonBuilder1 = new ButtonBuilder()
                .setStyle(ButtonStyle.SUCCESS)
                .setLabel("Green")
                .setCustomId("uno color green " + messageID);

        ButtonBuilder buttonBuilder2 = new ButtonBuilder()
                .setStyle(ButtonStyle.SECONDARY)
                .setLabel("Yellow")
                .setCustomId("uno color yellow " + messageID);

        ButtonBuilder buttonBuilder3 = new ButtonBuilder()
                .setStyle(ButtonStyle.PRIMARY)
                .setLabel("Blue")
                .setCustomId("uno color blue " + messageID);

        ButtonBuilder buttonBuilder4 = new ButtonBuilder()
                .setStyle(ButtonStyle.DANGER)
                .setLabel("Red")
                .setCustomId("uno color red " + messageID);

        ActionRowBuilder actionRowBuilder = new ActionRowBuilder()
                .addComponents(buttonBuilder1.build())
                .addComponents(buttonBuilder2.build())
                .addComponents(buttonBuilder3.build())
                .addComponents(buttonBuilder4.build());
        return actionRowBuilder.build();
    }


    public boolean userIsOwner(User user) {
        if(user1.getIdAsString().equalsIgnoreCase(user.getIdAsString()) || user2.getIdAsString().equalsIgnoreCase(user.getIdAsString())) {
            return true;
        }
        return false;
    }

    public ArrayList<UNOCard> getUserHand(User user) {
        if(user.getIdAsString().equalsIgnoreCase(user1.getIdAsString())) {
            return user1Hand;
        }
        else if(user.getIdAsString().equalsIgnoreCase(user2.getIdAsString())) {
            return user2Hand;
        }
        return null;
    }

    public boolean isUsersTurn(User user) {
        if(user.getIdAsString().equalsIgnoreCase(user1.getIdAsString()) && isUser1Turn()) {
            return true;
        }
        if(user.getIdAsString().equalsIgnoreCase(user2.getIdAsString()) && !isUser1Turn()) {
            return true;
        }
        return false;
    }

    public User getOtherUser(User user) {
        if(user.getIdAsString().equalsIgnoreCase(user1.getIdAsString())) {
            return user2;
        }
        if(user.getIdAsString().equalsIgnoreCase(user2.getIdAsString())) {
            return user1;
        }
        return null;
    }

    public EmbedBuilder updateEmbed() throws ExecutionException, InterruptedException {
        if(user1Hand.size() == 0) {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("UNO: " + user1.getDisplayName(server) + " vs. " + user2.getDisplayName(server))
                    .setDescription("**" + user1.getMentionTag() + " won the Game!**")
                    .addField("Cards left:", user1.getDisplayName(server) + ": " + this.getUser1Hand().size() + "\n" + user2.getDisplayName(server) + ": " + this.getUser2Hand().size())
                    .setThumbnail(UNOCard.UNO.toEmoteURL());
            api.getMessageById(messageID, channel.asTextChannel().get()).get().edit(user1.getMentionTag() + " won the Game!", embedBuilder);
            return embedBuilder;
        }
        else if(user2Hand.size() == 0) {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("UNO: " + user1.getDisplayName(server) + " vs. " + user2.getDisplayName(server))
                    .setDescription("**" + user2.getMentionTag() + " won the Game!**")
                    .addField("Cards left:", user1.getDisplayName(server) + ": " + this.getUser1Hand().size() + "\n" + user2.getDisplayName(server) + ": " + this.getUser2Hand().size())
                    .setThumbnail(UNOCard.UNO.toEmoteURL());
            api.getMessageById(messageID, channel.asTextChannel().get()).get().edit(user2.getMentionTag() + " won the Game!", embedBuilder);
            return embedBuilder;
        }
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("UNO: " + user1.getDisplayName(server) + " vs. " + user2.getDisplayName(server))
                .setDescription("Current card: " + this.getOnTable() + "\n" +
                        "Last Action: " + this.getLastAction())
                .addField("Cards left:", user1.getDisplayName(server) + ": " + this.getUser1Hand().size() + "\n" + user2.getDisplayName(server) + ": " + this.getUser2Hand().size())
                .setThumbnail(this.getOnTable().toEmoteURL());
        api.getMessageById(messageID, channel.asTextChannel().get()).get().edit(this.getWhoseTurn().getNicknameMentionTag() + " its your turn", embedBuilder);
        return embedBuilder;
    }

    public void run() {
        if(!running) {
            running = true;
        }
    }

    public User getWhoseTurn() {
        if(user1Turn) {
            return user1;
        }
        return user2;
    }

    public void setWhoseTurn(User user) {
        if(user.getIdAsString().equalsIgnoreCase(user1.getIdAsString())) {
            user1Turn = true;
            return;
        }
        user1Turn = false;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    private boolean isUser1Turn() {
        return user1Turn;
    }

    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }

    public String getMessageID() {
        return messageID;
    }

    public ArrayList<UNOCard> getUser1Hand() {
        return user1Hand;
    }

    public ArrayList<UNOCard> getUser2Hand() {
        return user2Hand;
    }

    public UNOCard getOnTable() {
        return onTable;
    }

    public UNOColor getUserWish() {
        return userWish;
    }

    public void setUserWish(UNOColor userWish, User user) throws ExecutionException, InterruptedException {
        this.userWish = userWish;
        setLastAction(getWhoseTurn().getDisplayName(server) + " played " + onTable.toEmoji() + "\n**Wish: " + userWish.name().toLowerCase(Locale.ROOT) + "**");
        setWhoseTurn(getOtherUser(user));
        updateEmbed();
    }

    public String getLastAction() {
        return lastAction;
    }

    public Server getServer() {
        return server;
    }

    public void setLastAction(String lastAction) {
        this.lastAction = lastAction;
    }

    public HashMap<String, ButtonInteraction> getMoveMessagesMap() {
        return moveMessagesMap;
    }
}
