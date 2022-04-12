package de.brentspine.util;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Dice {

    private Integer min;
    private Integer max;
    private Integer result;

    boolean running = false;
    HashMap<Integer, String> diceEmojis = new HashMap<>();
    HashMap<String[], Integer> rollAnimation = new HashMap<>();
    HashMap<String[], Message> rollAnimationMessages = new HashMap<>();
    HashMap<String[], ArrayList<Integer>> rollAnimationNumbers = new HashMap<>();
    HashMap<String[], Message> rollAnimationTemporaryMessages = new HashMap<>();
    HashMap<String[], String> rollAnimationUserIDs = new HashMap<>();

    public Dice(Integer min, Integer max) {
        this.min = min;
        this.max = max;
    }

    public Dice() { }


    public Integer roll() {
        if(min == null || max == null) {
            throw new MissingResourceException("Missing max or min", "roll()", "");
        }
        if(min > max) {
            throw new IllegalArgumentException("Min can't be higher than the max value");
        }
        result = (int) (Math.random()*(max-min+1)+min);
        return result;
    }

    private Dice run() {
        if(!running) {
            diceEmojis.put(1, "<:Dice_1:889953802776616980>");
            diceEmojis.put(2, "<:Dice_2:889953821210595418>");
            diceEmojis.put(3, "<:Dice_3:889953841909481552>");
            diceEmojis.put(4, "<:Dice_4:889953842228252712>");
            diceEmojis.put(5, "<:Dice_5:889953842865782824>");
            diceEmojis.put(6, "<:Dice_6:889954220617367592>");
            diceEmojis.put(-1, "<a:animatedDice:890973390670811166>");

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        for(String[] messageArgs : rollAnimation.keySet()) {
                            if(messageArgs.length > rollAnimation.get(messageArgs)) {
                                String content = "";
                                Message message = rollAnimationMessages.get(messageArgs);
                                messageArgs[rollAnimation.get(messageArgs)] = diceEmojis.get(roll());
                                if(rollAnimation.get(messageArgs) == 0) {
                                    rollAnimationNumbers.put(messageArgs, new ArrayList<>());
                                } else if(rollAnimation.get(messageArgs) == 1) {
                                    rollAnimationTemporaryMessages.get(messageArgs).delete();
                                    rollAnimationTemporaryMessages.remove(messageArgs);
                                }
                                ArrayList<Integer> test = rollAnimationNumbers.get(messageArgs);
                                test.add(result);
                                rollAnimationNumbers.put(messageArgs, test);
                                for(String current : messageArgs) {
                                    content = content + current + " ";
                                }
                                message = message.edit(content).get();
                                rollAnimationMessages.put(messageArgs, message);
                                rollAnimation.put(messageArgs, rollAnimation.get(messageArgs) + 1);
                            } else {
                                if(rollAnimationTemporaryMessages.containsKey(messageArgs)) {
                                    rollAnimationTemporaryMessages.get(messageArgs).delete();
                                    rollAnimationTemporaryMessages.remove(messageArgs);
                                }
                                Integer messageResult = 0;
                                for(Integer current : rollAnimationNumbers.get(messageArgs)) {
                                    messageResult = messageResult + current;
                                }
                                String content = "";
                                for(String current : messageArgs) {
                                    content = content + current + " ";
                                }
                                content = content + "\nResult: " + String.valueOf(messageResult);
                                rollAnimationMessages.get(messageArgs).edit(content);
                                rollAnimation.remove(messageArgs);
                                rollAnimationMessages.remove(messageArgs);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                        //Main.getApi().getChannelById("815960474604011536").get().as(TextChannel.class).get().sendMessage(e.getMessage());
                    }
                }
            } , 0, 2, TimeUnit.SECONDS);

            running = true;
        }
        return this;
    }

    public Dice sendDices(TextChannel channel, Integer numberOfDices, Dice dice, String userID) throws ExecutionException, InterruptedException {
        try {
            if(rollAnimationUserIDs.containsValue(userID)) {
                channel.sendMessage("You can only run 1 dice command at once!");
                return this;
            }
            run();
            String[] messageArgs = getDiceAnimation(numberOfDices).split(" ");
            Message temporaryMessage = channel.sendMessage("Generating a random number between " + String.valueOf(messageArgs.length) + " and " + String.valueOf(messageArgs.length * 6)).get();
            Message message = channel.sendMessage(getDiceAnimation(numberOfDices) + " ").get();
            rollAnimation.put(messageArgs, 0);
            rollAnimationMessages.put(messageArgs, message);
            rollAnimationTemporaryMessages.put(messageArgs, temporaryMessage);
            rollAnimationUserIDs.put(messageArgs, userID);
            return this;
        } catch (NullPointerException e) {
            e.printStackTrace();
            channel.sendMessage("Null");
        } catch (Exception e) {
            e.printStackTrace();
            channel.sendMessage("Exception");
        }
        return this;
    }

    public String getDiceAnimation(Integer numberOfDices) {
        run();
        String content = "";
        for(int i = 1; i <= numberOfDices; i++) {
            content = content + diceEmojis.get(-1) + " ";
        }
        return content;

        /*catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        channel.sendMessage("Usage: `m!dice <DiceAmount>`");
                    }*/
    }

    public static int generateRandomNumberBetween(int min, int max) {
        return (int) (Math.random()*(max-min+1)+min);
    }

    public String getDiceEmoji(int i) {
        run();
        return diceEmojis.get(i);
    }

    public HashMap<Integer, String> getDiceEmojis() {
        run();
        return diceEmojis;
    }

    public Integer getResult() {
        if(result == null) {
            throw new NullPointerException("Dice didn't get rolled yet");
        }
        return result;
    }

    public Dice setMin(Integer min) {
        this.min = min;
        return this;
    }

    public Dice setMax(Integer max) {
        this.max = max;
        return this;
    }
}
