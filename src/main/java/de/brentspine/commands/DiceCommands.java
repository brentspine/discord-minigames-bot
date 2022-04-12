package de.brentspine.commands;

import de.brentspine.util.Dice;
import de.brentspine.Main;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.concurrent.ExecutionException;

public class DiceCommands {

    DiscordApi api = Main.getApi();

    public DiceCommands run() {

        api.addMessageCreateListener(event -> {

            String[] args = event.getMessageContent().split(" ");

            if (args[0].startsWith("m!")) {

                Server server = event.getServer().get();
                TextChannel channel = event.getChannel();
                User user = event.getMessage().getUserAuthor().get();

                if(args[0].equalsIgnoreCase("m!dice")) {
                    try {
                        if(args.length >= 2) {
                            Dice dice = new Dice().setMin(1).setMax(6);
                            Integer numberOfDices = Integer.parseInt(args[1]);
                            if(numberOfDices >= 51) {
                                channel.sendMessage("You can only generate up to 50 dices!");
                                return;
                            }
                            if(numberOfDices <= 0) {
                                channel.sendMessage("The number of dices must be over 0!");
                                return;
                            }
                            dice.sendDices(channel, numberOfDices, dice, user.getIdAsString());
                        } else
                            channel.sendMessage("Usage: `m!dice <DiceAmount>`");
                    } catch (NumberFormatException e) {
                        channel.sendMessage("Usage: `m!dice <DiceAmount>`");
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(args[0].equalsIgnoreCase("m!random")) {
                    if(args.length >= 3) {
                        Integer min = 0;
                        Integer max = 0;
                        try {
                            min = Integer.parseInt(args[1]);
                            max = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            EmbedBuilder embed = new EmbedBuilder()
                                    .setTitle("ERROR")
                                    .setDescription("")
                                    .addField( "Possible errors", "`min` is lower than " + Integer.MIN_VALUE + "\n`max` is higher than " + Integer.MAX_VALUE + "\n`min` or `max` are not numbers")
                                    .setColor(Color.RED);
                            event.getChannel().sendMessage(embed);
                            return;
                        }
                        if (min > max) {
                            channel.sendMessage("Min cant be higher than max!");
                            return;
                        }
                        Dice dice = new Dice(min, max);
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setTitle("Random generator")
                                .setDescription("Number rolled: " + String.valueOf(dice.roll()) + " <:hmmm:891693980780220527>");
                        //.setFooter("Range: " + String.valueOf(min) + "-" + String.valueOf(max));
                        channel.sendMessage(embedBuilder);
                    } else
                        channel.sendMessage("Usage: `m!random min max`");

                }
                if(args[0].equalsIgnoreCase("m!howgay")) {
                    Dice dice = new Dice(0, 100);
                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setTitle("Gay r8ter")
                            .setDescription("You are " + String.valueOf(dice.roll()) + "% gay :rainbow_flag:");
                    //.setFooter("Range: " + String.valueOf(min) + "-" + String.valueOf(max));
                    channel.sendMessage(embedBuilder);
                }
            }
        });

        return this;
    }

}
