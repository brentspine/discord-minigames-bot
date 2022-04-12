package de.brentspine.games;

import org.javacord.api.entity.message.component.*;
import org.javacord.api.entity.user.User;

public class RockPaperScissors {

    private final User user1;
    private final User user2;
    private String user1Move;
    private String user2Move;

    public RockPaperScissors(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
        this.user1Move = "none";
        this.user2Move = "none";
    }

    public String user1Move(String move) {
        //Verify that userMove is valid
        if(!move.equalsIgnoreCase("rock") && !move.equalsIgnoreCase("paper") && !move.equalsIgnoreCase("scissors")) {
            return "Invalid Move!";
        } else {
            if(user2.isBot()) {
                //Randomly generate the opponents move (0, 1, 2)
                int rand = (int) (Math.random() * 3);
                if(rand == 0) {
                    user2Move = "rock";
                } else if(rand == 1) {
                    user2Move = "paper";
                } else
                    user2Move = "scissors";
            }
            user1Move = move;
            return checkWin();
        }
    }


    public String user2Move(String move) {
        //Verify that userMove is valid
        if(!move.equalsIgnoreCase("rock") && !move.equalsIgnoreCase("paper") && !move.equalsIgnoreCase("scissors")) {
            return "Invalid Move!";
        } else {
            user2Move = move;
            return checkWin();
        }
    }

    public String checkWin() {
        if(user1Move == "none" || user2Move == "none") {
           return "none";
        }
        //Calculate if the user won, lost or tied
        if(user1Move.equalsIgnoreCase(user2Move)) {
            return "tie";
        } else if((user1Move.equalsIgnoreCase("rock") && user2Move.equalsIgnoreCase("scissors")) || (user1Move.equalsIgnoreCase("scissors") && user2Move.equalsIgnoreCase("paper")) || (user1Move.equalsIgnoreCase("paper") && user2Move.equalsIgnoreCase("rock"))) {
            return "user1";
        } else
            return "user2";
    }

    public String getEmoji(String move) {
        if(move.equalsIgnoreCase("paper")) {
            return ":page_facing_up: paper";
        }
        return ":" + move + ": " + move;
    }


    public ActionRow getActionRow(User user, User target) {
        ButtonBuilder buttonBuilder1 = new ButtonBuilder()
                .setStyle(ButtonStyle.PRIMARY)
                .setEmoji("\uD83E\uDEA8")
                .setLabel("Rock")
                .setCustomId("rps " + user.getIdAsString() + " " + target.getIdAsString() + " rock");
        ButtonBuilder buttonBuilder2 = new ButtonBuilder()
                .setStyle(ButtonStyle.PRIMARY)
                .setLabel("Paper")
                .setEmoji("\uD83D\uDCC4")
                .setCustomId("rps " + user.getIdAsString() + " " + target.getIdAsString() + " paper");
        ButtonBuilder buttonBuilder3 = new ButtonBuilder()
                .setStyle(ButtonStyle.PRIMARY)
                .setLabel("Scissors")
                .setEmoji("✂️")
                .setCustomId("rps " + user.getIdAsString() + " " + target.getIdAsString() + " scissors");

        if(!checkWin().equals("none")) {
            buttonBuilder1.setDisabled(true);
            buttonBuilder2.setDisabled(true);
            buttonBuilder3.setDisabled(true);
        }

        Button buttonRock = buttonBuilder1.build();
        Button buttonPaper = buttonBuilder2.build();
        Button buttonScissors = buttonBuilder3.build();

        ActionRowBuilder actionRowBuilder = new ActionRowBuilder()
                .addComponents(buttonRock)
                .addComponents(buttonPaper)
                .addComponents(buttonScissors);
        ActionRow actionRow = actionRowBuilder.build();
        return actionRow;
    }

    public String createAnswer(User user, User target, RockPaperScissors rps) {
        String arg1 = "**" + user.getName() + " vs. " + target.getName() + "**";
        String arg2 = "\n" + user.getName() + " is ready";
        String arg3 = "\n" + target.getName() + " is ready";
        String arg4 = "";
        if(!rps.getUser1Move().equals("none") && !rps.getUser2Move().equals("none")) {
            arg2 = "\n" + user.getName() + " chose " + rps.getEmoji(rps.getUser1Move());
            arg3 = "\n" + target.getName() + " chose " + rps.getEmoji(rps.getUser2Move());
            String checkWin = rps.checkWin();
            if(checkWin.equals("tie")) {
                arg4 = "\nYou both chose the same, its a tie";
            } else if(checkWin.equals("user1")) {
                arg4 = "\n" + user.getMentionTag() + " won the Game";
            } else
                arg4 = "\n" + target.getMentionTag() + " won the Game";
        } else {
            if(rps.getUser1Move().equals("none")) {
                arg2 = "\n" + user.getName() + " is choosing...";
            }
            if(rps.getUser2Move().equals("none")) {
                arg3 = "\n" + target.getName() + " is choosing...";
            }
        }
        return arg1 + arg2 + arg3 + arg4;
    }

    /*//Randomly generate the opponents move (0, 1, 2)
            int rand = (int) (Math.random() * 3);
            String opponentMove = "";
            if(rand == 0) {
                opponentMove = "rock";
            } else if(rand == 1) {
                opponentMove = "paper";
            } else
                opponentMove = "scissors";
            System.out.println("Opponent move: " + opponentMove);*/

    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }

    public String getUser1Move() {
        return user1Move;
    }

    public String getUser2Move() {
        return user2Move;
    }
}
