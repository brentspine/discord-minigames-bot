package de.brentspine.commands.gamecommands;

import de.brentspine.games.RockPaperScissors;
import de.brentspine.Main;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ButtonInteraction;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class RPSCommand {

    DiscordApi api = Main.getApi();

    HashMap<String, RockPaperScissors> messageIDRPSMap = new HashMap<>();

    public RPSCommand run() {
        api.addMessageCreateListener(event -> {

            String[] args = event.getMessageContent().split(" ");

            if (args[0].startsWith("m!")) {

                Server server = event.getServer().get();
                TextChannel channel = event.getChannel();
                User user = event.getMessage().getUserAuthor().get();

                if(args[0].equalsIgnoreCase("m!rps")) {
                    if(args.length >= 2) {
                        try {
                            User target;
                            if(event.getMessage().getMentionedUsers().isEmpty()) {
                                try {
                                    target = api.getUserById(args[1]).get();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    return;
                                } catch (ExecutionException e) {
                                    channel.sendMessage("Mention a user you want to play against!");
                                    return;
                                } catch (Exception e) {
                                    channel.sendMessage("Mention a user you want to play against!");
                                    return;
                                }
                            } else {
                                target = event.getMessage().getMentionedUsers().get(0);
                            }
                            if(target.getMentionTag().equalsIgnoreCase(user.getMentionTag())) {
                                channel.sendMessage("You cant play against yourself!\nTry `m!rps @Any-Bot` to play anyways");
                                return;
                            }
                            RockPaperScissors rps = new RockPaperScissors(user, target);
                            Message message = channel.sendMessage("**" + user.getName() + " vs. " + target.getName() + "**\n" + user.getName() + " is choosing...\n" + target.getName() + " is choosing...", rps.getActionRow(user, target)).get();
                            messageIDRPSMap.put(message.getIdAsString(), rps);
                            if(target.isBot()) {
                                message.edit("**" + user.getName() + " vs. " + target.getName() + "**\n" + user.getName() + " is choosing...\n" + target.getName() + " is ready...");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else
                        channel.sendMessage("Mention a user/bot you want to play against!");
                }
            }
        });

        api.addButtonClickListener(event -> {

            String clickID = event.getButtonInteraction().getCustomId();
            ButtonInteraction buttonInteraction = event.getButtonInteraction();
            User u = buttonInteraction.getUser();

            //buttonInteraction.getChannel().get().sendMessage("CustomID " + buttonInteraction.getCustomId() + "\nUser1: " + messageIDRPSMap.get(buttonInteraction.getMessage().get().getIdAsString()).getUser1().getName() + " Move: " + messageIDRPSMap.get(buttonInteraction.getMessage().get().getIdAsString()).getUser1Move() + "\nUser2: " + messageIDRPSMap.get(buttonInteraction.getMessage().get().getIdAsString()).getUser2().getName() + " Move: " + messageIDRPSMap.get(buttonInteraction.getMessage().get().getIdAsString()).getUser2Move());

            String[] args = clickID.split(" ");

            if(args[0].equalsIgnoreCase("rps")) {
                RockPaperScissors rps = messageIDRPSMap.get(buttonInteraction.getMessage().get().getIdAsString());
                User user = rps.getUser1();
                User target = rps.getUser2();
                if(args[1].equalsIgnoreCase(buttonInteraction.getUser().getIdAsString())) {
                    if(!rps.getUser1Move().equalsIgnoreCase("none")) {
                        buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent("Hey, you already choosed " + rps.getUser1Move() + "!").respond();
                        return;
                    }
                    rps.user1Move(args[3]);
                }
                else if(args[2].equalsIgnoreCase(buttonInteraction.getUser().getIdAsString())) {
                    if(!rps.getUser2Move().equalsIgnoreCase("none")) {
                        buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent("Hey, you already choosed " + rps.getUser2Move() + "!").respond();
                        return;
                    }
                    rps.user2Move(args[3]);
                } else {
                    buttonInteraction.createImmediateResponder().setFlags(MessageFlag.EPHEMERAL).setContent("Hey, this is not yours!").respond();
                    return;
                }
                buttonInteraction.createOriginalMessageUpdater().setContent(rps.createAnswer(user, target, rps)).addComponents(rps.getActionRow(user, target)).update();
            }

        });

        return this;
    }

}
