package de.brentspine.commands;

import de.brentspine.Main;
import de.brentspine.util.ColorUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordClient;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.List;

public class ModCommands {

    DiscordApi api = Main.getApi();

    public ModCommands run() {

        api.addMessageCreateListener(event -> {

            String[] args = event.getMessageContent().split(" ");

            if (args[0].startsWith("m!")) {

                Server server = event.getServer().get();
                TextChannel channel = event.getChannel();
                User user = event.getMessage().getAuthor().asUser().get();
                Message message = event.getMessage();

                if (args[0].equalsIgnoreCase("m!userinfo")) {
                    if(args.length <= 1) {
                        channel.sendMessage("Usage: m!userinfo <userID>");
                        return;
                    }

                    User target;
                    try {
                        if(message.getMentionedUsers().size() > 0)
                            target = message.getMentionedUsers().get(0);
                        else
                            target = api.getUserById(args[1]).get();
                        target.getMentionTag();
                    } catch (Exception e) {
                        //e.printStackTrace();
                        channel.sendMessage("User not found (Maybe they are not cached by the bot)");
                        return;
                    }

                    EmbedBuilder embedBuilder = new EmbedBuilder()
                            .setTitle("User Info " + target.getDiscriminatedName())
                            .setThumbnail(target.getAvatar());

                    String status = target.getWebStatus().getStatusString();
                    if(target.getCurrentClients().size() > 0) {
                        status = target.getStatusOnClient((DiscordClient) target.getCurrentClients().toArray()[0]).getStatusString();
                    }

                    embedBuilder.addField("User Info", "" +
                            "`DiscriminatedName:` " + target.getDiscriminatedName() + "\n" +
                            "`ID:` " + target.getIdAsString() + "\n" +
                            "`Status:` " + status + "\n" +
                            "`MentionTag:` " + target.getMentionTag() + "\n" +
                            "`Created at:` <t:" + target.getCreationTimestamp().getEpochSecond()  + ":R> / <t:" + target.getCreationTimestamp().getEpochSecond() + ":D>\n" +
                            "`Bot:` " + target.isBot());

                    String roleColor = "*none*";
                    if(target.getRoleColor(server).isPresent()) {
                        roleColor = new ColorUtils().getColorNameFromColor(target.getRoleColor(server).get());
                    }

                    String connectedVoiceChannel = "*none*";
                    if(target.getConnectedVoiceChannel(server).isPresent()) {
                        connectedVoiceChannel = "<#" + target.getConnectedVoiceChannel(server).get().getIdAsString() + ">";
                    }

                    embedBuilder.addField("Server Info", "" +
                                    "`DisplayName:` " + target.getDisplayName(server) + "\n" +
                                    "`Joined At:` <t:" + target.getJoinedAtTimestamp(server).get().getEpochSecond() + ":R> / <t:" + target.getJoinedAtTimestamp(server).get().getEpochSecond() + ":D>\n" +
                                    "`Role Color:` " + roleColor + "\n" +
                                    "`Connected Voice Channel:` " + connectedVoiceChannel + "\n");

                    embedBuilder.addField("Permissions", "" +
                            "`Deafened:` " + target.isDeafened(server) + "\n" +
                            "`Muted:` " + target.isMuted(server) + "\n" +
                            "`Pending:` " + target.isPending(server) + "\n" +
                            "`Self Muted:` " + target.isSelfMuted(server) + "\n" +
                            "`Self Deafened:` " + target.isSelfDeafened(server) + "");

                    String roles = "";
                    List<Role> roleList = target.getRoles(server);
                    revlist(roleList);
                    int i = 0;
                    for(Role c : roleList) {
                        i++;
                        i += c.getName().length();
                        roles += c.getMentionTag() + " ";
                        if(i >= 42)
                            roles += "\n";
                    }

                    embedBuilder.addField("Roles", "" +
                            roles);
                    channel.sendMessage(embedBuilder);
                }
            }
        });
        return this;
    }

    public static <T> void revlist(List<T> list)
    {
        // base condition when the list size is 0
        if (list.size() <= 1 || list == null)
            return;


        T value = list.remove(0);

        // call the recursive function to reverse
        // the list after removing the first element
        revlist(list);

        // now after the rest of the list has been
        // reversed by the upper recursive call,
        // add the first value at the end
        list.add(value);
    }

}
