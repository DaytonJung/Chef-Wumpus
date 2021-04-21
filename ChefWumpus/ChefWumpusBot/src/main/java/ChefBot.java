import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * Class that builds the bot, and contains methods integral to the bots functions.
 * @author Group 2
 */
public class ChefBot implements Serializable {
    /**
     *Declaration of Strings used later
     */
    private static final String BASE_URL = "https://api.spoonacular.com/recipes/";
    private static final String API_KEY = "&apiKey=51a7aa37f6ef405b99101b92bc70db68";

    private static final String FIND_RECIPE_COMMAND = "-ingredients";
    private static final String BLACKLIST_COMMAND = "-blacklist";
    private static final String RANDOM_RECIPE_COMMAND = "-chefrandom";
    private static final String RECIPE_BOOK = "-recipebook";

    /**
     * Class that returns a command from the user.
     * @author Group 2
     */
    private static class Command {

        String type = "";
        List<String> arguments = new ArrayList<>();

        static Command getCommand(String rawString) {

            Command command = new Command();

            command.arguments.addAll(Arrays.asList(rawString.trim().replaceAll(",", " ").split(" ")));

            command.arguments.removeIf(string -> string.replaceAll(" ", "").isEmpty());

            if(!command.arguments.isEmpty()) {

                command.type = command.arguments.remove(0);

            }

            return command;
        }

    }

    /**
     * A listener adapter used for reading messages from the discord chat.
     */
    private transient ListenerAdapter listenerAdapter;
    /**
     * Hash maps used for the recipe book and black lists.
     */
    private HashMap<String, RecipeBook> recipeBookHashMap;
    private HashMap<String, Set<String>> blackListHashMap;

    /**
     * ChefBot constructor method.
     */
    public ChefBot() {

        buildListenerAdapter();

        recipeBookHashMap = new HashMap<>();

        blackListHashMap = new HashMap<>();

    }

    /**
     * Read Object class used in the buildListenerAdapter method
     * @param objectInputStream user input
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void readObject(ObjectInputStream objectInputStream) throws ClassNotFoundException, IOException {

        objectInputStream.defaultReadObject();

        buildListenerAdapter();

    }

    /**
     * Class that contains the various actions the bot should take if activated in the discord server.
     */
    private void buildListenerAdapter() {

        listenerAdapter = new ListenerAdapter() {

            @Override
            public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {

                ChefBot.this.onMessageReactionAdd(event);

            }

            @Override
            public void onMessageReactionRemove(@Nonnull MessageReactionRemoveEvent event) {
                ChefBot.this.onMessageReactionRemove(event);
            }

            @Override
            public void onMessageReceived(@Nonnull MessageReceivedEvent event) {

                ChefBot.this.onMessageReceived(event);

            }

        };

    }

    /**
     * Getter for the listenerAdapter
     * @return listenerAdapter
     */
    public ListenerAdapter getListenerAdapter() {

        return listenerAdapter;
    }

    /**
     * Boolean to check if the message cam from Chef Wumpus
     * @param event message
     * @return is chef wumpus
     */
    private boolean isChefBot(GenericMessageEvent event) {

        User user = event.getChannel().retrieveMessageById(event.getMessageId()).complete().getAuthor();

        return user.isBot() && user.getName().equals("Chef Wumpus");
    }

    /**
     * Checks if the reaction is to a Chef Wumpus message.
     * @param event reaction
     * @return boolean if it is a reaction
     */
    private boolean isChefReaction(GenericMessageReactionEvent event) {

        return event.getReactionEmote().toString().equals("RE:U+1f468U+200dU+1f373");
    }

    /**
     * Adds recipe to the recipeBook
     * @param event reaction added
     */
    public void onMessageReactionAdd(MessageReactionAddEvent event) {

        if(isChefBot(event) && isChefReaction(event)) {

            String userId = event.getUserId();

            if(!recipeBookHashMap.containsKey(userId)) {

                recipeBookHashMap.put(userId, new RecipeBook());

            }

            MessageEmbed messageEmbed = event.getChannel().retrieveMessageById(event.getMessageId())
                    .complete()
                    .getEmbeds().get(0);

            Recipe recipe = buildRecipe(messageEmbed);

            recipeBookHashMap.get(event.getUserId()).add(recipe);

        }

    }

    /**
     * Removes recipe from the recipeBook
     * @param event reaction removed
     */
    public void onMessageReactionRemove(MessageReactionRemoveEvent event){

        if(isChefBot(event) && isChefReaction(event)) {

            String userId = event.getUserId();

            MessageEmbed messageEmbed = event.getChannel().retrieveMessageById(event.getMessageId())
                    .complete()
                    .getEmbeds().get(0);

            RecipeBook recipeBook = recipeBookHashMap.get(userId);

            Recipe recipe = buildRecipe(messageEmbed);

            recipeBook.remove(recipe);

        }

    }

    /**
     * Builds the recipe based on user inputs
     * @param messageEmbed recipe that is sent to the channel
     * @return built recipe
     */
    private Recipe buildRecipe(MessageEmbed messageEmbed) {

        return new Recipe(messageEmbed.getDescription(),
                messageEmbed.getTitle(),
                messageEmbed.getUrl(),
                messageEmbed.getImage().getUrl(),
                messageEmbed.getFooter().getText());

    }


