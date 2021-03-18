import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChefBot extends ListenerAdapter {

    private static final String FIND_RECIPE_COMMAND = "-ingredients";
    private static final String WHITELIST_COMMAND = "-whitelist";

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
     * Receives Messages from discord bot and handles them according to content
     * @param event the MessageReceivedEvent to use
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        String rawString = event.getMessage().getContentRaw();

        if(!rawString.isEmpty()) {

            Command command = Command.getCommand(rawString);

            switch(command.type) {

                case FIND_RECIPE_COMMAND:

                    try{

                        for(Recipe recipe : RecipeBook.getRecipes(command.arguments, getNumRecipes(command.arguments))) {

                            event.getChannel().sendMessage(recipe.getMessageEmbed()).queue();

                        }

                    }catch(IOException e) {

                        event.getChannel().sendMessage("Sorry! Couldn't find any recipes related to your ingredients.").queue();

                        return;

                    }

                    break;

                case WHITELIST_COMMAND:

                    break;

            }

        }

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

}