package de.brentspine.commands.gamecommands;

import de.brentspine.Main;
import de.brentspine.games.BattleShipsGame;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ButtonInteraction;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class BattleShipsCommand {

    DiscordApi api = Main.getApi();
    HashMap<String, BattleShipsGame> games = new HashMap<>();
    HashMap<String, Integer> boardSites = new HashMap<>();

    public BattleShipsCommand run() throws ExecutionException, InterruptedException {
        api.addMessageCreateListener(event -> {

            String[] args = event.getMessageContent().split(" ");

            if (args[0].startsWith("m!")) {

                Server server = event.getServer().get();
                TextChannel channel = event.getChannel();
                User user = event.getMessage().getUserAuthor().get();
                //Message message = event.getMessage();

                //

                if (args[0].equalsIgnoreCase("m!bs") || args[0].equalsIgnoreCase("m!battleships") || args[0].equalsIgnoreCase("m!bship")) {
                    if(event.getMessage().getMentionedUsers().isEmpty()) {
                        channel.sendMessage("Please mention a user to play with");
                        return;
                    }
                    User versus = event.getMessage().getMentionedUsers().get(0);
                    if(versus.getIdAsString().equalsIgnoreCase(user.getIdAsString())) {
                        channel.sendMessage("You can't challenge yourself");
                        return;
                    }
                    if(versus.isBot()) {
                        channel.sendMessage("At least for now it is not possible to challenge bots");
                        return;
                    }
                    BattleShipsGame battleShipsGame = new BattleShipsGame(user, versus, server, channel).sendConfirmationMessage();

                    games.put(battleShipsGame.getMessageID(), battleShipsGame);
                }

            }
        });

        api.addButtonClickListener(event -> {

            String[] args = event.getButtonInteraction().getCustomId().split(" ");
            ButtonInteraction buttonInteraction = event.getButtonInteraction();
            User user = buttonInteraction.getUser();
            TextChannel channel = buttonInteraction.getChannel().get().asTextChannel().get();

            if (args[0].equalsIgnoreCase("battleships")) {
                if(!buttonInteraction.getMessage().isPresent()) {
                    BattleShipsGame battleShipsGame = games.get(args[3]);
                    switch (args[1]) {
                        case "move":
                            battleShipsGame = games.get(args[2]);
                            int page = boardSites.get(args[2]);
                            int coordinate = Integer.valueOf(args[4]);
                            if (args[3].equalsIgnoreCase("0")) {
                                //args[2] is uuid to get the page
                                //user pressed a letter, so args[3] is the x coordinate
                                //args[4] is a number from 0 to 11
                                int[][] board;
                                if(page == 1)
                                    board = battleShipsGame.getBoard(battleShipsGame.getOtherUser(user));
                                else
                                    board = battleShipsGame.getBoard(user);
                                battleShipsGame.sendBoard(buttonInteraction, page, board, false, true, coordinate);
                            } else {
                                //user pressed a number, so args[3] is the y coordinate
                                //args[4] is a number from 0 to 11
                                //args[5] is the first move they made
                                int firstMove = Integer.valueOf(args[5]);
                                int[][] board;
                                if(page == 1)
                                    board = battleShipsGame.getBoard(battleShipsGame.getOtherUser(user));
                                else
                                    board = battleShipsGame.getBoard(user);
                                battleShipsGame.moveSetBoatsPhase(firstMove, coordinate, user);
                                battleShipsGame.sendBoard(buttonInteraction, page, board, false, false);
                            }
                            break;

                        case "board":
                            if (args[2].equalsIgnoreCase("opponent")) {
                                try {
                                    battleShipsGame.sendBoard(buttonInteraction, 1, battleShipsGame.getBoard(battleShipsGame.getOtherUser(user)), false, false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            else if (args[2].equalsIgnoreCase("you")) {
                                try {
                                    battleShipsGame.sendBoard(buttonInteraction, 2, battleShipsGame.getBoard(user), false, false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                    }
                    return;
                }
                Server server = buttonInteraction.getServer().get();
                if (!games.containsKey(buttonInteraction.getMessage().get().getIdAsString())) {
                    buttonInteraction.acknowledge();
                    return;
                }
                BattleShipsGame battleShipsGame = games.get(buttonInteraction.getMessage().get().getIdAsString());
                if (!battleShipsGame.getMessageID().equalsIgnoreCase(buttonInteraction.getMessage().get().getIdAsString())) {
                    buttonInteraction.acknowledge();
                    return;
                }
                switch (args[1]) {

                    case "settings":
                        if(!battleShipsGame.isUserPlayer(user.getIdAsString())) {
                            sendNotEnoughPerms(buttonInteraction);
                            return;
                        }
                        if(battleShipsGame.canUserAccept(user.getIdAsString())) {
                            buttonInteraction.createImmediateResponder()
                                    .setFlags(MessageFlag.EPHEMERAL)
                                    .setContent("You are not allowed to change the settings since you didn't start the game")
                                    .respond();
                            return;
                        }
                        int amountAdd = 0;
                        if(args.length < 4)
                            amountAdd = 0;
                        else if(args[3].equalsIgnoreCase("plus"))
                            amountAdd = 1;
                        else if(args[3].equalsIgnoreCase("minus"))
                            amountAdd = -1;
                        int currentAmount;
                        switch (args[2]) {
                            case "start":
                                try {
                                    battleShipsGame.startInGame(buttonInteraction);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;

                            case "submarine":
                                currentAmount = battleShipsGame.getShips1();
                                if((currentAmount + amountAdd) > 12) {
                                    buttonInteraction.createImmediateResponder()
                                            .setFlags(MessageFlag.EPHEMERAL)
                                            .setContent(user.getMentionTag() + "\nYou can't have more than 12 " + args[2])
                                            .respond();
                                    return;
                                }
                                if((currentAmount + amountAdd) < 0) {
                                    buttonInteraction.createImmediateResponder()
                                            .setFlags(MessageFlag.EPHEMERAL)
                                            .setContent(user.getMentionTag() + "\nYou already disabled this ship")
                                            .respond();
                                    return;
                                }
                                if(args[3].equalsIgnoreCase("description"))
                                    buttonInteraction.createImmediateResponder()
                                            .setFlags(MessageFlag.EPHEMERAL)
                                            .setContent("The smallest of them all\n`Length:` 1\n`Amount:` " + currentAmount)
                                            .respond();
                                else
                                    battleShipsGame.setShips1(currentAmount + amountAdd);
                                try {
                                    battleShipsGame.sendSettings(buttonInteraction, false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;

                            case "scouter":
                                currentAmount = battleShipsGame.getShips2();
                                if((currentAmount + amountAdd) > 6) {
                                    buttonInteraction.createImmediateResponder()
                                            .setFlags(MessageFlag.EPHEMERAL)
                                            .setContent(user.getMentionTag() + "\nYou can't have more than 6 " + args[2])
                                            .respond();
                                    return;
                                }
                                if((currentAmount + amountAdd) < 0) {
                                    buttonInteraction.createImmediateResponder()
                                            .setFlags(MessageFlag.EPHEMERAL)
                                            .setContent(user.getMentionTag() + "\nYou already disabled this ship")
                                            .respond();
                                    return;
                                }
                                if(args[3].equalsIgnoreCase("description"))
                                    buttonInteraction.createImmediateResponder()
                                            .setFlags(MessageFlag.EPHEMERAL)
                                            .setContent("Easy to hide in some corner\n`Length:` 2\n`Amount:` " + battleShipsGame.getShips2())
                                            .respond();
                                else
                                    battleShipsGame.setShips2(battleShipsGame.getShips2() + amountAdd);
                                try {
                                    battleShipsGame.sendSettings(buttonInteraction, false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;

                            case "destroyer":
                                currentAmount = battleShipsGame.getShips3();
                                if((currentAmount + amountAdd) > 4) {
                                    buttonInteraction.createImmediateResponder()
                                            .setFlags(MessageFlag.EPHEMERAL)
                                            .setContent(user.getMentionTag() + "\nYou can't have more than 4 " + args[2])
                                            .respond();
                                    return;
                                }
                                if((currentAmount + amountAdd) < 0) {
                                    buttonInteraction.createImmediateResponder()
                                            .setFlags(MessageFlag.EPHEMERAL)
                                            .setContent(user.getMentionTag() + "\nYou already disabled this ship")
                                            .respond();
                                    return;
                                }
                                if(args[3].equalsIgnoreCase("description"))
                                    buttonInteraction.createImmediateResponder()
                                            .setFlags(MessageFlag.EPHEMERAL)
                                            .setContent("The golden mid, destroys 'em all\n`Length:` 3\n`Amount:` " + battleShipsGame.getShips3())
                                            .respond();
                                else
                                    battleShipsGame.setShips3(battleShipsGame.getShips3() + amountAdd);
                                try {
                                    battleShipsGame.sendSettings(buttonInteraction, false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;

                            case "cruiser":
                                currentAmount = battleShipsGame.getShips4();
                                if((currentAmount + amountAdd) > 3) {
                                    buttonInteraction.createImmediateResponder()
                                            .setFlags(MessageFlag.EPHEMERAL)
                                            .setContent(user.getMentionTag() + "\nYou can't have more than 3 " + args[2])
                                            .respond();
                                    return;
                                }
                                if((currentAmount + amountAdd) < 0) {
                                    buttonInteraction.createImmediateResponder()
                                            .setFlags(MessageFlag.EPHEMERAL)
                                            .setContent(user.getMentionTag() + "\nYou already disabled this ship")
                                            .respond();
                                    return;
                                }
                                if(args[3].equalsIgnoreCase("description"))
                                    buttonInteraction.createImmediateResponder()
                                            .setFlags(MessageFlag.EPHEMERAL)
                                            .setContent("This could cause some serious damage... But they aren't shooting. It's just easy to hit then\n`Length:` 4\n`Amount:` " + battleShipsGame.getShips4())
                                            .respond();
                                else
                                    battleShipsGame.setShips4(battleShipsGame.getShips4() + amountAdd);
                                try {
                                    battleShipsGame.sendSettings(buttonInteraction, false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;

                            case "aircraftcarrier":
                                currentAmount = battleShipsGame.getShips5();
                                if((currentAmount + amountAdd) > 2) {
                                    buttonInteraction.createImmediateResponder()
                                            .setFlags(MessageFlag.EPHEMERAL)
                                            .setContent(user.getMentionTag() + "\nYou can't have more than 2 " + args[2])
                                            .respond();
                                    return;
                                }
                                if((currentAmount + amountAdd) < 0) {
                                    buttonInteraction.createImmediateResponder()
                                            .setFlags(MessageFlag.EPHEMERAL)
                                            .setContent(user.getMentionTag() + "\nYou already disabled this ship")
                                            .respond();
                                    return;
                                }
                                if(args[3].equalsIgnoreCase("description"))
                                    buttonInteraction.createImmediateResponder()
                                            .setFlags(MessageFlag.EPHEMERAL)
                                            .setContent("The biggest of them all, easy to find but hard to destroy ||wait what :flushed:||\n`Length:` 5\n`Amount:` " + battleShipsGame.getShips5())
                                            .respond();
                                else
                                    battleShipsGame.setShips5(battleShipsGame.getShips5() + amountAdd);
                                try {
                                    battleShipsGame.sendSettings(buttonInteraction, false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;

                            default:
                                sendUnknownInteraction(buttonInteraction, user);
                                break;
                        }
                        break;

                    case "accept":
                        if (battleShipsGame.canUserAccept(user.getIdAsString())) {
                            try {
                                battleShipsGame.startGame(buttonInteraction);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else
                            buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL)
                                    .setContent("You are not permitted to do this")
                                    .respond();
                        break;
                    case "decline":
                        if (battleShipsGame.isUserPlayer(user.getIdAsString())) {
                            channel.sendMessage("<@" + battleShipsGame.getUser1().getIdAsString() + ">\n" + user.getDisplayName(buttonInteraction.getServer().get()) + " declined the game request", battleShipsGame.getConfirmationActionRow(true));
                            buttonInteraction.getMessage().get().delete();
                        } else
                            buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL)
                                    .setContent("You are not permitted to do this")
                                    .respond();
                        break;

                    case "showboard":
                        if(battleShipsGame.isUserPlayer(user.getIdAsString())) {
                            try {
                                battleShipsGame.sendBoard(buttonInteraction, 1, battleShipsGame.getBoard(battleShipsGame.getOtherUser(user)), true, false);
                            } catch (Exception e) {
                                channel.sendMessage(user.getMentionTag() + " failed to show board, please try again or contact a developer `m!support`");
                            }
                        } else
                            buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL)
                                    .setContent("You are not permitted to do this")
                                    .respond();
                        break;

                    default:
                        sendUnknownInteraction(buttonInteraction, user);
                        break;
                }
            }
        });
        return this;
    }

    public void sendUnknownInteraction(ButtonInteraction buttonInteraction, User user) {
        buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent("<@" + user.getIdAsString() + ">\nUnknown interaction `" + buttonInteraction.getCustomId() + "`").respond();
        System.out.println("Unknown interaction: \"" + buttonInteraction.getCustomId() + "\"");
    }

    public void sendNotEnoughPerms(ButtonInteraction buttonInteraction) {
        buttonInteraction.createImmediateResponder()
                .setFlags(MessageFlag.EPHEMERAL)
                .setContent("You are not allowed to do that")
                .respond();
    }

    public HashMap<String, BattleShipsGame> getGames() {
        return games;
    }

    public DiscordApi getApi() {
        return api;
    }

    public HashMap<String, Integer> getBoardSites() {
        return boardSites;
    }

}
