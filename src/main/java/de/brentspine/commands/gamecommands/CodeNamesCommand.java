package de.brentspine.commands.gamecommands;

import de.brentspine.games.CodeNamesGame;
import de.brentspine.games.util.CodeNamesRole;
import de.brentspine.Main;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

import java.util.HashMap;

public class CodeNamesCommand {

    DiscordApi api = Main.getApi();
    HashMap<String, CodeNamesGame> messageIDCodeNamesMap = new HashMap<>();

    public CodeNamesCommand run() {
        api.addMessageCreateListener(event -> {

            String[] args = event.getMessageContent().split(" ");

            if (args[0].startsWith("m!")) {

                Server server = event.getServer().get();
                TextChannel channel = event.getChannel();
                User user = event.getMessage().getUserAuthor().get();

                if (args[0].equalsIgnoreCase("m!codenames")) {
                    try {
                        if(args.length >= 2) {
                            if(args[1].equalsIgnoreCase("debug")) {

                            }
                            if(event.getMessage().getMentionedUsers().isEmpty()) {
                                channel.sendMessage("Mention a user you want to play with!");
                                return;
                            }
                            CodeNamesGame codeNamesGame = new CodeNamesGame(user);
                            codeNamesGame.setServer(server);
                            codeNamesGame.setChannel(channel);
                            String messageID = codeNamesGame.sendJoinMessage();
                            codeNamesGame.setMainMessageID(messageID);
                            messageIDCodeNamesMap.put(messageID, codeNamesGame);
                        } else
                            channel.sendMessage("Mention a user you want to play with!");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

        });


        api.addButtonClickListener(event -> {
            ButtonInteraction buttonInteraction = event.getButtonInteraction();
            String clickID = buttonInteraction.getCustomId();
            TextChannel channel = buttonInteraction.getChannel().get();
            User user = buttonInteraction.getUser();
            Server server = buttonInteraction.getServer().get();

            String[] args = clickID.split(" ");

            if(args[0].equalsIgnoreCase("codenames")) {

                channel.sendMessage(clickID);

                if (args[1].equalsIgnoreCase("join")) {
                    CodeNamesGame codeNamesGame = messageIDCodeNamesMap.get(buttonInteraction.getMessage().get().getIdAsString());
                    if(codeNamesGame.getPlayers().contains(user)) {
                        buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent("You already joined!").respond();
                        return;
                    }
                    if(codeNamesGame.getKickedPlayers().contains(user)) {
                        buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent("You are banned from this game").respond();
                        return;
                    }
                    try {
                        codeNamesGame.joinPlayer(user);
                        buttonInteraction.acknowledge();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (args[1].equalsIgnoreCase("leave")) {
                    CodeNamesGame codeNamesGame = messageIDCodeNamesMap.get(buttonInteraction.getMessage().get().getIdAsString());
                    if(!codeNamesGame.getPlayers().contains(user)) {
                        buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent("You never joined, nothing changed.").respond();
                        return;
                    }
                    try {
                        codeNamesGame.leavePlayer(user);
                        buttonInteraction.acknowledge();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (args[1].equalsIgnoreCase("start")) {
                    CodeNamesGame codeNamesGame = messageIDCodeNamesMap.get(buttonInteraction.getMessage().get().getIdAsString());
                    if(!codeNamesGame.getStartedBy().equals(user)) {
                        buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent("You are not the owner!").respond();
                        return;
                    }
                    try {
                        messageIDCodeNamesMap.remove(codeNamesGame.getMainMessageID());
                        messageIDCodeNamesMap.put(codeNamesGame.sendSelectTeamMessage(), codeNamesGame);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if(args[1].equalsIgnoreCase("kick")) {

                    if(args[2].equalsIgnoreCase("message")) {
                        CodeNamesGame codeNamesGame = messageIDCodeNamesMap.get(buttonInteraction.getMessage().get().getIdAsString());
                        InteractionImmediateResponseBuilder responseBuilder = buttonInteraction.createImmediateResponder().setContent("Choose one option").setFlags(MessageFlag.EPHEMERAL);
                        for(ActionRow current : codeNamesGame.createKickPlayerActionRow()) {
                            responseBuilder.addComponents(current);
                        }
                        try {
                            responseBuilder.respond().get();
                        } catch (Exception e) {
                            buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent("There are no players to be kicked!").respond();
                        }

                    }

                    else {
                        CodeNamesGame codeNamesGame = messageIDCodeNamesMap.get(args[3]);
                        try {
                            codeNamesGame.kickPlayer(args[2]);
                            buttonInteraction.getMessage().get().delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }

                if(args[1].equalsIgnoreCase("team")) {
                    CodeNamesGame codeNamesGame = messageIDCodeNamesMap.get(buttonInteraction.getMessage().get().getIdAsString());
                    if(!codeNamesGame.getPlayers().contains(user)) {
                        buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent("You didn't join the game!").respond();
                        return;
                    }
                    if(args[2].equalsIgnoreCase("start")) {
                        if(codeNamesGame.getStartedBy() != user) {
                            buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent("Hey, you are not the owner!").respond();
                            return;
                        }
                        buttonInteraction.acknowledge();
                    }
                    else if(args[2].equalsIgnoreCase("blue")) {
                        if(args[3].equalsIgnoreCase("operative")) {
                            codeNamesGame.roles.put(user, CodeNamesRole.BLUE_OPERATIVE);
                            try {
                                codeNamesGame.updateSelectTeamMessage();
                                buttonInteraction.acknowledge();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                        codeNamesGame.roles.put(user, CodeNamesRole.BLUE_SPYMASTER);
                        try {
                            codeNamesGame.updateSelectTeamMessage();
                            buttonInteraction.acknowledge();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else if(args[2].equalsIgnoreCase("red")) {
                        if(args[3].equalsIgnoreCase("operative")) {
                            codeNamesGame.roles.put(user, CodeNamesRole.RED_OPERATIVE);
                            try {
                                codeNamesGame.updateSelectTeamMessage();
                                buttonInteraction.acknowledge();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                        codeNamesGame.roles.put(user, CodeNamesRole.RED_SPYMASTER);
                        try {
                            codeNamesGame.updateSelectTeamMessage();
                            buttonInteraction.acknowledge();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                if(args[1].equalsIgnoreCase("presshere")) {
                    CodeNamesGame codeNamesGame = messageIDCodeNamesMap.get(buttonInteraction.getMessage().get().getIdAsString());

                }

            }
        });


        return this;
    }

}
