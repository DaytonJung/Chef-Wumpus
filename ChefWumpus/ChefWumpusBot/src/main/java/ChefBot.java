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

public class ChefBot implements Serializable {

    private static final String BASE_URL = "https://api.spoonacular.com/recipes/";
    private static final String API_KEY = "&apiKey=51a7aa37f6ef405b99101b92bc70db68";

    private static final String FIND_RECIPE_COMMAND = "-ingredients";
    private static final String BLACKLIST_COMMAND = "-blacklist";
    private static final String RANDOM_RECIPE_COMMAND = "-chefrandom";
    private static final String RECIPE_BOOK = "-recipebook";

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

    private transient ListenerAdapter listenerAdapter;

    private HashMap<String, RecipeBook> recipeBookHashMap;
    private HashMap<String, Set<String>> blackListHashMap;

    public ChefBot() {

        buildListenerAdapter();

        recipeBookHashMap = new HashMap<>();

        blackListHashMap = new HashMap<>();

    }

    private void readObject(ObjectInputStream objectInputStream) throws ClassNotFoundException, IOException {

        objectInputStream.defaultReadObject();

        buildListenerAdapter();

    }

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

    public ListenerAdapter getListenerAdapter() {

        return listenerAdapter;
    }

    private boolean isChefBot(GenericMessageEvent event) {

        User user = event.getChannel().retrieveMessageById(event.getMessageId()).complete().getAuthor();

        return user.isBot() && user.getName().equals("Chef Wumpus");
    }

    private boolean isChefReaction(GenericMessageReactionEvent event) {

        return event.getReactionEmote().toString().equals("RE:U+1f468U+200dU+1f373");
    }

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

    private static void sendMessage(GenericMessageEvent event, String message) {

        event.getChannel().sendMessage(message).queue();

    }

    private static void sendMessage(GenericMessageEvent event, MessageEmbed message) {

        event.getChannel().sendMessage(message).queue();

    }

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