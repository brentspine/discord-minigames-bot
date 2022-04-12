package de.brentspine.commands;

import de.brentspine.Main;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeveloperCommands {

    DiscordApi api = Main.getApi();

    public DeveloperCommands run() {

        api.addMessageCreateListener(event -> {

            String[] args = event.getMessageContent().split(" ");

            if(args[0].startsWith("m!")) {

                Server server = event.getServer().get();
                TextChannel channel = event.getChannel();
                User user = event.getMessage().getAuthor().asUser().get();

                if(args[0].equalsIgnoreCase("m!shutdown")) {
                    if(!user.isBotOwner()) {
                        channel.sendMessage("Sussy baka, no you won't");
                        return;
                    }
                    channel.sendMessage("Shutting down");
                    api.updateActivity(ActivityType.PLAYING, "Shutting down...");
                    api.updateStatus(UserStatus.IDLE);
                    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
                    executorService.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            api.disconnect();
                        }
                    }, 3, 999999999, TimeUnit.SECONDS);
                    return;
                }

                if(args[0].equalsIgnoreCase("m!emojis")) {
                    String list = "";
                    list += "**This server has " + server.getCustomEmojis().size() + " emojis**\n";
                    for(KnownCustomEmoji emoji : server.getCustomEmojis()) {
                        if((list.length() + (list + emoji.getMentionTag() + "`" + emoji.getMentionTag() + "`\n").length()) > 2000) {
                            if(!user.isBotOwner() && !server.hasPermission(user, PermissionType.ADMINISTRATOR) && !server.hasPermission(user, PermissionType.MANAGE_EMOJIS) && !server.hasPermission(user, PermissionType.MANAGE_WEBHOOKS) && !server.hasPermission(user, PermissionType.MANAGE_ROLES) && !server.hasPermission(user, PermissionType.MANAGE_SERVER)) {
                                channel.sendMessage("You don't have enough permissions to use this command, since it spams the chat");
                                return;
                            }
                            channel.sendMessage(list);
                            list = "";
                        }
                        list = list + emoji.getMentionTag() + "`" + emoji.getMentionTag() + "`\n";
                    }
                    if(list.length() >= 1) {
                        channel.sendMessage(list);
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setTitle("");
                    } else
                        channel.sendMessage("This server has no emojis!");
                }


            }

        });
        return this;
    }

}
