package de.brentspine.commands.gamecommands;

import de.brentspine.games.TicTacToeGame;
import de.brentspine.Main;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ButtonInteraction;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class TicTacToeCommand {

    DiscordApi api = Main.getApi();
    HashMap<String, TicTacToeGame> messageIDTTTGameMap = new HashMap<>();

    public TicTacToeCommand run() throws ExecutionException, InterruptedException {
        api.addMessageCreateListener(event -> {

            String[] args = event.getMessageContent().split(" ");

            if (args[0].startsWith("m!")) {

                Server server = event.getServer().get();
                TextChannel channel = event.getChannel();
                User user = event.getMessage().getUserAuthor().get();
                //Message message = event.getMessage();

                if (args[0].equalsIgnoreCase("m!tictactoe") || args[0].equalsIgnoreCase("m!ttt")) {
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
                    TicTacToeGame tttGame = new TicTacToeGame(user, versus, server, channel).sendConfirmationMessage();

                    messageIDTTTGameMap.put(tttGame.getMessageID(), tttGame);
                }

            }
        });

        api.addButtonClickListener(event -> {

            String[] args = event.getButtonInteraction().getCustomId().split(" ");
            ButtonInteraction buttonInteraction = event.getButtonInteraction();
            User user = buttonInteraction.getUser();
            TextChannel channel = buttonInteraction.getChannel().get().asTextChannel().get();
            Server server = buttonInteraction.getServer().get();

            if(args[0].equalsIgnoreCase("ttt")) {
                if(!messageIDTTTGameMap.containsKey(buttonInteraction.getMessage().get().getIdAsString())) {
                    buttonInteraction.acknowledge();
                    return;
                }
                TicTacToeGame tttGame = messageIDTTTGameMap.get(buttonInteraction.getMessage().get().getIdAsString());
                if (!tttGame.getMessageID().equalsIgnoreCase(buttonInteraction.getMessage().get().getIdAsString())) {
                    buttonInteraction.acknowledge();
                    return;
                }

                switch (args[1]) {
                    case "accept":
                        if(tttGame.canUserAccept(user.getIdAsString())) {
                            tttGame.startGame(buttonInteraction);
                        } else
                            buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL)
                                    .setContent("You are not permitted to do this")
                                    .respond();
                        break;
                    case "decline":
                        if(tttGame.isUserPlayer(user.getIdAsString())) {
                            channel.sendMessage("<@" + tttGame.getUser1().getIdAsString() +">\n" + user.getDisplayName(buttonInteraction.getServer().get()) + " declined the game request", tttGame.getConfirmationActionRow(true));
                            buttonInteraction.getMessage().get().delete();
                        } else
                            buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL)
                                    .setContent("You are not permitted to do this")
                                    .respond();
                        break;

                    case "move":
                        if(!tttGame.isUserPlayer(user.getIdAsString())) {
                            buttonInteraction.createImmediateResponder()
                                    .setFlags(MessageFlag.EPHEMERAL)
                                    .setContent("You are not permitted to do this")
                                    .respond();
                            return;
                        }
                        if(!tttGame.getWhoseTurn().equalsIgnoreCase(user.getIdAsString())) {
                            try {
                                buttonInteraction.createImmediateResponder()
                                        .setFlags(MessageFlag.EPHEMERAL)
                                        .setContent("It's " + api.getUserById(tttGame.getWhoseTurn()).get().getDisplayName(server) + "'s turn")
                                        .respond();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                        if(!tttGame.isMovePossible(args[2], args[3])) {
                            buttonInteraction.createImmediateResponder()
                                    .setFlags(MessageFlag.EPHEMERAL)
                                    .setContent("This field is already used")
                                    .respond();
                            return;
                        }
                        tttGame.makeMove(args[2], args[3], user);
                        tttGame.updateGameBoard(buttonInteraction);
                        break;

                    default:
                        buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent("<@" + user.getIdAsString() + ">\nUnknown interaction `" + buttonInteraction.getCustomId() + "`").respond();
                        System.out.println("Unknown interaction: \"" + buttonInteraction.getCustomId() + "\"");
                        break;
                }
            }

        });
        return this;
    }
}
