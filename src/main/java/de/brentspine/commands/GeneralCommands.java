package de.brentspine.commands;

import de.brentspine.Main;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GeneralCommands {

    DiscordApi api = Main.getApi();

    public GeneralCommands run() {
        api.addMessageCreateListener(event -> {

            String[] args = event.getMessageContent().split(" ");

            if (args[0].startsWith("m!")) {

                Server server = event.getServer().get();
                TextChannel channel = event.getChannel();
                User user = event.getMessageAuthor().asUser().get();

                if(args[0].equalsIgnoreCase("m!cum")) {
                    try {
                        Message message = channel.sendMessage("<@572064680487550987> https://tenor.com/bDOWF.gif").get();
                        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
                        executorService.scheduleWithFixedDelay(() -> {
                            message.delete();
                            executorService.shutdown();
                        }, 2, 99, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        user.sendMessage("Not enough permissions to send this message");
                    }
                    return;
                }

                if (args[0].equalsIgnoreCase("m!ping")) {
                    try {
                        EmbedBuilder embedBuilder = new EmbedBuilder().setAuthor(api.getYourself().getDiscriminatedName(), "", api.getYourself().getAvatar())
                                .setDescription(":heartpulse: Latency: **" + String.valueOf(api.getLatestGatewayLatency().getNano()).substring(0, 3)  + "** ms\n" +
                                                ":stopwatch: Rest Latency: **" + api.measureRestLatency().get().toMillis() + "** ms")
                                .setFooter("Bot by " + api.getOwner().get().getName(), api.getOwner().get().getAvatar());
                        event.getChannel().sendMessage(embedBuilder);
                    }  catch (InterruptedException e) {
                    e.printStackTrace();
                    } catch (ExecutionException e) {
                    e.printStackTrace();
                    }
                } else
                if (args[0].equalsIgnoreCase("m!invite")) {
                    try {
                        EmbedBuilder embed = new EmbedBuilder()
                                .setTitle(":mailbox: Invite :mailbox:")
                                .setUrl("https://discord.com/oauth2/authorize?client_id=889591484595187722&scope=bot&permissions=137976094913")
                                .setThumbnail(api.getYourself().getAvatar())
                                .setDescription("Click on the link above to add me to your server!")
                                .setFooter("Bot by " + api.getOwner().get().getName(), api.getOwner().get().getAvatar())
                                .setColor(Color.BLACK);
                        channel.sendMessage(embed);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                else if(args[0].equalsIgnoreCase("m!devtest")) {
                    Integer huhChamp = -1;
                }
                else if(args[0].equalsIgnoreCase("m!help")) {
                    channel.sendMessage("m!ping\nm!invite\nm!help\nm!uptime\nm!dice\nm!random\nm!howgay\nm!uno\nm!rps\nm!ttt");
                }
                else if(args[0].equalsIgnoreCase("m!uptime")) {
                    try {
                        String timeString = "";
                        Integer minutes = Main.getUptime();
                        Integer hours = 0;
                        Integer days = 0;
                        while (minutes >= 60) {
                            minutes = minutes - 60;
                            hours++;
                        }
                        while (hours >= 24) {
                            hours = hours - 24;
                            days++;
                        }
                        EmbedBuilder embedBuilder = new EmbedBuilder().setAuthor(api.getYourself().getDiscriminatedName(), "", api.getYourself().getAvatar())
                                .setDescription(":stopwatch: **" + days + "d " + hours + "h " + minutes + "m**")
                                .setFooter("Bot by " + api.getOwner().get().getName(), api.getOwner().get().getAvatar());
                        event.getChannel().sendMessage(embedBuilder);
                    }  catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    return this;
    }
}