    /**
     * Receives Messages from discord bot and handles them according to content
     * @param event the MessageReceivedEvent to use
     */
    public void onMessageReceived(MessageReceivedEvent event) {

        String rawString = event.getMessage().getContentRaw();

        String userID = event.getAuthor().getId();

        if(!rawString.isEmpty()) {

            Command command = Command.getCommand(rawString);

            switch(command.type) {

                case FIND_RECIPE_COMMAND:

                    if(blackListHashMap.containsKey(userID) && !Collections.disjoint(command.arguments, blackListHashMap.get(userID))) {

                        Set<String> userBlackList = blackListHashMap.get(userID);

                        for(String ingredient : command.arguments) {

                            if(userBlackList.contains(ingredient)) {

                                sendMessage(event, String.format("Sorry %s, some of those ingredients are on your blacklist", event.getAuthor().getName()));

                                return;

                            }

                        }

                    }

                    try{

                        for(Recipe recipe : RecipeBook.getRecipes(command.arguments, getNumRecipes(command.arguments))) {

                            sendMessage(event, recipe.getMessageEmbed());

                        }

                    }catch(IOException e) {

                        event.getChannel().sendMessage("Sorry! Couldn't find any recipes related to your ingredients.").queue();

                        return;

                    }

                    break;

                case RECIPE_BOOK:

                    if(!recipeBookHashMap.containsKey(userID)) {

                        sendMessage(event, String.format("Sorry %s, you do not have any recipes in your recipe book", event.getAuthor().getName()));

                    }else{

                        for(MessageEmbed messageEmbed : recipeBookHashMap.get(userID).getRecipeMessages()) {

                            sendMessage(event, messageEmbed);

                        }

                    }

                    break;

                case BLACKLIST_COMMAND:

                    String message = modifyBlacklist(event.getAuthor(), command.arguments);

                    sendMessage(event, message);

                    break;

                case RANDOM_RECIPE_COMMAND:

                    int num = Integer.min(10, getNumRecipes(command.arguments));

                    JSONObject jsonObject;
                    JSONArray jArr;

                    try {

                        jsonObject = CommandBuilder.getJSONObject(CommandBuilder.getClientResponse(BASE_URL + "random?number=" + num + API_KEY));

                        jArr = new JSONArray(jsonObject.get("recipes").toString());

                    } catch (IOException e) {

                        sendMessage(event, "\"Sorry! Couldn't find any random recipes at the moment.\"");

                        return;

                    }

                    for(int i = 0; i < jArr.length(); i++){

                        sendMessage(event, CommandBuilder.getEmbedMessage((JSONObject) jArr.get(i)));

                    }

                    break;

            }

        }

    }

    /**
     * Sends message to the discord channel
     * @param event to be sent to the channel
     * @param message to be sent back to the discord chat
     */
    private static void sendMessage(GenericMessageEvent event, String message) {

        event.getChannel().sendMessage(message).queue();

    }

    /**
     * Overloaded sendMessage
     * @param event to be sent to the channel
     * @param message to be sent back to the discord chat
     */
    private static void sendMessage(GenericMessageEvent event, MessageEmbed message) {

        event.getChannel().sendMessage(message).queue();

    }

    /**
     * The default number of recipes constant
     */
    private static final int DEFAULT_NUM_RECIPES = 1;

    /**
     * Attempts to find the number of recipes from a list of arguments. The last element is
     * where the method will look
     * @param arguments the arguments list
     * @return the number of recipes to get if successful, otherwise DEFAULT_NUM_RECIPES
     */
    private int getNumRecipes(List<String> arguments) {

        int numRecipes = DEFAULT_NUM_RECIPES;

        String lastArgument = null;

        try{

            lastArgument = arguments.remove(arguments.size() - 1);

            numRecipes = Integer.parseInt(lastArgument);

        }catch(ArrayIndexOutOfBoundsException ignored) {

            //Ignored

        }catch(NumberFormatException e) {

            if(lastArgument != null) {

                arguments.add(lastArgument);

            }

        }

        return numRecipes;
    }

    /**
     * Modifies the blacklist based on which user is doing the command.
     * @param user on the channel
     * @param ingredients list
     * @return the blacklist
     */
    private String modifyBlacklist(User user, List<String> ingredients) {

        String userID = user.getId();
        String name = user.getName();

        if(!blackListHashMap.containsKey(userID)) {

            blackListHashMap.put(userID, new HashSet<>());

        }

        Set<String> userBlackList = blackListHashMap.get(userID);

        if(ingredients.isEmpty()) {

            if(!userBlackList.isEmpty()) {

                return String.format("%s's blacklist: ", name) + userBlackList;

            }else{

                return String.format("%s's blacklist is empty", name);

            }

        }else{

            String firstString = ingredients.get(0);

            if(firstString.equals("clear")) {

                userBlackList.clear();

                return String.format("Cleared %s's blacklist", name);

            }else if(firstString.equals("remove")) {

                ingredients.remove(0);

                if(ingredients.isEmpty()) {

                    return String.format("Sorry %s, please provide ingredients to remove from your blacklist", name);

                }else{

                    userBlackList.removeAll(ingredients);

                    return String.format("Removed ingredients from %s's blacklist", name);

                }

            }else{

                userBlackList.addAll(ingredients);

                return String.format("Added ingredients to %s's blacklist", name);

            }

        }

    }

}