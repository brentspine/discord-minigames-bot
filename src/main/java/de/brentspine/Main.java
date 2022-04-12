package de.brentspine;

import de.brentspine.commands.DeveloperCommands;
import de.brentspine.commands.DiceCommands;
import de.brentspine.commands.GeneralCommands;
import de.brentspine.commands.ModCommands;
import de.brentspine.commands.gamecommands.*;
import de.brentspine.util.Settings;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.UserStatus;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static DiscordApi api;
    private static DeveloperCommands developerCommands;
    private static GeneralCommands generalCommands;
    private static DiceCommands diceCommands;
    private static ModCommands modCommands;
    private static RPSCommand rpsCommand;
    private static UNOCommand unoCommand;
    private static CodeNamesCommand codeNamesCommand;
    private static TicTacToeCommand ticTacToeCommand;
    private static BattleShipsCommand battleShipsCommand;
    public static Integer uptime;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        String token = Settings.MINI_GAMES_TOKEN;

        uptime = 0;
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                uptime++;
            }
        }, 60, 60, TimeUnit.SECONDS);

        api = new DiscordApiBuilder().setToken(token).login().join();
        api.updateActivity(ActivityType.PLAYING, "Loading...");
        developerCommands = new DeveloperCommands().run();
        generalCommands = new GeneralCommands().run();
        diceCommands = new DiceCommands().run();
        modCommands = new ModCommands().run();
        rpsCommand = new RPSCommand().run();
        unoCommand = new UNOCommand().run();
        codeNamesCommand = new CodeNamesCommand().run();
        ticTacToeCommand = new TicTacToeCommand().run();
        battleShipsCommand = new BattleShipsCommand().run();

        api.addMessageCreateListener(event -> {
            if(event.getChannel().getIdAsString() != "929768937594449950") return;
            Message message = event.getMessage();
            message.addReaction("👍");
            message.addReaction("👎");
        });

        /*for(Object object : api.getChannelById("929768937594449950").get().asTextChannel().get().getMessagesAsStream().toArray()) {
            Message message = (Message) object;
            boolean contains = false;
            for(Reaction reaction : message.getReactions()) {
                if(reaction.getEmoji().equalsEmoji("👍") && reaction.containsYou()) {
                    contains = true;
                    break;
                }
            }
            if(!contains)
                message.addReaction("👍");
            contains = false;
            for(Reaction reaction : message.getReactions()) {
                if(reaction.getEmoji().equalsEmoji("👎") && reaction.containsYou()) {
                    contains = true;
                    break;
                }
            }
            if(!contains)
                message.addReaction("👎");
        }*/

        /*System.out.println("Der Bot ist auf " + api.getServers().size() + " Servern:");
        for(Server server : api.getServers()) {
            ServerChannel channel = null;
            for (ServerChannel c : server.getChannels()) {
                if(c.canYouSee() && c.getType() != ChannelType.CHANNEL_CATEGORY)
                    channel = c;
            }
            if(channel != null)
                System.out.println(server.getName() + ": " + new InviteBuilder(channel).setMaxAgeInSeconds(100).create().get().getUrl().toString());
            else
                System.out.println(server.getName() + ": " + "Invitecreation not possible");
        }*/

        //api.getChannelById("889942673966891092").get().asTextChannel().get().sendMessage("Bot is Ready!");

        System.out.println(api.createBotInvite());

        api.updateActivity(ActivityType.PLAYING, "MiniGames || m!help");
        api.updateStatus(UserStatus.DO_NOT_DISTURB);
    }


    public static DiscordApi getApi() {
        return api;
    }

    public static Integer getUptime() {
        return uptime;
    }

    public static BattleShipsCommand getBattleShipsCommand() {
        return battleShipsCommand;
    }

}
