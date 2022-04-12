package de.brentspine.commands.gamecommands;

import de.brentspine.games.UNOGame;
import de.brentspine.games.util.UNOCard;
import de.brentspine.games.util.UNOColor;
import de.brentspine.Main;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.interaction.callback.ComponentInteractionOriginalMessageUpdater;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class UNOCommand {

    DiscordApi api = Main.getApi();
    HashMap<String, UNOGame> messageIDUnoGameMap = new HashMap<>();

    public UNOCommand run() {
        api.addMessageCreateListener(event ->{

            String[] args = event.getMessageContent().split(" ");

            if (args[0].startsWith("m!")) {

                Server server = event.getServer().get();
                TextChannel channel = event.getChannel();
                User user = event.getMessage().getUserAuthor().get();

                if(args[0].equalsIgnoreCase("m!uno")) {
                    try {
                        if(args.length >= 2) {
                            if(args[1].equalsIgnoreCase("debug")) {
                                if(args[2].equalsIgnoreCase("allcards")) {
                                    String content = "";
                                    for(UNOCard current : UNOCard.getAllCards()) {
                                        content = content + current + ": " + current.toEmoji() + "\n";
                                    }
                                    channel.sendMessage(content);
                                }
                                else if(args[2].equalsIgnoreCase("roll")) {
                                    String content = "";
                                    for(UNOCard current : new UNOGame(user, user, server, channel).startGame().getUser1Hand()) {
                                        content = content + current.toEmoji() + " ";
                                    }
                                    channel.sendMessage(content);
                                }
                                return;
                            }
                            User target;
                            if(event.getMessage().getMentionedUsers().isEmpty()) {
                                try {
                                    target = api.getUserById(args[1]).get();
                                } catch (Exception e) {
                                    channel.sendMessage("Mention a user you want to play with!");
                                    return;
                                }
                            } else {
                                target = event.getMessage().getMentionedUsers().get(0);
                            }
                            if(target.getMentionTag().equalsIgnoreCase(user.getMentionTag())) {
                                channel.sendMessage("You cant play against yourself!");
                                return;
                            }

                            UNOGame unoGame = new UNOGame(user, target, server, channel).startGame();

                            EmbedBuilder embedBuilder = new EmbedBuilder()
                                    .setTitle("UNO: " + user.getDisplayName(server) + " vs. " + target.getDisplayName(server))
                                    .setDescription("Current card: " + unoGame.getOnTable() + "\n" +
                                            "Last Action: " + unoGame.getLastAction())
                                    .addField("Cards left:", user.getDisplayName(server) + ": " + unoGame.getUser1Hand().size() + "\n" + target.getDisplayName(server) + ": " + unoGame.getUser2Hand().size())
                                    .setThumbnail(unoGame.getOnTable().toEmoteURL());

                            Message message = channel.sendMessage(unoGame.getWhoseTurn().getNicknameMentionTag() + " its your turn", embedBuilder, unoGame.createActionRow()).get();
                            unoGame.setMessageID(message.getIdAsString());
                            messageIDUnoGameMap.put(message.getIdAsString(), unoGame);


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

            if(args[0].equalsIgnoreCase("uno")) {

                if(args[1].equalsIgnoreCase("move")) {
                    UNOGame unoGame = messageIDUnoGameMap.get(args[3]);
                    if(!unoGame.isUsersTurn(user)) {
                        buttonInteraction.createImmediateResponder().setContent("It's " + unoGame.getOtherUser(user).getDisplayName(server) + "'s turn!").setFlags(MessageFlag.EPHEMERAL).respond();
                        return;
                    }
                    UNOCard unoCard = UNOCard.valueOf(args[2]);
                    if(unoCard.getLast2Letters().equalsIgnoreCase("sh") || unoCard.getLast2Letters().equalsIgnoreCase("h4")) {
                        try {
                            unoGame.makeMove(unoCard, user);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        buttonInteraction.createOriginalMessageUpdater().setContent("Select a color!").setFlags(MessageFlag.EPHEMERAL).addComponents(unoGame.createColorPickActionRow()).update();
                        return;
                    }
                    if(!unoGame.isMovePossible(unoCard)) {
                        buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent("This move is not possible!").respond();
                        return;
                    }

                    try {
                        unoGame.makeMove(unoCard, user);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    try {
                        unoGame.updateEmbed();
                        ComponentInteractionOriginalMessageUpdater originalMessageUpdater = buttonInteraction.createOriginalMessageUpdater().setContent("Choose one option");
                        for(ActionRow current : unoGame.createMoveActionRow(user)) {
                            originalMessageUpdater.addComponents(current);
                        }
                        originalMessageUpdater.update();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return;
                }

                else if(args[1].equalsIgnoreCase("color")) {
                    UNOGame unoGame = messageIDUnoGameMap.get(args[3]);
                    try {
                        unoGame.setUserWish(UNOColor.color(args[2]), user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ComponentInteractionOriginalMessageUpdater originalMessageUpdater = buttonInteraction.createOriginalMessageUpdater().setContent("Choose one option");
                    for(ActionRow current : unoGame.createMoveActionRow(user)) {
                        originalMessageUpdater.addComponents(current);
                    }
                    originalMessageUpdater.update();

                    try {
                        unoGame.updateEmbed();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return;
                }

                UNOGame unoGame = messageIDUnoGameMap.get(buttonInteraction.getMessage().get().getIdAsString());

                try{
                    unoGame.getUser1();
                } catch (Exception e) {
                    buttonInteraction.createImmediateResponder().setContent("This game is unavailable").setFlags(MessageFlag.EPHEMERAL).respond();
                    return;
                }

                if(!unoGame.userIsOwner(user)) {
                    buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent("Hey, this is not yours!").respond();
                    return;
                }

                if(args[1].equalsIgnoreCase("makemove")) {
                    if(!unoGame.userIsOwner(user)) {
                        buttonInteraction.createImmediateResponder().setContent("Hey, this is not your game!");
                    }

                    InteractionImmediateResponseBuilder immediateResponseBuilder = buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent("Choose one option!");
                    for(ActionRow current : unoGame.createMoveActionRow(user)) {
                        immediateResponseBuilder.addComponents(current);
                    }

                    immediateResponseBuilder.respond();
                    unoGame.getMoveMessagesMap().put(user.getIdAsString(), buttonInteraction);

                    try {
                        unoGame.updateEmbed();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                else if(args[1].equalsIgnoreCase("myhand")) {
                    String content = "**Your Hand**\n";
                    for(UNOCard current : unoGame.getUserHand(user)) {
                        content = content + current.toEmoji() + " ";
                    }
                    buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent(content).respond();
                    unoGame.getMoveMessagesMap().put(user.getIdAsString(), buttonInteraction);
                }

                else if(args[1].equalsIgnoreCase("draw")) {
                    if(!unoGame.isUsersTurn(user)) {
                        buttonInteraction.createImmediateResponder().setContent("Hey, its not your turn!").setFlags(MessageFlag.EPHEMERAL).respond();
                        return;
                    }
                    unoGame.getUserHand(user).add(UNOCard.generateRandom());
                    unoGame.setLastAction(user.getDisplayName(server) + " drew 1 card\n**[You should delete the old message now!](https://gfycat.com/scalyunevenjuliabutterfly)**");
                    try {
                        unoGame.updateEmbed();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    unoGame.setWhoseTurn(unoGame.getOtherUser(unoGame.getWhoseTurn()));
                    try {
                        unoGame.updateEmbed();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    InteractionImmediateResponseBuilder immediateResponseBuilder = buttonInteraction.createImmediateResponder().setContent("Choose one option").setFlags(MessageFlag.EPHEMERAL);
                    for(ActionRow current : unoGame.createMoveActionRow(user)) {
                        immediateResponseBuilder.addComponents(current);
                    }
                    immediateResponseBuilder.respond();

                }

            }


        });

        return this;
    }

}
